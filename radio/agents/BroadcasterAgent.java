package radio.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;


/**
 * agents linked to a window, sends a radio message on a channel
 *
 * 【广播者智能体】发送广播消息到指定频道。
 *
 * 【JADE 广播机制】：
 *   - `AgentServicesTools.generateTopicAID(this, "频道名")` 生成一个 topic 的 AID
 *   - `msg.addReceiver(topic)` 把消息发到该 topic，所有订阅该 topic 的智能体都能收到
 *   - **注意**：generateTopicAID 不是"订阅"，只是拿一个频道 ID 用来发；
 *     接收方需要显式订阅（见 [ListenerAgent](ListenerAgent.java)）
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class BroadcasterAgent extends AgentWindowed {
    /**
     * address of the radio topic
     */
    // 【频道地址】通过名字生成的 topic AID
    AID topic;
    /**
     * no of the sent msg
     */
    // 【消息计数】每发一条 +1，用于生成不同的内容
    int i = 0;

    // 【setup】创建频道并初始化
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.YELLOW);
        //Create a "radio channel" with the name 'BestAgentsCharts'
        // 【生成频道】任何使用同名 topic 的智能体（发送或订阅）都会指向同一个频道
        topic = AgentServicesTools.generateTopicAID(this, "BestAgentsCharts");
    }

    /**
     * reaction to a gui event
     *
     * 【按钮事件】点击"OK"按钮时发送广播
     */
    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            sendMessages();
        }
    }

    /**
     * send messages on the "radio channel"
     *
     * 【发送广播消息】
     */
    private void sendMessages() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        // 【收件人 = topic】所有订阅者都能收到
        msg.addReceiver(topic);
        msg.setContent("hello " + i);
        i++;
        send(msg);
    }


    //fermer la fenetre lorsque l'agent est arrete
    // 【takeDown】下线时关窗口
    protected void takeDown() {
        window.dispose();
    }

}
