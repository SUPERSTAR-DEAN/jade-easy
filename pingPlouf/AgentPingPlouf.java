package pingPlouf;

import jade.core.Runtime;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'tzoing'; the problem being that this agent doesn't exist *
 *
 * 【故意失败的例子】ping 想跟一个**平台上不存在**的智能体 'tzoing' 通信。
 *
 * 这个例子的教学重点：
 *   1. 多智能体平台不会因为"发错地址"而崩溃——这是健壮性的基本要求
 *   2. 失败信息会由 **AMS（白页服务）** 主动回一条 ACLMessage.FAILURE 告诉你
 *   3. 所以真实程序里必须写"接收 FAILURE"的逻辑，否则错误会被静默吞掉
 *
 * 对比阅读：[pingPong/AgentPingPong.java](../pingPong/AgentPingPong.java) 是同一个结构但地址正确的版本
 *
 * @author emmanueladam
 */
public class AgentPingPlouf extends Agent {

    /**
     * agent setup, adds its behaviours
     * *
     * 【setup】给智能体挂载行为。两个行为：一个定时发消息，一个监听失败消息。
     */
    @Override
    protected void setup() {
        println(getLocalName() + " -> Hello, my address is " + getAID());

        // 如果当前智能体叫 "ping"
        // 【条件挂载】只让 ping 负责发起第一次通信，避免两个智能体互相发包
        if (getLocalName().equals("ping")) {
            println(getLocalName() + " -> I send a message to a bad address. ");
            println(getLocalName() + " -> In good multiagent platform, there is no crash..  ");
            println(getLocalName() + " -> I can listen if the AMS agent inform me about an eventual problem.");
            long temps = 15000;
            out.println(getLocalName() + " -> I start in " + temps + " ms");
            // 【WakerBehaviour】延迟一段时间再执行 onWake()，常用于"稍后开始"
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("tzoing"); // 【故意写错的智能体名】平台上没有这个智能体
                    msg.setContent("ball");
                    myAgent.send(msg);
                    println(getLocalName() + " -> I launch the ball to tzoing.");
                }
            });
        }

        // 【监听 FAILURE】用消息模板只匹配"失败"这类消息，收到就打印
        var failureMsgTemplate = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        // ReceiverBehaviour 参数：(智能体, 超时=-1即一直等, 模板, 阻塞, 收到消息后的回调)
        addBehaviour(new ReceiverBehaviour(this, -1, failureMsgTemplate, true, (a, msg) ->
                println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    // 【下线】离开平台时打印一句告别
    @Override
    protected void takeDown() {
        println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    public static void main(String[] args) {
        // 【用程序化方式启动平台】这是 JADE 的另一种启动方式，等价于命令行参数
        Properties prop = new ExtendedProperties();
        // display a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // declare the agents
        // 【注意】这里声明了 ping 和 pong 两个智能体——pong 是"陪衬"，真正要发的目标是 tzoing（不存在）
        prop.setProperty(Profile.AGENTS, "ping:pingPlouf.AgentPingPlouf;pong:pingPlouf.AgentPingPlouf");
        // create the main container
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch it !
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
