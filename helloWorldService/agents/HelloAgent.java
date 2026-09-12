package helloWorldService.agents;

import helloWorldService.gui.SimpleGui4Agent;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * class for an agent that registers to a service and that messages to agents according to the service they belong
 *
 * 【黄页 + 按服务群发的智能体】这是学习"服务发现"的关键例子。
 *
 * 三个核心概念，本类全部涉及：
 *   1. **黄页服务（DF）**：智能体把自己注册成某类服务（这里分"主服务/子服务"两级）
 *   2. **服务查询**：想发消息时，先问黄页"谁注册了这个服务"，拿到地址列表
 *   3. **群发**：把消息发给查询到的所有人
 *
 * 继承 GuiAgent 而不是 Agent，因为这类智能体自带一个窗口，能响应 GUI 事件。
 *
 * @author eadam
 */
public class HelloAgent extends GuiAgent {

    /**
     * little gui to display and send messages
     */
    // 【智能体自己的窗口】用来显示收到的消息、输入要发的消息
    SimpleGui4Agent window;

    /**
     * address (aid) of the other agents
     */
    // 【邻居地址】从黄页查回来的智能体地址数组
    AID[] neighbourgs;

    /**
     * msg to send
     */
    // 【要发的消息内容】来自创建参数，默认 "Hello"
    String helloMsg;

    /**
     * agent set-up.
     * - Register the agent in the yellow pages for a service chosen between cordiality-lobby or cordiality-reception.
     * - Add a behavior that listens and displays the received message
     *
     * 【setup】做三件事：建窗口 → 随机注册到某个子服务 → 挂一个"收消息就显示"的行为
     */
    @Override
    protected void setup() {
        // 【读创建参数】创建时传的 hello / goodbye 等，没传就用 Hello
        String[] args = (String[]) this.getArguments();
        helloMsg = ((args != null && args.length > 0) ? args[0] : "Hello");
        window = new SimpleGui4Agent(this);
        window.println(helloMsg, false);
        //choose to register to lobby or reception-desk
        // 【随机选一边注册】这样平台上的智能体会自然分成两组，便于演示"按组群发"
        if (Math.random() < 0.5) {
            //Register as a lobby agent in the cordiality service
            // 【两级服务注册】"cordiality" 是服务类型，"lobby" 是它下面的子服务
            AgentServicesTools.register(this, "cordiality", "lobby");
            window.mainTextArea.setBackground(Color.pink);
            window.println("I'm registered in the service 'lobby' inside the 'cordiality' type of service");
        } else {
            //Register as a reception agent in the cordiality service
            AgentServicesTools.register(this, "cordiality", "receptiondesk");
            window.mainTextArea.setBackground(Color.lightGray);
            window.println("I'm registered in the service 'reception desk' inside the 'cordiality' type of service");
        }

        // 【持续监听所有消息】ReceiverBehaviour(this, -1=不限时, null=不筛类型, true=阻塞, 回调)
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a, msg) -> {
            window.println("I've received a message from " + msg.getSender().getLocalName(),
                    true);
            window.println("  Here is the content: \"" + msg.getContent() + "\"", true);
            window.println("-".repeat(30));
        }));
    }

    /**
     * Send a message to the agents registered under a given service
     * @param text text to send
     * @param nameService Name of the service to which the recipients of the message belong
     *
     * 【向某服务下的所有智能体群发】这是本类的核心方法，流程是"先查后发"
     */
    private void sendMessage(String text, String nameService) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(text);
        // 【问黄页】查出所有注册在 cordiality / nameService 下的智能体地址
        neighbourgs = AgentServicesTools.searchAgents(this, "cordiality", nameService);
        msg.addReceivers(neighbourgs);   // 把查到的地址全塞进收件人列表
        send(msg);                        // 一次群发
        window.println("-> \"" + text + "\" sent to agents of the service '" + nameService + "'");
    }


    /**
     * Reaction to the event transmitted by the window
     *
     * 【窗口事件回调】用户在窗口里点按钮，事件被转成这里处理。
     * GuiAgent 会自动调用本方法。
     *
     * @param ev evenement
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            // 用户点了"发给 lobby 组"
            case SimpleGui4Agent.SENDLOBBY -> sendMessage(window.lowTextArea.getText(), "lobby");
            // 用户点了"发给 receptiondesk 组"
            case SimpleGui4Agent.SENDRECEPTIONDESK -> sendMessage(window.lowTextArea.getText(), "receptiondesk");
            // 用户点了退出
            case SimpleGui4Agent.QUITCODE -> doDelete();
        }
    }

    /**deregister to the service and close the window before leaving
     * *
     * 【takeDown】离开前做两件必要的事：从黄页注销 + 关窗口。
     * 不注销的话，黄页里会留着一个已死的地址，别人查出来照样发不过去。
     */
    @Override
    protected void takeDown() {
        // S'effacer du service pages jaunes
        // 【从黄页注销】把所有注册过的服务全部解除
        AgentServicesTools.deregisterAll(this);
        //fermer la fenetre
        // 【关窗口】不关的话窗口会一直留在屏幕上
        window.dispose();
        //bye
        System.err.println("Agent : " + getAID().getName() + " quitte la plateforme.");
    }

}
