package pingPong.negociation;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * 【买家智能体】这是"讨价还价"的双智能体例子之一。
 *
 * 谈判规则（本类实现买家的部分）：
 *   - 买方的心理底价 prixAcheteur 从 50 起，每次收到报价后**上调 1%**
 *   - 如果卖家的报价 <= 我方的心理价 → 接受（ACCEPT_PROPOSAL）
 *   - 如果轮数用完还没谈成 → 发 CANCEL 主动终止谈判
 *
 * 与 [AgentVendeur](AgentVendeur.java) 对照读效果最好：
 *   卖家从 100 往下压，买家从 50 往上涨，两条线在中间某处相遇。
 *
 * @author emmanueladam
 */
public class AgentAcheteur extends Agent {

    // 【当前心理价】每次谈判迭代会往上抬，代表"我最多愿意出这么多"
    double prixAcheteur;
    // 【预算上限】硬约束，超过就不谈了（本例未显式用到）
    double prixMax;
    // 【让步步长】每次迭代调价的比例，0.01 = 1%
    double delta;
    // 【最大回合数】防止无限拉扯
    int nbEchanges;
    // 【卖家地址】从收到的消息里学来的，用于回信和发 CANCEL
    AID aidVendeur;
    /**
     * agent setup, adds its behaviours
     *
     * 【setup】初始参数 + 挂四个行为：① 谈判主循环 ② 收 FAILURE ③ 收 CANCEL ④ 收 ACCEPT_PROPOSAL
     */
    @Override
    protected void setup() {
        prixAcheteur = 50;
        prixMax = 200;
        delta = 0.01;
        nbEchanges = 20;

        println(getLocalName() + " -> Hello, my address is " + getAID());

        // 【只关心谈判会话的报价】会话 ID "MARCHE" + 行为 PROPOSE
        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("MARCHE"),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));

        // 【谈判循环】收到报价 → 抬高心理价 → 决定接受或还价
        addBehaviour(new Behaviour(this) {
            int step = 0;
            // 【是否还在谈】一旦达成接受，置 false 以结束循环
            boolean nego = true;

            @Override
            public void action() {
                // 【阻塞接收】没消息就挂起，有消息才推进
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    // 【记住对方地址】第一次收到消息时才知道卖家是谁
                    aidVendeur = msg.getSender();
                    var reply = msg.createReply();
                    double offre = Double.parseDouble(content);
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(), content, aidVendeur.getLocalName()));
                    myAgent.doWait(300);
                    // 【心理价上调 1%】买家越谈越"松口"，让步幅度固定
                    prixAcheteur = prixAcheteur * (1 + delta);
                    if (offre > prixAcheteur) {
                        // 【还价】对方报价高于我能接受的，就报一个更低的数字回去
                        reply.setContent(String.valueOf(prixAcheteur));
                    } else {
                        // 【成交】对方报价已进入我方可接受区间
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ok pour " + offre + "€ !!");
                        nego = false;
                    }
                    myAgent.send(reply);
                } else block();
            }

            @Override
            public boolean done() {
                // 【谈崩收场】轮数用尽且仍未成交，主动发 CANCEL 通知对方
                if (step == nbEchanges && nego) {
                    println(getLocalName() + " -> I don't buy anymore");
                    var msg = new ACLMessage(ACLMessage.CANCEL);
                    msg.addReceiver(aidVendeur);
                    myAgent.send(msg);
                }
                return step == nbEchanges;
            }

        });

        // 【FAILURE 监听】地址无效或平台报错时通知自己
        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addBehaviour(new ReceiverBehaviour(this, -1, modele2, true, (a, msg) ->
                println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));

        // 【CANCEL 监听】对方放弃谈判
        var modele3 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele3, true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " stopped the negociation  : " + msg.getContent())
        ));

        // 【ACCEPT_PROPOSAL 监听】对方确认成交
        var modele4 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele4, true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " accepted the offer  : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    // 【下线】离开平台时打印告别语
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}
