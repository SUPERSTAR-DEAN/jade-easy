package voyagesEnVille.comportements;

import voyagesEnVille.gui.AgenceGui;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import voyagesEnVille.agents.AgenceAgent;
import voyagesEnVille.data.Journey;
import voyagesEnVille.data.JourneysList;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Journeys Seller Behaviour by contract net
 *
 * 【旅行社的"卖行程"行为】Contract-Net 响应方，由 AgenceAgent 挂载。
 *
 * 流程：
 *   - 收到旅客的 CFP → handleCfp()：把整个 catalog 作为 Java 对象塞进 PROPOSE 消息
 *   - 旅客选中后回 ACCEPT_PROPOSAL，里面带着选中的 Journey 列表 → handleAcceptProposal()
 *   - 本行为把选中的每条行程的"剩余座位数 -1"
 *   - 旅客拒接 → handleRejectProposal()
 *
 * 【关键技术：序列化 Java 对象进 ACL 消息】
 *   - `msg.setContentObject(catalog)` 把 JourneysList 序列化成字节流
 *   - 接收方用 `msg.getContentObject()` 反序列化
 *   - 要求对象及其字段都实现 Serializable
 *
 * @author revised by Emmanuel ADAM
 * @version 191017
 */
@SuppressWarnings("serial")
public class ContractNetVente extends ContractNetResponder {

    /**
     * catalog of the proposed journeys
     */
    // 【行程目录】本旅行社提供的所有行程
    private final JourneysList catalog;


    /**
     * agent gui
     */
    // 【关联的窗口】
    private final AgenceGui window;

    /**
     * Initialisation du contract net
     *
     * @param agent    agent agence lie
     * @param template modele de message a attendre
     * @param _catalog catalogue des voyages
     *
     * 【构造】保存智能体、窗口、目录的引用
     */
    public ContractNetVente(Agent agent, MessageTemplate template, JourneysList _catalog) {
        super(agent, template);
        var monAgent = (AgenceAgent) agent;
        window = monAgent.getWindow();
        catalog = _catalog;
    }

    /**
     * methode lancee a la reception d'un appel d'offre
     *
     * @param cfp l'appel recu
     * @throws NotUnderstoodException si le message n'est pas compris
     * @throws RefuseException        s'il n'y a pas de trajet en catalogue
     * @see ContractNetResponder#handleCfp(ACLMessage)
     *
     * 【收到 CFP】把整个目录作为 PROPOSE 回复
     */
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        window.println("Agent " + myAgent.getLocalName() + ": CFP recu de " + cfp.getSender().getLocalName());
        // 【目录空就拒绝】抛 RefuseException 是 JADE 协议的标准拒绝方式
        if (catalog.isEmpty()) throw new RefuseException("no journey !");
        var propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        try {
            // 【序列化目录进消息】setContentObject 把 Java 对象转字节流
            propose.setContentObject(catalog);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propose;
    }


    /**
     * methode lancee suite a la reception d'une acceptation de l'offre par l'acheteur
     *
     * @param cfp     l'appel a proposition initial
     * @param propose la proposition retourne par l'agent
     * @param accept  le message d'acceptation de l'offre
     * @return le message de confirmation de la vente
     * @throws FailureException si le livre n'est plus disponible (si la transaction echoue)
     * @see ContractNetResponder#handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
     *
     * 【收到 ACCEPT_PROPOSAL】旅客选中了某些行程，本方法扣减座位
     */
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        window.println(" RECU UN ACCORD DE " + accept.getSender().getLocalName() + " !!!");
        ArrayList<Journey> liste = null;
        try {
            // 【反序列化旅客选中的行程列表】
            liste = (ArrayList<Journey>) accept.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (liste != null) {
            window.println("Il veut ");
            liste.forEach(j -> window.println(j.toString()));
            window.println("  !!!!");
            inform.setContent("ok pour ces " + liste.size() + " tickets...");
            //for each ticket bought, remove 1 place
            // 【每张票扣一个座位】
            liste.forEach(this::removeTicket);
        }
        return inform;
    }


    /**get in the catalog the journey corresponding to j and remove one place*/
    // 【扣减座位】从目录里找到匹配的行程（起点+终点+出发时间都一致），座位数 -1
    private void removeTicket(Journey j) {
        ArrayList<Journey> list = catalog.getJourneysFrom(j.getStart());
        list.stream().filter(journey -> (journey.getStop().equals(j.getStop()) && journey.getDepartureDate() == j.getDepartureDate()))
                .forEach(journey -> journey.setPlaces(journey.getPlaces() - 1));
    }

    /**
     * methode lancee suite a la reception d'un refus de l'offre par l'acheteur
     *
     * @param cfp     l'appel a proposition initial
     * @param propose la proposition retourne par l'agent
     * @param reject  le message de refus de l'offre
     * @see ContractNetResponder#handleRejectProposal(ACLMessage, ACLMessage, ACLMessage)
     *
     * 【收到 REJECT_PROPOSAL】旅客没选本旅行社
     */
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        window.println("Agent " + reject.getSender().getLocalName() + " a rejete la proposition pour " + reject.getContent());
    }
}
