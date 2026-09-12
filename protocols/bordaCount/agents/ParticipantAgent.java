package protocols.bordaCount.agents;


import jade.core.Agent;
import jade.core.AgentServicesTools;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * agent that waits for a message from a CFP protocol, prepares the response, and returns it
 * use AgentWindowed to display the messages in a window if few agents are used, otherwise use Agent to avoid too many windows (nb agents between 100 and 10000...)
 *
 * 【投票参与者（Contract-Net 响应方）】Contract-Net 协议在 JADE 里的响应者实现。
 *
 * 【Contract-Net 与 FIPA-Request 的区别】
 *   - FIPA-Request：一对多的请求，所有响应者都可以响应
 *   - Contract-Net：一对多的**招标**，响应者出价后由发起方挑选"中标者"，其余被拒绝
 *
 * 本例把"招标"改造成"投票"：
 *   - 发起方（PollingStation）发 CFP：请给出你的偏好排序
 *   - 参与者（本类）回 PROPOSE：option1>option2>...
 *   - 发起方"接受所有出价"（这里其实不招标，只是借用协议的消息流）
 *   - 参与者最后收到 ACCEPT_PROPOSAL，回一条 INFORM 确认
 *
 * 注释里的 `AgentWindowed` vs `Agent` 是关键提示：智能体数量少用 Windowed，多则用 Agent 避免窗口爆炸。
 *
 * @author eadam
 */
public class ParticipantAgent extends AgentWindowed { // extends Agent {

    /**
     * agent setup
     * - registration to the service "vote"-"participant"
     * - add the contractnetresponder protocol behaviour
     * - create the gui
     *
     * 【setup】注册服务 + 挂 ContractNetResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        // 【注册为 vote/participant 服务】
        AgentServicesTools.register(this, "vote", "participant");

        // 【只处理会话 ID = "voteNo1" 的 CFP】
        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        // 【挂载 ContractNet 响应行为】
        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            //function triggered by a PROPOSE msg : send back the ranking
            // 【覆写 handleCfp】收到 CFP 后回一条 PROPOSE 消息，内容是自己的偏好排序
            //   注意返回类型是 ACLMessage，抛三种 FIPA 异常表示不同失败模式
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                println("~".repeat(40));
                println(cfp.getSender().getLocalName() + " proposes this options: " + cfp.getContent());
                ACLMessage answer = cfp.createReply();
                answer.setPerformative(ACLMessage.PROPOSE);
                // 【随机排序】这里只是模拟"我随便选"，实际业务逻辑替换这里即可
                String choice = makeItsChoice(cfp.getContent());
                answer.setContent(choice);
                return answer;
            }

            /**proposals in the form option1,option2,option3,option4,.....
             * * he returns his choice by ordering the options and giving their positions
             * @param offres list of proposals in the form of option1,option2,option3,option4
             * @return orderly choice in the form of option2_1,option4_2,option3_3,option1_4
             * *
             * 【生成偏好排序】输入 "option1,option2,option3" 输出 "option2>option4>option3>option1"
             *   用 > 分隔是因为发起方那边用 > 来切分并计算点数
             */
            private String makeItsChoice(String offres) {
                ArrayList<String> choice = new ArrayList<>(List.of(offres.split(",")));
                // 【Collections.shuffle】洗牌——每个参与者给出的排序不同
                Collections.shuffle(choice);
                StringBuilder sb = new StringBuilder();
                String pref = ">";
                for (String s : choice) sb.append(s).append(pref);
                // 【去掉末尾多余的 >】
                String proposition = sb.substring(0, sb.length() - 1);
                println("I propose this ranking: " + proposition);
                return proposition;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the polling station agent  accept the vote
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            // 【覆写 handleAcceptProposal】中标后回一条 INFORM 确认
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("=".repeat(15));
                println("END OF ROUND");
                println(cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                // 【accept.getContent() 里装的是当选的选项】
                println(cfp.getSender().getLocalName() + " accepted my vote and sent the result:  " + accept.getContent());
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            //function triggered by a REJECT_PROPOSAL msg : the auctioneer rejected my vote !
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the reject sent by the auctioneer
            // 【覆写 handleRejectProposal】被拒绝时的处理
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                println("=".repeat(10));
                println("VOTE REJECTED");
                println(cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                println(cfp.getSender().getLocalName() + " refused ! with this message: " + reject.getContent());
            }


        };

        addBehaviour(comportementVote);

    }

    //before leaving, the agent unsubscribe from its services
    // 【takeDown】下线前从黄页注销
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
//        System.err.println(this.getLocalName() + ", I leave the platform...");
    }

}
