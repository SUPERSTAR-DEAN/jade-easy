package protocols.voteCondorcet.agents;


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
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 *
 * 【Condorcet 投票的投票者】Contract-Net 响应方，随机给出候选项排序。
 *
 * 与 bordaCount 的 ParticipantAgent 几乎相同——都是"收到 CFP → 随机排序 → PROPOSE"。
 * 差别在**发起方 PollingStationAgent 用不同算法算分**：
 *   - Borda：按排名累加分（第 1 名 n 分，第 2 名 n-1 分）
 *   - Condorcet：计算两两对决胜场数
 *
 * @author eadam
 */
public class VoterAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     *
     * 【setup】注册服务 + 挂 ContractNetResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        // 【注册为 vote/participant】
        AgentServicesTools.register(this, "vote", "participant");

        // 【消息模板】会话 ID = "voteNo1"，与 PollingStationAgent 匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        // 【挂载协议响应行为】
        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            // 【收到 CFP】准备投票
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                println("~".repeat(40));
                println(cfp.getSender().getLocalName() + " propose les choix : " + cfp.getContent());
                ACLMessage reponse = cfp.createReply();
                reponse.setPerformative(ACLMessage.PROPOSE);
                // 【随机排序】
                String choix = faireSonChoix(cfp.getContent());
                reponse.setContent(choix);
                return reponse;
            }

            /**le participant recoit une liste de propositions sous la forme option1,option2,option3,option4,.....
             * il retourne son choix en ordonnant les options et en donnant leurs positions
             * @param offres liste de propositions sous la forme option1,option2,option3,option4
             * @return choix ordonne sous la forme option2>option4>option3>option1
             * *
             * 【生成偏好排序】"a,b,c" → "b>c>a"（用 > 分隔以便 PollingStationAgent 用 split(">") 计算胜场）
             */
            private String faireSonChoix(String offres) {
                ArrayList<String> choix = new ArrayList<>(List.of(offres.split(",")));
                Collections.shuffle(choix);
                StringBuilder sb = new StringBuilder();
                String pref = ">";
                for (String s : choix) sb.append(s).append(pref);
                String proposition  = sb.substring(0, sb.length()-1);
                println("je propose ceci " + proposition);
                return proposition;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            // 【收到 ACCEPT_PROPOSAL】投票结束，回 INFORM 确认
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("~".repeat(30));
                println("Fin du vote..");
                println(cfp.getSender().getLocalName() + " avait lance un vote pour " + cfp.getContent());
                println(" j'ai propose " + propose.getContent());
                println(cfp.getSender().getLocalName() + " m'informe que l'option elue est " + accept.getContent());
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            /**prise en compte du refus*/
            // 【收到 REJECT_PROPOSAL】被拒
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                println("=".repeat(10));
                println("VOTE REJETE");
                println(cfp.getSender().getLocalName() + " avait lance un vote pour " + cfp.getContent());
                println(" j'ai propos " + propose.getContent());
                println(cfp.getSender().getLocalName() + " a refuse ! avec ce message " + reject.getContent());
            }
        };

        addBehaviour(comportementVote);

    }

    // 【下线前注销服务】
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
        window.dispose();
    }

}
