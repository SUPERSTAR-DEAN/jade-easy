package protocols.voteDoubleBorda.agents;


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
 * 【双重 Borda 投票的投票者】结构与 bordaCount 的 ParticipantAgent 完全相同——
 * 收到 CFP 就随机给出排序，回 PROPOSE。
 *
 * 差别在发起方 [PollingStationAgent](PollingStationAgent.java)：
 *   - 普通 Borda：平票时**重新发起完整投票**
 *   - 双重 Borda：平票时**只在平票选项之间**再做一次 Borda
 *
 * @author eadam
 */
public class ParticipantAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     *
     * 【setup】注册服务 + 挂 ContractNetResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        // 【注册为 vote/participant 服务】
        AgentServicesTools.register(this, "vote", "participant");

        // 【消息模板】会话 ID = "voteNo1"，与 PollingStationAgent 匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        // 【挂载协议响应行为】
        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            // 【收到 CFP】
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                println("~".repeat(40));
                println(cfp.getSender().getLocalName() + " propose these preferences : " + cfp.getContent());
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
             * @return choix ordonne sous la forme option2_1,option4_2,option3_3,option1_4
             * *
             * 【生成偏好排序】把候选项随机打乱，用 > 连接
             */
            private String faireSonChoix(String offres) {
                ArrayList<String> choix = new ArrayList<>(List.of(offres.split(",")));
                Collections.shuffle(choix);
                StringBuilder sb = new StringBuilder();
                String pref = ">";
                for (String s : choix) sb.append(s).append(pref);
                String proposition  = sb.substring(0, sb.length()-1);
                println("I proposed this " + proposition);
                return proposition;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            // 【收到 ACCEPT_PROPOSAL】投票结束，回 INFORM 确认
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("=".repeat(10));
                println(cfp.getSender().getLocalName() + " launched a vote among " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                println(cfp.getSender().getLocalName() + " accepted with this message " + accept.getContent());
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
                println(cfp.getSender().getLocalName() + " launched a vote among " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                println(cfp.getSender().getLocalName() + " accepted with this message " + reject.getContent());
            }


        };

        addBehaviour(comportementVote);

    }

    // 【下线前注销服务】
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println("I, " + this.getLocalName() + ", I leave the platform...");
    }

}
