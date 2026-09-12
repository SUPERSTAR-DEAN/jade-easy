package issia23.behaviours;

import issia23.agents.UserAgent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.List;

/**
 * 【用户联系维修咖啡机的行为】Contract-Net 发起方，由用户智能体挂载。
 *
 * 流程：
 *   - UserAgent 发出 CFP 后，由本行为接管协议
 *   - 收到维修方的 PROPOSE 报价 → handlePropose()
 *   - 收齐所有报价后 → handleAllResponses() 选最低价中标
 *   - 中标方收到 ACCEPT_PROPOSAL → 回 INFORM 确认 → handleInform()
 *
 * 关键逻辑：**选最低价**——遍历所有 PROPOSE，记录价格最低者，把它改成 ACCEPT_PROPOSAL
 *
 * 注意：本类未处理"全部 REFUSE"的情况——如果所有维修方都拒绝，bestProposal 为 null，会跳过接受步骤
 */
public class ContacterRepairCafe extends ContractNetInitiator {
    UserAgent monAgent;

    // 【构造】调用父类构造把 CFP 消息传进去
    public ContacterRepairCafe(UserAgent a, ACLMessage msg) {
        super(a, msg);
        monAgent = a;
//        reset();
        monAgent.println("I am in the contacter repair cafe behaviour");
    }

    //function triggered by a PROPOSE msg
    // @param propose     the received propose message
    // @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
    //                    list that can be modified here or at once when all the messages are received
    // 【收到一个 PROPOSE】只打印，实际决策在 handleAllResponses 里
    @Override
    public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
        monAgent.println("Agent %s proposes %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
    }

    //function triggered by a REFUSE msg
    // 【收到拒绝】
    @Override
    protected void handleRefuse(ACLMessage refuse) {
        monAgent.println("REFUSE ! I received a refuse from " + refuse.getSender().getLocalName());
    }

    //function triggered when all the responses are received (or after the waiting time)
    //@param theirVotes the list of message sent by the voters
    //@param myAnswers the list of answers for each voter
    // 【所有响应收齐后】这里做"选最低价中标"的决策
    @Override
    protected void handleAllResponses(List<ACLMessage> theirVotes, List<ACLMessage> myAnswers) {
        ArrayList<ACLMessage> listeProposals = new ArrayList<>(theirVotes);
        //we keep only the proposals only
        // 【只保留 PROPOSE】
        listeProposals.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);
        myAnswers.clear();

        ACLMessage bestProposal = null;
        ACLMessage bestAnswer = null;
        // 【最低价初值 = 整数最大值】
        var bestPrice = Integer.MAX_VALUE;

        for (ACLMessage proposal : listeProposals) {
            //by default, we build a accept answer for each proposal
            // 【默认构造 REJECT_PROPOSAL】先给每个报价都准备"拒绝"回复
            var answer = proposal.createReply();
            answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
            myAnswers.add(answer);
            // 【解析报价】内容是字符串数字
            var content = Integer.parseInt(proposal.getContent());
            monAgent.println(proposal.getSender().getLocalName() + " has proposed " + content);
            // 【如果这个报价比当前最低还低，更新】
            if (content < bestPrice) {
                bestPrice = content;
                bestProposal = proposal;
                bestAnswer = answer;
            }

        }

        // 【把最低价者的回复改成 ACCEPT_PROPOSAL】
        if (bestProposal != null) {
            bestProposal.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            monAgent.println("I choose the proposal of " + bestProposal.getSender().getLocalName());
        }

        monAgent.println("-".repeat(40));

    }

    //function triggered by a INFORM msg : a voter accept the result
    // @Override
    // 【中标方回 INFORM】维修方确认维修完成
    protected void handleInform(ACLMessage inform) {
        monAgent.println("the vote is accepted by " + inform.getSender().getLocalName());
    }


};
