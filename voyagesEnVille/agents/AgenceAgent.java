package voyagesEnVille.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import voyagesEnVille.comportements.ContractNetVente;
import voyagesEnVille.data.Journey;
import voyagesEnVille.data.JourneysList;
import voyagesEnVille.launch.LaunchSimu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

/*
 * 【旅行社智能体·卖方】这个类代表一家旅行社，在多智能体系统中扮演“卖方”角色：
 * 它从 CSV 文件读取自己的旅行目录（catalogue），并通过 ContractNet（合同网）协议
 * 响应旅行者发来的招标（CFP），出售车票/行程（journey）。
 */
/**
 * Journey Seller
 *
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class AgenceAgent extends GuiAgent {
    /**
     * code shared with the gui to quit the agent
     */
    public static final int EXIT = 0;

    /**
     * catalog of the proposed journeys
     */
    private JourneysList catalog;
    /**
     * graphical user interface linked to the seller agent
     */
    private voyagesEnVille.gui.AgenceGui window;
    /**
     * topic from which the alert will be received
     */
    private AID topic;

    // Initialisation de l'agent
    // 【setup 方法】智能体启动时执行：读取 CSV 目录、注册到黄页、监听交通广播、准备出售行为
    @Override
    protected void setup() {
        final Object[] args = getArguments(); // Recuperation des arguments
        catalog = new JourneysList();
        window = new voyagesEnVille.gui.AgenceGui(this);
        window.display();

        if (args != null && args.length > 0) {
            fromCSV2Catalog((String) args[0]);
        }

        // 向黄页服务（DF，Directory Facilitator）注册：类型是 "travel agency"（旅行社）、角色是 "seller"（卖家）
        AgentServicesTools.register(this, "travel agency", "seller");

        //REGLAGE ECOUTE DE LA RADIO（设置收听广播）
        // 生成一个“交通新闻”话题（topic）地址，用于接收广播消息
        topic = AgentServicesTools.generateTopicAID(this, "TRAFFIC NEWS");

        //ecoute des messages radio（收听广播消息）
        // 添加一个持续监听的“接收行为”：一旦收到该话题上的广播，就打印出来
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topic), true, (a, m)->{
                    println("Message recu sur le topic " + topic.getLocalName() + ". Contenu " + m.getContent()
                            + " emis par :  " + m.getSender().getLocalName());
                }));

        //FIN REGLAGE ECOUTE DE LA RADIO


        // attendre une demande de catalogue & achat（等待“要目录/购买”的请求）
        // 只匹配“合同网协议 + CFP 招标”消息，然后交给 ContractNetVente 行为去处理
        var template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        addBehaviour(new ContractNetVente(this, template, catalog));

    }

    // Fermeture de l'agent
    // 【takeDown 方法】智能体关闭前执行：从黄页注销、关闭窗口
    @Override
    protected void takeDown() {
        // S'effacer du service pages jaunes
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            LaunchSimu.logger.log(Level.SEVERE, fe.getMessage());
        }
        LaunchSimu.logger.log(Level.INFO, "Agent Agence : " + getAID().getName() + " quitte la plateforme.");
        if(window!=null) {
            window.dispose();
            System.out.println(getLocalName() +   ">>> I leave the platform. ");
        }
    }

    /**
     * methode invoquee par la gui
     */
    // 【onGuiEvent 方法】接收来自 GUI 窗口的事件（例如点击关闭按钮则退出）
    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == AgenceAgent.EXIT) {
            doDelete();
        }
    }

    /**
     * initialize the catalog from a cvs file<br>
     * csv line = origine, destination,means,departureTime,duration,financial
     * cost, co2, confort, nbRepetitions(optional),frequence(optional)
     *
     * @param file name of the cvs file
     */
    // 【fromCSV2Catalog 方法】从 CSV 文件逐行读取行程（起点、终点、交通方式、时间、成本等），并加入目录
    private void fromCSV2Catalog(final String file) {
        List<String> lines = null;
        try {lines = Files.readAllLines(new File(file).toPath());}
        catch (IOException e) {
            window.println("fichier " + file + " non trouve !!!");
        }
        if(lines!=null)
        {
            int nbLines = lines.size();
            for(int i=1; i<nbLines; i++){
            // 每一行用逗号分隔：起点, 终点, 交通方式, 出发时间, 时长, 成本, co2, 舒适度, ...
            String[] nextLine = lines.get(i).split(",");
            String origine = nextLine[0].trim().toUpperCase();      // origine：起点
            String destination = nextLine[1].trim().toUpperCase();  // destination：终点
            String means = nextLine[2].trim();                      // means：交通方式（velo/car/bus/tram）
            int departureDate = Integer.parseInt(nextLine[3].trim());
            int duration = Integer.parseInt(nextLine[4].trim());
            double cost = Double.parseDouble(nextLine[5].trim());
            int co2 = Integer.parseInt(nextLine[6].trim());
            int confort = Integer.parseInt(nextLine[7].trim());
            int nbParams = nextLine.length;
            int nbRepetitions = (nbParams < 9) ? 0 : Integer.parseInt(nextLine[8].trim());
            int frequence = (nbRepetitions == 0) ? 0 : Integer.parseInt(nextLine[9].trim());
            Journey firstJourney = new Journey(origine, destination, means, departureDate, duration, cost, co2, confort);
            firstJourney.setProposedBy(this.getLocalName());
            // 根据交通方式设定每趟可售座位数：自行车 50、汽车 3、公交 50、电车 200
            int nbPlaces = switch (means) {
                case "velo" -> 50;
                case "car" -> 3;
                case "bus" -> 50;
                case "tram" -> 200;
                default -> 0;
            };
            firstJourney.setPlaces(nbPlaces);
            window.println(firstJourney.toString());
            catalog.addJourney(firstJourney);
            if (nbRepetitions > 0) {
                repeatJourney(departureDate, nbRepetitions, frequence, firstJourney);
                }
            }
        }
    }

    /**
     * repeat a journey on a sequence of dates into a catalog
     *
     * @param departureDate date of the first journey
     * @param nbRepetitions nb of journeys to add
     * @param frequence     frequency of the journeys in minutes
     * @param journey       the first journey to clone
     */
    // 【repeatJourney 方法】按固定频率复制多趟行程（例如每隔 frequence 分钟开一班）
    private void repeatJourney(final int departureDate, final int nbRepetitions, final int frequence,
                               final Journey journey) {
        int nextDeparture = departureDate;
        for (int i = 0; i < nbRepetitions; i++) {
            final Journey cloneJ = journey.clone();
            nextDeparture = Journey.addTime(nextDeparture, frequence);
            cloneJ.setDepartureDate(nextDeparture);
            window.println(cloneJ.toString());
            catalog.addJourney(cloneJ);
        }
    }

    /**remove in the catalog 1 place for the journey that corresponds to the journey j*/




    /**
     * display a msg on the window
     */
    public void println(String msg) {
        window.println(msg);
    }

    ///// GETTERS AND SETTERS
    public voyagesEnVille.gui.AgenceGui getWindow() {
        return window;
    }


}
