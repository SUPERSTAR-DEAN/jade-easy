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
 * 【卖家智能体】"讨价还价"的另一半，由它**主动发起**谈判。
 *
 * 谈判规则（本类实现卖家的部分）：
 *   - 卖方的挂牌价 prixVendeur 从 100 起，每次收到还价后**下调 1%**
 *   - 卖家心里的最低价是 prixMin = 25（低于此价绝不卖）
 *   - 如果买家的出价 >= 我方最新挂牌价 → 接受（ACCEPT_PROPOSAL）
 *   - 轮数用完仍未成交 → 发 CANCEL 收场
 *
 * 【与买家的关键差别】卖家是"发起方"，所以多了一个 WakerBehaviour 先发第一封 PROPOSE；
 * 买家是"应对方"，上来就挂着循环等报价。
 *
 * @author emmanueladam
 */
public class AgentVendeur extends Agent {

    // 【当前挂牌价】每次迭代下调，代表"现在我最少收这么多"
    double prixVendeur;
    // 【底价】卖家的心理下限，本例中只做数据准备，未参与判断逻辑
    double prixMin;
    // 【让步步长】0.01 = 每次降价 1%
    double delta;
    // 【最大回合数】
    int nbEchanges;
    // 【买家地址】
    AID aidAcheteur;

    /**
     * agent setup, adds its behaviours
     *
     * 【setup】参数初始化 + 四个行为：① 定时开谈 ② 谈判主循环 ③ 收 FAILURE ④ 收 CANCEL ⑤ 收 ACCEPT_PROPOSAL
     */
    @Override
    protected void setup() {
        prixVendeur = 100;
        prixMin = 25;
        delta = 0.01;
        nbEchanges = 10;

        println(getLocalName() + " -> Hello, my address is " + getAID());

        // 【延迟 10 秒后发起谈判】给平台留启动时间
        long temps = 10000;
        out.println(getLocalName() + " -> I start in" + temps + " ms");

        addBehaviour(new WakerBehaviour(this, temps) {
            protected void onWake() {
                // 【开第一枪】用 PROPOSE 把挂牌价发给买家
                var msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.addReceiver("acheteur");
                msg.setContent(String.valueOf(prixVendeur));
                // 【会话 ID】"MARCHE"=市场，让双方能把这轮谈判串起来
                msg.setConversationId("MARCHE");
                myAgent.send(msg);
                println(getLocalName() + " -> I launch the negociation");
            }
        });

        // 【只关心谈判会话的还价】
        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("MARCHE"),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        // 【谈判循环】收到还价 → 降价 → 决定接受或继续还价
        addBehaviour(new Behaviour(this) {
            int step = 0;
            boolean nego = true;

            public void action() {
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    var reply = msg.createReply();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(), content, sender.getLocalName()));
                    myAgent.doWait(300);
                    // 【解析买家的出价】
                    double offre = Double.parseDouble(content);
                    // 【挂牌价下调 1%】卖家越谈越"让步"
                    prixVendeur = prixVendeur * (1 - delta);
                    if (offre < prixVendeur) {
                        // 【买家出价太低】继续还价，报一个略低的新价
                        reply.setContent(String.valueOf(prixVendeur));
                    } else {
                        // 【成交】买家出价已达标
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ok pour " + offre + "€ !!");
                        nego = false;
                    }
                    myAgent.send(reply);
                } else block();
            }

            public boolean done() {
                // 【谈崩收场】发 CANCEL 通知买家
                if (step == nbEchanges && nego) {
                    println(getLocalName() + " -> I don't sell anymore");
                    var msg = new ACLMessage(ACLMessage.CANCEL);
                    msg.addReceiver(aidAcheteur);
                    myAgent.send(msg);
                }
                return step == nbEchanges || !nego;
            }
        });

        // 【FAILURE 监听】
        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addBehaviour(new ReceiverBehaviour(this, -1, modele2, true, (a, msg) ->
                println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));

        // 【CANCEL 监听】
        var modele3 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele3, true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " stopped the negociation  : " + msg.getContent())
        ));

        // 【ACCEPT_PROPOSAL 监听】
        var modele4 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele4, true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " accepted the offer  : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    // 【下线】
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}
