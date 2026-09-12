package serviceDetection.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Class for an agent who subscribes to the yellow page to receive all the information about a particular service.
 * It disseminates to all the agents of this service the information it receives.
 *
 * 【观察者 / scribe】这是理解"黄页订阅"的关键类。
 *
 * 它做了什么：
 *   1. 订阅黄页里 "traveller/quiet" 服务的变更事件
 *   2. 每当有智能体注册或注销，回调 `onRegister` / `onDeregister`
 *   3. 把"当前群里都有谁"广播给这个服务的全体成员
 *
 * 这是"服务发现 + 事件通知"结合的经典写法。`DFSubscriber` 继承自 Behaviour，
 * 所以要用 `addBehaviour()` 挂载。
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ScribeAgent extends AgentWindowed {
    /** list of traveller agents*/
    // 【群组成员表】记录当前注册在 traveller/quiet 服务下的所有智能体地址
    List<AID> travellers;

    /** no of sent message*/
    // 【计数】本例中未实际使用
    int i = 0;

    /*** Agent set-up*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setBackgroundTextColor(Color.YELLOW);
        detectTravellers();
    }

    /**
     * listen for event about registration/deregistration to the yellow pages about the service traveller-quiet<br>
     * At each new arrival/departure, the list of agents of the group is sent to these agents
     *
     * 【订阅黄页变更事件】核心方法。构造一个 DFSubscriber，注册到平台事件系统。
     */
    private void detectTravellers() {
        // 【构造查询条件】"traveller" 类型下的 "quiet" 子服务
        var model = AgentServicesTools.createAgentDescription("traveller", "quiet");
        travellers = new ArrayList<>();

        //Subscribe to the Yellow Pages service to receive an alert in case of movement on the traveller-quiet service
        // 【挂载订阅行为】DFSubscriber 是一个 Behaviour，需要 addBehaviour
        addBehaviour(new DFSubscriber(this, model) {
            // 【有人注册时回调】
            @Override
            public void onRegister(DFAgentDescription dfd) {
                // 把新成员加入群里
                travellers.add(dfd.getName());
                window.println(dfd.getName().getLocalName() + " has just registered to " + model);
                // 【广播新名单】把当前完整的群组成员列表告诉所有人
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                //transform the list in an array
                // 【List → AID[]】Java 的 toArray 泛型转换写法
                msg.addReceivers(travellers.toArray(AID[]::new));
                // 【只发本地名】流式转换，把完整 AID 简化成短名字
                msg.setContent("group members: " + Arrays.toString(travellers.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

            // 【有人注销时回调】
            @Override
            public void onDeregister(DFAgentDescription dfd) {
                // 从群里移除
                travellers.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " has just deregistered from  " + model);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceivers(travellers.toArray(AID[]::new));
                msg.setContent("group members: " + Arrays.toString(travellers.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

        });
    }

    /**close the window when leaving*/
    // 【takeDown】下线时只关窗口（本智能体自己没注册过任何服务，所以不用 deregister）
    @Override
    public void takeDown() {
        System.err.println("I'm leaving the platform...(" + getLocalName() + ")");
        window.dispose();
    }


}
