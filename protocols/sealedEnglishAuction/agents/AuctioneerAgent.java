package protocols.sealedEnglishAuction.agents;


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
 * class of an agent that proposes a call for proposal using the ContractNet protocol
 *
 * 【密封英式拍卖的拍卖方】用 Contract-Net 协议组织一次密封投标拍卖：
 *   - 发起方（本类）向所有参与者发 CFP（投标征询）
 *   - 参与者回 PROPOSE（自己的出价）或 REFUSE（拒绝参拍）
 *   - 发起方从所有出价里选出最高者，ACCEPT_PROPOSAL 中标者，REJECT_PROPOSAL 其他
 *   - 中标者回 INFORM（确认成交）或 FAILURE（放弃成交）
 *
 * 与 [bordaCount](../bordaCount/) 里的投票站是**同一套协议，不同的业务规则**——
 * 只是 `handleAllResponses` 里"如何决定中标者"不同。
 *
 * @author eadam
 */
public class AuctioneerAgent extends AgentWindowed {

    /**
     * setup the gui
     *
     * 【setup】只建窗口，拍卖由按钮触发
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a call for proposal
     *
     * 【创建拍卖协议行为】构造 CFP → 找参与者 → 挂 ContractNetInitiator
     */
    private void createOffer(String id, String object) {

        // 【构造 CFP：Call For Proposal】
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(object);   // 【拍卖物品描述】如 "book"

        // 【服务发现】找到所有注册为 auction/bidder 的参与者
        var adresses = AgentServicesTools.searchAgents(this, "auction", "bidder");
        msg.addReceivers(adresses);
        println("(o)".repeat(30));

        println("bidders found: " + Arrays.stream(adresses).map(AID::getLocalName).toList());

        // 【显式声明协议】Contract-Net 必须设置，否则响应方不会按协议处理
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // 【回复截止】1 秒内不回当没回
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println(" I start the auction for " + msg.getContent());


        // 【挂载协议发起行为】
        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            //function triggered by a PROPOSE msg
            // @param propose     the received propose message
            // @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
            //                    list that can be modified here or at once when all the messages are received
            // 【每收到一个 PROPOSE 就触发】这里只记录，实际决策在 handleAllResponses 里
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println(String.format("agent %s proposes %s ", propose.getSender().getLocalName(),
                        propose.getContent()));
            }

            //function triggered by a REFUSE msg
            // 【收到拒绝参拍】
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I received a refuse from " + refuse.getSender().getLocalName());
            }

            //function triggered when all the responses are received (or after the waiting time)
            //@param theirOffers the list of message sent by the bidders
            //@param myAnswers the list of answers for each proposal
            // 【所有响应收齐后触发】这里做"选最高出价者"的决策
            @Override
            protected void handleAllResponses(List<ACLMessage> theirOffers, List<ACLMessage> myAnswers) {
                // 【记录当前最高出价】初值 Integer.MIN_VALUE 表示"还没收到任何出价"
                int maxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> listeOffres = new ArrayList<>(theirOffers);

                //we keep only the proposals only
                // 【只保留 PROPOSE 消息】过滤掉 REFUSE
                listeOffres.removeIf(msg -> msg.getPerformative() != ACLMessage.PROPOSE);
                List<ACLMessage> listeReponses = new ArrayList<>(listeOffres.size());

                StringBuilder sb = new StringBuilder("In short: \n");
                for (ACLMessage offre : listeOffres) {
                    //by default, we build a reject answer for each proposal; we go back to the best offer later
                    // 【默认构造 REJECT_PROPOSAL】先给每个出价都准备好"拒绝"回复
                    var retour = offre.createReply();
                    retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    listeReponses.add(retour);
                    int valeurProposition = Integer.parseInt(offre.getContent());
                    sb.append("\treceived this proposal from ").append(offre.getSender().getLocalName()).append(" :: ").append(valeurProposition).append("\n");
                    // 【如果这个出价比当前最高还高，替换】
                    if (valeurProposition > maxi) {
                        maxi = valeurProposition;
                        msgPourMeilleurOffreur = retour;
                    }
                }
                sb.append("~".repeat(30)).append("\n");
                sb.append("best offer = ").append(maxi).append("\n");
                println(sb.toString());

                // 【把所有回复的内容填成"很遗憾，最高出价是 X"】
                int finalMaxi = maxi;
                listeReponses.forEach(msg -> msg.setContent("Refused, sorry, the best offer was " + finalMaxi));
                //we go back to the msg for the best bidder that we finally accept
                // 【把最高出价者的回复改成 ACCEPT_PROPOSAL】
                if (msgPourMeilleurOffreur != null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("SOLD for " + finalMaxi);
                }
                // 【把所有回复塞给 myAnswers】JADE 会自动发出
                myAnswers.addAll(listeReponses);
            }

            //function triggered by a INFORM msg : the best bidder confirm the sale
            // 【中标者回 INFORM：成交确认】
            @Override
            protected void handleInform(ACLMessage inform) {
                println("_".repeat(30));
                println("Sale confirmed with  " + inform.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }

            @Override
            //function triggered by a FAILURE msg : the best bidder cancel the sale
            // 【中标者回 FAILURE：反悔】
            protected void handleFailure(ACLMessage failure) {
                println("_".repeat(30));
                println("PB : Sale cancelled with  " + failure.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }

        };

        addBehaviour(init);

    }

    // 【按钮事件】触发拍卖
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    // 【发起拍卖】物品是 "book"
    public void launchRequest() {
        createOffer("auction1", "book");
    }

}
