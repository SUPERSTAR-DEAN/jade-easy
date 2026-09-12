package serviceDetection.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

/**
 * agent that register to a service in "some" time and display the message received
 *
 * 【随机注册者】这个智能体会**在随机的某个时刻**注册到 "traveller/quiet" 服务。
 *
 * 设计意图：多个智能体各自在 10~20 秒内随机注册，模拟"群体陆续加入"的动态场景，
 * 让 [ScribeAgent](ScribeAgent.java) 有东西可观察。
 *
 * @author eadam
 */
public class IncomingAgent extends AgentWindowed {
    AID topic = null;


    // 【setup】继承 AgentWindowed，可直接用字段 `window`
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getAID().getName() + " is ready. ");

        //regisration to any service
        // 【先随便注册一个占位服务】让"任意类型/任意名称"这个调用形式被看到
        AgentServicesTools.register(this, "anyTypeOfService", "anyNameOfService");

        //regisration to the service
        // 【随机延迟注册】延迟 10000 + random*10000 毫秒（10~20 秒）后注册到真正的目标服务
        //   用 WakerBehaviour 做"稍后执行"是最简洁的写法
        addBehaviour(new WakerBehaviour(this, (long) (Math.random() * 10000d) + 10000,
                a -> AgentServicesTools.register(a, "traveller", "quiet")));

        //Wait limitless(-1) for all message types(null), cyclically (true)
        // 【无限监听所有消息】把收到的每条消息打印出来
        addBehaviour(new ReceiverBehaviour(this, -1, null, true,
                (a, msg) -> println("received ftom \"" + msg.getSender().getLocalName() + "\", ceci : \n" + msg.getContent() +
                        "\n" + "-".repeat(20))));
    }



    /**deregister from all services when leaving*/
    // 【takeDown】下线前必须做两件事：从黄页注销所有服务 + 关窗口
    @Override
    public void takeDown() {
        // 【注销所有注册过的服务】否则黄页里会留着一个死地址
        AgentServicesTools.deregisterAll(this);
        System.err.println("I'm leaving the platform...(" + getLocalName() + ")");
        // 【关窗口】不关的话窗口会留在屏幕上
        window.dispose();
    }


}
