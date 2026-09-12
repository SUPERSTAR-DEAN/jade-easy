package protocols.vickrey.agents;

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
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 *
 * 【Vickrey 拍卖的参与者】与密封英式拍卖的 ParticipantAgent 结构相同，
 * 差别在注册的服务名 "auction/participant" 和会话 ID "echereNo1"——
 * 因此与英式拍卖可以**同时运行**。
 *
 * 【关键差异提示】虽然参与者逻辑完全相同（随机出价），
 * Vickrey 拍卖**中标者付的钱不同**——付第二高价而非自己的出价。
 * 详见 [AuctioneerAgent](AuctioneerAgent.java)。
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ParticipantAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     *
     * 【setup】注册服务 + 挂 ContractNetResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        // 【主注册】auction/participant
        AgentServicesTools.register(this, "auction", "participant");
        Random hasard = new Random();

        // 【额外的"影子"服务】auction/participantS
        //   这个注册看起来没被用，是历史遗留，可安全忽略
        AgentServicesTools.register(this, "auction", "participantS");

        // 【消息模板】会话 ID = "echereNo1"（法语 échère 拼写），与 AuctioneerAgent 匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("echereNo1");

        // 【挂载协议响应行为】
        ContractNetResponder encherissement = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            // 【收到 CFP】
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                window.setBackgroundTextColor(Color.WHITE);
                ACLMessage reponse = cfp.createReply();
                println("'%s' proposes  '%s' to the auction..".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                // 【随机出价 0-99】
                int offre = hasard.nextInt(0, 100);
                //ici l'agent refuse 1 fois sur 3 (lorsque la valeur aleatoire offre est < a 33)
                // 【33% 概率放弃参拍】
                if (offre < 33) {
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I don't want to bid for this object.");
                    reponse.setPerformative(ACLMessage.REFUSE);
                } else {
                    println(String.format("I propose %d to buy '%s' to agent : '%s'", offre, cfp.getContent(),
                            cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offre));
                }
                println("-".repeat(30));
                return reponse;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            // 【收到 ACCEPT_PROPOSAL】中标，回 INFORM 确认
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                window.setBackgroundTextColor(Color.YELLOW);
                println("-".repeat(30));
                println("BIDDING ACCEPTED, as a reminder : ");
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

            /**prise en compte du refus*/
            // 【收到 REJECT_PROPOSAL】落选
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                window.setBackgroundTextColor(Color.RED);
                println("-".repeat(30));
                println("BIDDING REJECTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has declined with this message '%s'".formatted(cfp.getSender().getLocalName(), reject.getContent()));
                println("_".repeat(40));
                println("");

            }


        };

        addBehaviour(encherissement);

    }



    @Override
    public void takeDown() {
        //on se desinscrit du service des encheres avant de partir
        // 【下线前注销所有服务】
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }

}
