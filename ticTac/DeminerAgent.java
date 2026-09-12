package ticTac;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;

/**
 * class for an agent that listen 2 types of messages
 *
 * 【接收者】用两个 CyclicBehaviour 分别监听 "CLOCK" 和 "BOOM" 两种消息。
 *
 * 关键教学点：
 *   - **MessageTemplate.MatchConversationId**：按会话 ID 过滤消息
 *   - **两个 CyclicBehaviour 独立运行**：JADE 调度器会交替调用它们的 action()
 *   - **block()**：没消息时让出 CPU
 *
 * @author emmanueladam
 */
public class DeminerAgent extends Agent {
    /**
     * Agent set-up
     */
    @Override
    protected void setup() {
        // add a behaviour that reacts to "CLOCK" msgs
        // 【行为 1：监听 CLOCK 消息】
        addBehaviour(new CyclicBehaviour(this) {
            // 【过滤器】只匹配会话 ID = "CLOCK" 的消息
            final MessageTemplate mt = MessageTemplate.MatchConversationId("CLOCK");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(), content, sender.getLocalName()));
                } else block();
            }
        });

        // add a behaviour that reacts to "BOOM" msgs
        // 【行为 2：监听 BOOM 消息】
        addBehaviour(new CyclicBehaviour(this) {
            // 【过滤器】只匹配会话 ID = "BOOM" 的消息
            final MessageTemplate mt = MessageTemplate.MatchConversationId("BOOM");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    // 【特殊处理 BOOM】用 "ATTENTION" 前缀强调这是警报
                    println("ATTENTION :: %s -> I received \"%s\" from '%s'".formatted(getLocalName(), content,
                            sender.getLocalName()));
                } else block();
            }
        });

    }
}
