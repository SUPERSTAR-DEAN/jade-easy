package issia23.behaviours;

import issia23.agents.RepairCoffeeAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 【维修咖啡机响应用户的行为】Contract-Net 响应方，由 RepairCoffeeAgent 挂载。
 *
 * 流程：
 *   - 收到用户 CFP → handleCfp()
 *   - 随机掷骰子决定接不接（hasard 在 -4 到 3 之间，<=0 就 REFUSE，>0 就 PROPOSE）
 *   - 报价内容 = 这个随机数（模拟"维修价格"）
 *   - 收到 ACCEPT_PROPOSAL → handleAcceptProposal() 回 INFORM 确认
 *   - 收到 REJECT_PROPOSAL → handleRejectProposal() 打印
 *
 * 注意：当前实现是"纯随机决定"，真实场景应该基于"是否有所需零件"来决定
 */
public class CafeRepondreUtilisateur extends ContractNetResponder {

    RepairCoffeeAgent monAgent;

    // 【构造】传入消息模板（只处理会话 ID = "id" 的 CFP）
    public CafeRepondreUtilisateur(RepairCoffeeAgent a, MessageTemplate model) {
        super(a, model);
        monAgent = a;
    }

    //function triggered by a PROPOSE msg : send back the ranking
    // 【收到 CFP】决定接单还是拒单
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        monAgent.println("~".repeat(40));
        // 【掷骰子】-4 到 3 的随机数，模拟"是否有零件 + 维修成本"
        int hasard = (int) (Math.random() * 8) - 4;
        monAgent.println(cfp.getSender().getLocalName() + " proposes this options: " + cfp.getContent());
        ACLMessage answer = cfp.createReply();
        // 【hasard <= 0 就拒】——大约 5/8 = 62.5% 概率拒绝
        if (hasard <= 0) answer.setPerformative(ACLMessage.REFUSE);
        else answer.setPerformative(ACLMessage.PROPOSE);

//                String choice = makeItsChoice(cfp.getContent());
        // 【报价内容 = 这个随机数】注意：即使是 REFUSE 也会设置 content
        answer.setContent(String.valueOf(hasard));
        return answer;
    }

    /**proposals in the form option1,option2,option3,option4,.....
     * * he returns his choice by ordering the options and giving their positions
     * @param offres list of proposals in the form of option1,option2,option3,option4
     * @return orderly choice in the form of option2_1,option4_2,option3_3,option1_4
     * *
     * 【未使用的方法】从投票模块复制过来的遗留代码
     */
    private String makeItsChoice(String offres) {
        ArrayList<String> choice = new ArrayList<>(List.of(offres.split(",")));
        Collections.shuffle(choice);
        StringBuilder sb = new StringBuilder();
        String pref = ">";
        for (String s : choice) sb.append(s).append(pref);
        String proposition = sb.substring(0, sb.length() - 1);
        monAgent.println("I propose this ranking: " + proposition);
        return proposition;
    }

    //function triggered by a ACCEPT_PROPOSAL msg : the polling station agent  accept the vote
    //@param cfp : the initial cfp message
    //@param propose : the proposal I sent
    //@param accept : the acceptation sent by the auctioneer
    // 【收到 ACCEPT_PROPOSAL】中标，回 INFORM 确认
    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        monAgent.println("=".repeat(15));
        monAgent.println(" I proposed " + propose.getContent());
        monAgent.println(cfp.getSender().getLocalName() + " accepted my poposam and sent the result:  " + accept.getContent());
        ACLMessage msg = accept.createReply();
        msg.setPerformative(ACLMessage.INFORM);
        msg.setContent("ok !");
        return msg;
    }

    //function triggered by a REJECT_PROPOSAL msg : the auctioneer rejected my vote !
    //@param cfp : the initial cfp message
    //@param propose : the proposal I sent
    //@param accept : the reject sent by the auctioneer
    // 【收到 REJECT_PROPOSAL】落选
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        monAgent.println("=".repeat(10));
        monAgent.println("PROPOSAL REJECTED");
        monAgent.println(cfp.getSender().getLocalName() + " asked to repair elt no " + cfp.getContent());
        monAgent.println(" I proposed " + propose.getContent());
        monAgent.println(cfp.getSender().getLocalName() + " refused ! with this message: " + reject.getContent());
    }


};
