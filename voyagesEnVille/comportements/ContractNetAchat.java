package voyagesEnVille.comportements;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import voyagesEnVille.agents.TravellerAgent;
import voyagesEnVille.data.Journey;
import voyagesEnVille.data.JourneysList;
import voyagesEnVille.gui.TravellerGui;

import java.io.IOException;
import java.util.*;


/*
 * 【买方行为（合同网·发起方）】这个类继承 ContractNetInitiator，代表“买方”发起的合同网协商：
 * 它把招标（CFP）发给所有旅行社，收集回复，选择最优行程，并向中标者发送“接受”（ACCEPT_PROPOSAL）。
 */
/**
 * Journey Buyer Behaviour by contract net
 *
 * @author revised by Emmanuel ADAM
 * @version 191017
 */
public class ContractNetAchat extends ContractNetInitiator {

    private final String from;
    private final String to;
    private final int departure;
    private final String preference;

    /**
     * agent gui
     */
    private final TravellerGui window;

    /**
     * acheteur lie a ce comportement
     */
    private final TravellerAgent monAgent;

    /**
     * initialisation
     *
     * @param agent       agent initiator
     * @param msg         initial message to send
     * @param _from       origine city
     * @param _to         destination city
     * @param _departure  date of departure
     * @param _preference criteria (cost, duration, ...)
     */
    // 【构造方法】初始化招标：设置合同网协议、指定 1 秒内回复、把所有旅行社加为接收者
    public ContractNetAchat(Agent agent, ACLMessage msg, final String _from, final String _to, final int _departure, final String _preference) {
        super(agent, msg);
        from = _from;
        to = _to;
        departure = _departure;
        preference = _preference;
        monAgent = (TravellerAgent) agent;
        window = monAgent.getWindow();
        // définition du prococole（指定协议为 FIPA 合同网）
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // Réponse plus tard dans 1 sec（要求对方在 1 秒内回复）
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
        List<AID> vendeurs = monAgent.getVendeurs();
        vendeurs.forEach(msg::addReceiver);
        // relancer le comportement pour fixer la date de remise au plus tard, les destinataires, ...
        this.reset(msg);
    }


    /**
     * methode lancee a la reception de chaque refus
     *
     * @param refuse refus recu
     */
    // 【handleRefuse 方法】某个卖家拒绝参与时被调用
    @Override
    protected void handleRefuse(ACLMessage refuse) {
        window.println("Agent " + refuse.getSender().getLocalName() + " refuse");
    }

    /**
     * methode lancee a la reception d'un message d'erreur (impossibilite de poursuivre la vente)
     *
     * @param failure erreur recue
     */
    // 【handleFailure 方法】某个卖家出错（无法继续交易）时被调用
    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // ERREUR : le destinataire n'existe pas
            window.println("Le destinataire n'existe pas...");
        } else
            window.println("Agent " + failure.getSender().getLocalName() + " a echoue");
    }

    /**
     * methode lancée si toutes les reponses sont arrivées ou si le temps est écoulé<br>
     * accepte la meilleure offre, calcul basé sur la notoriété la plus haute et le prix le plus bas à part égale ici<br>
     * n'accepte pas l'offre si dépasse le prix max fixé par l'acheteur
     *
     * @param responses   reponses recues
     * @param acceptances vecteur des messages à transmettre en retour aux réponses reçues
     * @see ContractNetInitiator#handleAllResponses(List, List)
     */
    // 【handleAllResponses 方法】合同网核心：收集所有卖家的报价，选出最优行程，向中标者发送接受消息
    @Override
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
        //catalog of journeys built from answers（汇总所有卖家发来的行程目录）
        var catalogs = new JourneysList();
        //map <name to the agent (agence), Msg built to answer to it>（卖家名 -> 准备回给它的消息）
        Map<String, ACLMessage> reponses = new HashMap<>();
        for (ACLMessage ans : responses) {
            if (ans.getPerformative() == ACLMessage.PROPOSE) {
                JourneysList receivedCatalog = null;
                try {
                    // 卖家把整个目录作为对象内容发送过来，这里读出来
                    receivedCatalog = (JourneysList) ans.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (receivedCatalog != null) {
                    catalogs.addJourneys(receivedCatalog);
                    monAgent.println("reçu de " + ans.getSender().getLocalName() + " : ");
                    monAgent.println(receivedCatalog.toString());
                }

            }
            // 先默认拒绝每个卖家，之后再对中标者改为接受
            var reply = ans.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            acceptances.add(reply);
            reponses.put(ans.getSender().getLocalName(), reply);
        }
        monAgent.setCatalogs(catalogs);
        monAgent.println("j'ai bien recu les calalogues : ");
        monAgent.println(catalogs.toString());
        monAgent.println("je fais mon choix...");
        // 让旅行者按偏好（成本/时长/舒适度）选出最优的组合行程
        monAgent.computeComposedJourney(from, to, departure, preference);
        //map <name to the agent (agence), list of journeys to buy to it>（按卖家分组，统计需要向每家买哪些行程）
        Map<String, ArrayList<Journey>> voyagesAAcheter = new HashMap<>();
        var journey = monAgent.getMyJourney();
        journey.getJourneys().forEach(j ->
                voyagesAAcheter.compute(j.getProposedBy(),
                        (agence, list) -> {
                            if (list == null) list = new ArrayList<>();
                            list.add(j);
                            return list;
                        }));
        // 对真正要购买的卖家，把默认的“拒绝”改成“接受”，并附上要买的行程列表
        voyagesAAcheter.forEach((agence, journeys) -> {
            var msg = reponses.get(agence);
            msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            try {
                msg.setContentObject(journeys);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * methode lancee a la reception d'un message d'information (vente confirmee)
     *
     * @param inform message recu
     */
    // 【handleInform 方法】收到卖家“成交确认”消息时被调用
    @Override
    protected void handleInform(ACLMessage inform) {
        window.println("Agent " + inform.getSender().getLocalName() + " : " + inform.getContent());
    }
}
