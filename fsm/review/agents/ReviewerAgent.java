package fsm.review.agents;


import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.Random;


/**
 * class for a reviewer agent that wait for article to evaluate and send an evaluation
 *
 * 【审稿人智能体】最简单的一个：
 *   1. 把自己注册为 "journal/reviewer" 服务
 *   2. 监听收到的稿件
 *   3. 用户点击窗口按钮时，随机给一个 0/1/2 的评分回给期刊
 *
 * 这个"人肉审稿"是教学用的——真实系统里这里应该是自动评分或真人评审。
 *
 * @author eadam
 */
public class ReviewerAgent extends AgentWindowed {

    // 【最近收到的稿件】暂存，等用户点击按钮时再回复
    ACLMessage message;
    // 【随机数源】用来生成 0~2 的评分
    Random random;

    /**
     * a reviewer agent declares itself to the yellow pages (DFAgent) and adds a cyclic listening behaviour
     *
     * 【setup】注册到黄页 + 挂监听行为
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
        random = new Random();
        //Yellow Pages registration as a journal reviewer
        // 【注册为审稿人】之后期刊用 searchAgents("journal", "reviewer") 就能查到它
        AgentServicesTools.register(this, "journal", "reviewer");
        var dfd = AgentServicesTools.getAgentDescription(this, "journal", "reviewer");
        //add a behaviour that wait for messages
        // 【监听行为】收到任意消息就存下来并点亮按钮
        Behaviour bReception = new ReceiverBehaviour(this, -1, null, true, (a, m) -> {
            message = m;
            println("---> I received a new content to evaluate: \"" + message.getContent() + "\"");
            println("click to send a random evaluation... ");
            window.setButtonActivated(true);
        });
        addBehaviour(bReception);
        window.setButtonActivated(false);
    }

    /**
     * A click on the button triggers this function which sends a random mark back for article received
     *
     * 【按钮事件】随机给一个 0/1/2 的评分，用 createReply 发回去
     */
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        if (message != null) {
            // 【createReply】自动带上原会话 ID，期刊端就能按 key 匹配到
            var reply = message.createReply();
            // 【nextInt(0, 3) 生成 0~2 的整数】边界值：起点包含，终点不包含
            reply.setContent(Integer.toString(random.nextInt(0, 3)));
            println("I send this evaluation %s with the key %s ".formatted(reply.getContent(), reply.getConversationId()));
            println("-".repeat(40));
            send(reply);
            window.setButtonActivated(false);
            message = null;   // 【清空缓存】避免重复回复同一条稿件
        }
    }

    /**deregister to all the services when leaving the platform*/
    // 【takeDown】下线前从黄页注销，避免留下死地址
    @Override
    protected void takeDown() {
        AgentServicesTools.deregisterAll(this);
    }

}
