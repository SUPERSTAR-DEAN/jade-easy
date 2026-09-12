package pingPong;

import jade.core.Runtime;
import jade.core.*;
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
 * 【乒乓对打】两个同类的智能体互相发消息，模拟"发球→回球"来回 20 次。
 *
 * 这是理解 JADE 通信的最佳入门例子，务必吃透以下四个概念：
 *   1. **ACLMessage**：JADE 的消息单元，有"行为(performative)"、内容、收件人、会话 ID
 *   2. **MessageTemplate**：收消息时的过滤器，用来只关心符合条件的消息
 *   3. **Behaviour**：智能体的"可执行单元"，用 action()/done() 描述"做什么"和"何时结束"
 *   4. **block()**：告诉框架"我没活干了，让我挂起，等有新消息再来叫我"——避免空转烧 CPU
 *
 * @author emmanueladam
 */
public class AgentPingPong extends Agent {

    /**
     * agent setup, adds its behaviours
     *
     * 【setup】挂两个行为：① ping 定时发第一球；② 收到 SPORT 会话的 INFORM 就回一个
     */
    @Override
    protected void setup() {

        println(getLocalName() + " -> Hello, my address is " + getAID());
        // if the agent names "ping"
        // 【只有 ping 发起】同一个类创建了 ping 和 pong 两个实例，用名字区分职责
        if (getLocalName().equals("ping")) {
            long temps = 10000;
            out.println(getLocalName() + " -> I start in" + temps + " ms");
            // 【WakerBehaviour】延迟 10 秒后执行一次 onWake()，相当于"10 秒后发球"
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("pong");
                    msg.setContent("ball");
                    // 【会话 ID】把多条相关消息串成一次"对话"，方便之后用模板匹配
                    msg.setConversationId("SPORT");
                    myAgent.send(msg);
                    println(getLocalName() + " -> I launch the ball");
                }
            });
        }

        // 【组合过滤器】and() 把多个条件组合：会话 ID 是 SPORT 且行为是 INFORM
        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("SPORT"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        // add a behavior, with 20 iterations, that wait for a 'INFORM' msg about 'SPORT' and replies to it after 300ms
        // 【对打行为】自定义 Behaviour，最多回合 20 次
        addBehaviour(new Behaviour(this) {
            // 【回合计数】用成员变量记住已经来回了几次
            int step = 0;

            // 【做什么】每次被唤醒执行一次：收一条 → 处理 → 回一条
            public void action() {
                // 【阻塞式接收】没有匹配的消息就挂起，有消息立刻返回
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(), content, sender.getLocalName()));
                    // 【doWait】忙等一小段（毫秒），模拟"思考/处理"耗时
                    myAgent.doWait(300);
                    // 【createReply】自动填好收件人=原发送者、会话 ID=原会话，省去手动设置
                    var reply = msg.createReply();
                    reply.setContent("ball-" + step);
                    myAgent.send(reply);
                } else block();
                // ↑ 【block()】没收到消息时调用，让出 CPU，等新消息到达再唤醒。
                //   注意：这里 action() 返回后 done() 会被检查，所以 block() 放在分支外更稳妥。
            }

            // 【何时结束】回合数到 20 就结束这个行为
            public boolean done() {
                if (step == 20)
                    println(getLocalName() + " -> I don't play anymore");
                return step == 20;
            }
        });

        // 【错误监听】单独挂一个行为专门收 FAILURE，这是健壮程序的标配
        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addBehaviour(new ReceiverBehaviour(this, -1, modele2, true, (a, msg) ->
            println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    // 【下线】离开平台前打印告别语
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

    public static void main(String[] args) {
        // 【程序化启动平台】用 Properties 代替命令行参数，便于在代码里动态配置
        Properties prop = new ExtendedProperties();
        // display a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // declare the agents
        // 【两个同类实例】ping 和 pong 用的是同一个类，靠名字区分角色
        prop.setProperty(Profile.AGENTS, "ping:pingPong.AgentPingPong;pong:pingPong.AgentPingPong");
        // create the main container
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch it !
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
