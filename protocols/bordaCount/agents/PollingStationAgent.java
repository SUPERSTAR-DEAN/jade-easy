package protocols.bordaCount.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * class of an agent that proposes a call for proposal using the ContractNet protocol
 *
 * 【投票站（Contract-Net 发起方）】用 Contract-Net 协议组织 Borda 计数投票。
 *
 * 【Borda 计数】每位参与者给出候选项的排序，第 1 名得 n 分、第 2 名得 n-1 分、以此类推。
 * 所有参与者的分数求和，分数最高的当选。平票则重新在平票选项间投票。
 *
 * 【协议消息流】
 *   Station --CFP-->  Participant1..N
 *                     |--PROPOSE (偏好排序)--> Station.handlePropose()
 *                     |--REFUSE  (可选)------> Station.handleRefuse()
 *                     ...
 *                     [收齐或超时]--> Station.handleAllResponses() → 计算票数
 *   Station --ACCEPT_PROPOSAL-->  Participant1..N (回当选结果)
 *                     |--INFORM (确认收到)--> Station.handleInform()
 *
 * 【平票处理】当选结果 >1 个时，用 WakerBehaviour 延迟 100ms 递归发起新一轮投票
 *
 * @author eadam
 */
public class PollingStationAgent extends AgentWindowed {

    /**
     * setup the gui
     *
     * 【setup】只建窗口，投票由按钮触发
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a call for proposal for each vote
     *
     * 【创建投票协议行为】核心方法。构造 CFP、找参与者、挂 ContractNetInitiator
     */
    private void createVote(String id, String object) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("Strat a vote for this options " + object);
        // 【初始化投票箱】每个候选项一个计数器
        HashMap<String, Integer> votes = new HashMap<>();
        for (Restaurant r : Restaurant.values()) votes.put(r.toString(), 0);
        votes.forEach((k, v) -> println("nb votes for " + k + " = " + v));
        println("-".repeat(40));

        // 【构造 CFP】CFP = Call For Proposal，招标/征询
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(object);

        // 【服务发现】找到所有注册为 vote/participant 的参与者
        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("nb participants found : " + adresses.length);
        println("Participants found : " + Arrays.stream(adresses).map(AID::getLocalName).toList());
        println("-".repeat(40));

        // 【显式声明协议】Contract-Net 必须设置 InteractionProtocol，否则响应方不会按协议回复
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // 【回复截止时间】1 秒后没回就当没回
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        // 【挂载 Contract-Net 发起行为】
        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            //function triggered by a PROPOSE msg
            // @param propose     the received propose message
            // @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
            //                    list that can be modified here or at once when all the messages are received
            // 【每收到一个 PROPOSE 就触发】这里只做记录，实际"接受/拒绝"在 handleAllResponses 里决定
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("Agent %s proposes %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            //function triggered by a REFUSE msg
            // 【收到拒绝】
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I received a refuse from " + refuse.getSender().getLocalName());
            }

            //function triggered when all the responses are received (or after the waiting time)
            //@param theirVotes the list of message sent by the voters
            //@param myAnswers the list of answers for each voter
            // 【所有响应收齐或超时后触发】这里做票数计算和结果广播
            @Override
            protected void handleAllResponses(List<ACLMessage> theirVotes, List<ACLMessage> myAnswers) {
                ArrayList<ACLMessage> listeVotes = new ArrayList<>(theirVotes);
                //we keep only the proposals only
                // 【只保留 PROPOSE 消息】过滤掉 REFUSE 等
                listeVotes.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);

                List<ACLMessage> answers = new ArrayList<>();

                for (ACLMessage vote : listeVotes) {
                    //by default, we build a accept answer for each vote
                    // 【为每个投票构造 ACCEPT_PROPOSAL】这里把"招标"退化成"投票"——所有出价都接受
                    var answer = vote.createReply();
                    answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    answers.add(answer);
                    var content = vote.getContent();
                    //read the content resto1>resto2>,...
                    // 【解析偏好排序】按 > 切分
                    String[] itsVotes = content.split(">");
                    // 【Borda 计分】第 1 名 = 候选项总数分，第 2 名 = 总数-1，...，最后一项 = 1 分
                    int[] points = {itsVotes.length};
                    for (String s : itsVotes) {
                        //We add the value of the vote of each restaurant in the map of votes
                        // 【累加分数】computeIfPresent 只在 key 存在时更新
                        votes.computeIfPresent(s, (k, v) -> v + points[0]);
                        points[0]--;
                    }
                }

                println("-".repeat(40));
                //Viewing total votes
                // 【打印每个选项的总得分】
                votes.forEach((k, v) -> println(k + " obtained " + v + " points"));
                //Recovery of the highest score
                // 【求最高分】
                int highScore = Collections.max(votes.values());
                //Recovery of elected options
                // 【选出所有得分等于最高分的选项】（可能有多个，即平票）
                StringBuffer best = new StringBuffer();
                votes.forEach((k, v) -> {
                    if (v == highScore) best.append(k).append(",");
                });
                println("-".repeat(40));
                println("Result of the vote :  " + best);
                println("-".repeat(40));

                //Adding the names of the elected options in the messages to be returned
                // 【把当选结果填进每条回复】所有参与者都会收到相同的结果消息
                for (ACLMessage m : answers)
                    m.setContent(best.toString());
                myAnswers.addAll(answers);

                //If tied, we relaunch a vote with them
                // 【平票处理】当选选项数 >1 时，用 WakerBehaviour 延迟 100ms 递归发起新投票
                if ((best.toString()).split(",").length > 1) {
                    println("-".repeat(30));
                    println("A new round will be launched these choices : " + best);
                    println("-".repeat(30));
                    myAgent.addBehaviour(new WakerBehaviour(myAgent, 100) {
                        @Override
                        protected void onWake() {
                            // 【递归调用 createVote】只在平票选项之间重新投票
                            createVote("voteNo1", best.toString());
                        }
                    });
                }
            }

            //function triggered by a INFORM msg : a voter accept the result
            // @Override
            // 【收到参与者确认】参与者收到 ACCEPT_PROPOSAL 后回一条 INFORM
            protected void handleInform(ACLMessage inform) {
                println("the vote is accepted by " + inform.getSender().getLocalName());
            }


        };

        addBehaviour(init);

    }

    // 【按钮事件】触发投票
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    // 【生成候选列表】把所有餐厅名拼接成 "Pizzeria,Vegetables,Sushi,..."
    public void launchRequest() {
        StringBuilder sb = new StringBuilder();
        for (Restaurant r : Restaurant.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

}
