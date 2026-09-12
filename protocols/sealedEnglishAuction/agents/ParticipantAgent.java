package protocols.sealedEnglishAuction.agents;

import jade.core.AgentServicesTools;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.awt.*;
import java.util.Random;


/**
 * agent that waits for a message from a CFP protocol, prepares the response, and returns it
 *
 * 【密封英式拍卖的参与者】Contract-Net 响应方，随机决定"参拍"或"放弃"。
 *
 * 逻辑：
 *   - 收到 CFP 时，掷随机数 0-99，若 <33 就 REFUSE（放弃参拍），否则随机给一个 0-99 的出价
 *   - 收到 ACCEPT_PROPOSAL 时，回一条 INFORM 表示确认成交
 *   - 收到 REJECT_PROPOSAL 时，只是打印"被拒"
 *
 * 对比 [vickrey/ParticipantAgent](../vickrey/agents/ParticipantAgent.java)：结构完全一样，
 * 差别在注册的服务名（本类 "auction/bidder"，vickrey "auction/participant"），
 * 因此两个拍卖可**同时运行**互不干扰。
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ParticipantAgent extends AgentWindowed {

    /**
     * agent setup
     * - registration to the service "auction"-"bidder"
     * - add the contractnetresponder protocol behaviour
     * - create the gui
     *
     * 【setup】注册服务 + 挂 ContractNetResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));
        Random hasard = new Random();

        // 【注册为 auction/bidder 服务】AuctioneerAgent 用 searchAgents("auction","bidder") 能找到本智能体
        AgentServicesTools.register(this, "auction", "bidder");
        // 【消息模板】只处理会话 ID = "auction1" 的 CFP，与 AuctioneerAgent 匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("auction1");

        // 【挂载协议响应行为】
        ContractNetResponder bidding = new ContractNetResponder(this, model) {

            //function triggered by a PROPOSE msg : decide to bid or not, return a response
            // 【收到 CFP】决定参拍还是放弃
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                window.setBackgroundTextColor(Color.WHITE);
                ACLMessage reponse = cfp.createReply();
                println("'%s' proposes the object '%s' for bidding...".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                // 【生成随机出价 0-99】
                int offer = hasard.nextInt(0, 100);
                //here, the agent refuses to bid 1 times on 3 (when the random offer<33)
                // 【33% 概率放弃】用同一次随机值决定，简化逻辑
                if (offer < 33) {
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I decide to not bid for this object.");
                    reponse.setPerformative(ACLMessage.REFUSE);
                } else {
                    println(String.format("I propose %d to buy '%s' to the agent : '%s'", offer, cfp.getContent(),
                            cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offer));
                }
                println("-".repeat(30));
                return reponse;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the auctioneer accept my offer
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            // 【收到 ACCEPT_PROPOSAL】中标，回一条 INFORM 确认成交
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                window.setBackgroundTextColor(Color.YELLOW);
                println("-".repeat(30));
                println("OFFER ACCEPTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has accepted with this message '%s'".formatted(cfp.getSender().getLocalName(),
                        accept.getContent()));
                println("_".repeat(40));
                println("");
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            //function triggered by a REJECT_PROPOSAL msg : the auctioneer rejected my offer
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the reject sent by the auctioneer
            // 【收到 REJECT_PROPOSAL】落选，只打印
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                window.setBackgroundTextColor(Color.RED);
                println("-".repeat(30));
                println("OFFER REJECTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has declined with this message '%s'".formatted(cfp.getSender().getLocalName(), reject.getContent()));
                println("_".repeat(40));
                println("");
            }
        };

        addBehaviour(bidding);

    }



    @Override
    public void takeDown() {
        //before leaving, the agent unsubscribe from its services
        // 【下线前注销所有服务】
        AgentServicesTools.deregisterAll(this);
        System.err.println(this.getLocalName() + ", I leave the platform...");
    }

}
