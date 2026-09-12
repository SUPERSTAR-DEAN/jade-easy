package voyagesEnVille.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import voyagesEnVille.comportements.ContractNetAchat;
import voyagesEnVille.data.ComposedJourney;
import voyagesEnVille.data.JourneysList;
import voyagesEnVille.gui.TravellerGui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/*
 * 【旅行者智能体·买方】这个类代表客户/旅行者，在多智能体系统中扮演“买方”角色：
 * 它向多家旅行社发起招标（CFP），收集各家的旅行目录，按自己的偏好（成本/时长/舒适度等）选择并购买行程。
 */
/**
 * Journey searcher
 *
 * @author Emmanuel ADAM
 */
public class TravellerAgent extends GuiAgent {
    /**
     * code pour ajout de livre par la gui
     */
    public static final int EXIT = 0;
    /**
     * code pour achat de livre par la gui
     */
    public static final int BUY_TRAVEL = 1;

    /**
     * liste des vendeurs
     */
    private ArrayList<AID> vendeurs;

    /**
     * catalog received by the sellers
     */
    private JourneysList catalogs;

    /**
     * delay in minute between two rides in a junction (in minutes)
     * */
    int delay = 90;

    /**
     * the journey chosen by the agent
     */
    private ComposedJourney myJourney;

    /**
     * topic from which the alert will be received
     */
    private AID topic;

    /**
     * gui
     */
    private TravellerGui window;

    /**
     * Initialisation de l'agent
     */
    // 【setup 方法】启动时创建窗口、订阅黄页以发现旅行社、监听交通广播
    @Override
    protected void setup() {
        this.window = new TravellerGui(this);
        window.setColor(Color.cyan);
        window.println("Hello! AgentAcheteurCN " + this.getLocalName() + " est pret. ");
        window.setVisible(true);

        vendeurs = new ArrayList<>();
        detectAgences();

        topic = AgentServicesTools.generateTopicAID(this, "TRAFFIC NEWS");
        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topic), true, (a, m) -> {
            println("Message recu sur le topic " + topic.getLocalName() + ". Contenu " + m.getContent()
                    + " emis par :  " + m.getSender().getLocalName());
        }));

    }


    /**
     * ecoute des evenement de type enregistrement en tant qu'agence aupres des pages jaunes
     */
    // 【detectAgences 方法】订阅黄页服务：当有旅行社注册/注销时，自动更新本地的“卖家列表”
    private void detectAgences() {
        var model = AgentServicesTools.createAgentDescription("travel agency", "seller");
        vendeurs = new ArrayList<>();

        //souscription au service des pages jaunes ... （订阅黄页服务：监听 "travel agency"/"seller" 服务的动态变化）
        // DFSubscriber 会在有旅行社注册（onRegister）或注销（onDeregister）时回调对应方法
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                vendeurs.add(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est inscrit en tant qu'agence : " + model.getAllServices().get(0));
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                vendeurs.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est desinscrit de  : " + model.getAllServices().get(0));
            }

        });

    }

    /**
     * compute a composed journey from a departure to an arrival point
     * @param from       departure point
     * @param to         arrival point
     * @param departure  desired departure time (in hhmm)
     * @param preference preference for the choice of the journey (cost, confort, duration, duration-cost)
     * */
    // 【computeComposedJourney 方法】在收到的目录里找出从起点到终点的组合行程，并按用户偏好排序后选出最优的一个
    public void computeComposedJourney(final String from, final String to, final int departure,
                                       final String preference) {
        final List<ComposedJourney> journeys = new ArrayList<>();

        // 递归查找所有“直接或中转”的可行行程（允许换乘）
        final boolean result = catalogs.findIndirectJourney(from, to, departure, 60, new ArrayList<>(),
                new ArrayList<>(), journeys);

        if (!result) {
            println("no journey found !!!");
        }
        if (result) {
            //oter les voyages demarrant trop tard（剔除“比期望出发时间晚太久”的行程，delay=90 分钟）
            journeys.removeIf(j -> j.getJourneys().getFirst().getDepartureDate() - departure > delay);
            // 按用户偏好排序：时长最短 / 舒适度最高 / 成本最低 / 时长+成本综合
            switch (preference) {
                case "duration" -> {
                    journeys.sort(Comparator.comparingDouble(ComposedJourney::getDuration));
                }
                case "confort" -> journeys.sort(Comparator.comparingInt(ComposedJourney::getConfort).reversed());
                case "cost" -> journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
                case "duration-cost" ->
                //        journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
                // 综合“时长差异 + 成本差异”，取最小的排前面（差异越小越好）
                journeys.sort((j1, j2) -> {
                    var difDuration = j1.getDuration() - j2.getDuration() / Math.max(j2.getDuration(),j1.getDuration());
                    var difCost = j1.getCost() - j2.getCost() / Math.max(j2.getCost(),j1.getCost());
                    return (int)(10*(difDuration + difCost));});
                default -> journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
            }
            myJourney = journeys.getFirst();
            println("I choose this journey : " + myJourney);
        }
    }

    /**
     * get event from the GUI
     */
    // 【onGuiEvent 方法】接收 GUI 事件：退出，或按用户输入发起一次 ContractNet 招标（购买行程）
    @Override
    protected void onGuiEvent(final GuiEvent eventFromGui) {
        if (eventFromGui.getType() == TravellerAgent.EXIT) {
            doDelete();
        }
        if (eventFromGui.getType() == TravellerAgent.BUY_TRAVEL) {
            addBehaviour(new ContractNetAchat(this, new ACLMessage(ACLMessage.CFP),
                    (String) eventFromGui.getParameter(0), (String) eventFromGui.getParameter(1),
                    (Integer) eventFromGui.getParameter(2), (String) eventFromGui.getParameter(3)));
        }
    }

    // 'Nettoyage' de l'agent
    // 【takeDown 方法】关闭窗口、离开平台
    @Override
    protected void takeDown() {
        if (window != null) {
            window.dispose();
            System.out.println(getLocalName() + ">>> I leave the platform. ");
        }
    }

    ///// SETTERS AND GETTERS

    /**
     * @return agent gui
     */
    public TravellerGui getWindow() {
        return window;
    }


    /**
     * @return the vendeurs
     */
    public List<AID> getVendeurs() {
        return (ArrayList<AID>) vendeurs.clone();
    }


    /**
     * print a message on the window lined to the agent
     *
     * @param msg text to display in th window
     */
    public void println(final String msg) {
        window.println(msg);
    }

    /**
     * set the list of journeys
     */
    public void setCatalogs(final JourneysList catalogs) {
        this.catalogs = catalogs;
    }


    public ComposedJourney getMyJourney() {
        return myJourney;
    }

}
