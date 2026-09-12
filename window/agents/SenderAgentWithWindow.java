package window.agents;

import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;


/**
 * agent associated with a window, sends a direct message to agents b,c,d when the window informs him that the button
 * has been clicked
 *
 * 【带窗口的发送者】当用户点击窗口按钮时，向 b/c/d 三个智能体发消息。
 *
 * 【GuiAgent 工作流】
 *   1. `setup()` 里创建窗口
 *   2. 用户在窗口上点击 → 产生 `GuiEvent`
 *   3. 框架自动调用 `onGuiEvent(GuiEvent)`，我们在里面写响应逻辑
 *
 * @author emmanueladam
 */
@SuppressWarnings("serial")
public class SenderAgentWithWindow extends AgentWindowed {

    /**
     * agent set-up
     * *
     * 【setup】创建窗口并激活按钮
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        window.setBackgroundTextColor(Color.LIGHT_GRAY);
        window.setButtonActivated(true);   // 【按钮初始为可用状态】
    }

    /**
     * reaction to a gui event
     *
     * 【按钮事件回调】SimpleWindow4Agent.OK_EVENT 就是点击按钮
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case SimpleWindow4Agent.OK_EVENT -> sendMessages();
        }
    }

    /**
     * send a message to agents b, c & d
     *
     * 【群发】一条消息发给三个收件人
     */
    private void sendMessages() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("Hello !");
        // 【addReceivers 可变参数】一次调用传多个收件人
        msg.addReceivers("b", "c", "d");
        send(msg);
    }


}
