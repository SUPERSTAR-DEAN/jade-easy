package protocols.vickrey.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 *
 * 【Vickrey 拍卖的拍卖方】与 [sealedEnglishAuction/AuctioneerAgent](../sealedEnglishAuction/agents/AuctioneerAgent.java)
 * 的关键差别就在 handleAllResponses 里"选中标者"的方式：
 *   - 英式拍卖：中标者支付**自己的出价**
 *   - Vickrey：中标者支付**第二高的出价**（这就是本类追踪 presqueMaxi 的原因）
 *
 * Vickrey 拍卖的设计之美：参与者有动机如实出价（自己的真实心理价），
 * 因为无论报多高，最后付的都是第二高价。
 *
 * @author eadam
 */
public class AuctioneerAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     *
     * 【setup】建窗口
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a...
     *
     * 【创建拍卖协议行为】
     */
    private void createOffer(String id, String objet) {

        // 【构造 CFP】
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        // 【注意这里的服务名是 auction/participant，而英式拍卖用的是 auction/bidder】
        //   说明这两个模块互不干扰，可以同时跑
        var adresses = AgentServicesTools.searchAgents(this, "auction", "participant");
        msg.addReceivers(adresses);
        println("(o)".repeat(30));

        println("bidders found: " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());

        // 【显式声明 Contract-Net 协议】
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println("I launch an auction for  " + msg.getContent());


        // 【挂载协议发起行为】
        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            // 【每收到一个 PROPOSE】只记录，实际决策在 handleAllResponses 里
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println(String.format("l'agent %s propose %s ", propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            // 【收到拒绝参拍】
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I receive a refuse from " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            // 【所有响应收齐后触发】这里做 Vickrey 的核心决策：选出**最高**和**第二高**
            @Override
            protected void handleAllResponses(List<ACLMessage> leursOffres, List<ACLMessage> mesRetours) {
                // 【maxi = 最高出价，presqueMaxi = 第二高】
                int maxi = Integer.MIN_VALUE;
                int presqueMaxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> listeOffres = new ArrayList<>(leursOffres);

                //we keep only the proposals
                // 【只保留 PROPOSE】过滤掉 REFUSE
                listeOffres.removeIf(msg -> msg.getPerformative() != ACLMessage.PROPOSE);
                List<ACLMessage> listeReponses = new ArrayList<>(listeOffres.size());

                StringBuilder sb = new StringBuilder("To summarize: \n");
                for (ACLMessage offre : listeOffres) {
                    //by default, we build a reject answer for each proposal; we go back to the best offer later
                    // 【默认构造 REJECT_PROPOSAL 回复】
                    var retour =offre.createReply();
                    retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    listeReponses.add(retour);
                    int valeurProposition = Integer.parseInt(offre.getContent());
                    sb.append("\treceived this proposal from  ").append(offre.getSender().getLocalName()).append(" :: ").append(valeurProposition).append("\n");
                    // 【Vickrey 的核心逻辑：追踪两个最大值】
                    //   如果新出价 > 当前第二高：
                    //     - 如果新出价 > 当前最高：第二高 = 原最高，最高 = 新出价，中标者换成这个人
                    //     - 否则：第二高 = 新出价
                    //   这种"边扫边记两个最大值"是一次遍历的做法，比排序后再取更快
                    if (valeurProposition > presqueMaxi) {
                        if (valeurProposition > maxi) {
                            presqueMaxi = maxi;
                            maxi = valeurProposition;
                            msgPourMeilleurOffreur = retour;
                        }
                        else presqueMaxi = valeurProposition;
                    }
                }
                sb.append("~".repeat(30)).append("\n");
                sb.append("best bidding = ").append(maxi).append("\n");
                sb.append("nearest best bidding = ").append(presqueMaxi).append("\n");
                println(sb.toString());

                int finalMaxi = maxi;
                listeReponses.forEach(msg -> msg.setContent("Refuse, sorry, your bidding as not been kept..."));
                //on avait garde un pointeur vers le message envoye a la meilleure offre, qu'on accepte finalement
                // 【Vickrey 的关键：中标者付的不是自己的出价，而是"第二高"presqueMaxi】
                if (msgPourMeilleurOffreur != null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("**Auctioned-sold** for " + presqueMaxi);
                }
                mesRetours.addAll(listeReponses);
            }


            //function triggered by a INFORM msg : the best bidder confirm the sale
            // 【中标者确认成交】
            @Override
            protected void handleInform(ACLMessage inform) {
                println("_".repeat(30));
                println("Sale confirmed with  " + inform.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }

            @Override
            //function triggered by a FAILURE msg : the best bidder cancel the sale
            // 【中标者反悔】
            protected void handleFailure(ACLMessage failure) {
                println("_".repeat(30));
                println("PB : Sale cancelled with  " + failure.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }
        };

        addBehaviour(init);

    }

    // 【按钮事件】
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    // 【发起拍卖】物品 "livre"（书）
    public void launchRequest() {
        createOffer("echereNo1", "livre");
    }

}
