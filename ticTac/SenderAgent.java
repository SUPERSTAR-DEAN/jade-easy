package ticTac;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * class or an agent that send periodically some messages
 *
 * 【发送者】发两种不同类型的消息：
 *   - "CLOCK"：每 1 秒一条（TickerBehaviour 周期性行为）
 *   - "BOOM"：10 秒后一条（WakerBehaviour 延迟行为）
 *
 * 关键教学点：
 *   - **两种消息用不同的 conversationId 区分**——接收方用 MatchConversationId 过滤
 *   - **TickerBehaviour**：周期触发，会一直发
 *   - **WakerBehaviour**：延迟一次触发
 *   - **removeBehaviour()**：可以在运行时移除某个行为（本例 10 秒后停掉时钟）
 *
 * @author emmanueladam
 */
public class SenderAgent extends Agent {
    /**
     * Agent set-up
     */
    @Override
    protected void setup() {

        // creation of a behavior that will send the text 'tictac' every second to the demining agent
        // -- creation of the INFORM message, tagged "CLOCK"
        // 【构造 CLOCK 消息】conversationId = "CLOCK"，收件人 deminer，内容 "tictac"
        final var msgTic = new ACLMessage(ACLMessage.INFORM);
        msgTic.setConversationId("CLOCK");
        msgTic.addReceiver("deminer");
        msgTic.setContent("tictac");
        // -- create a ticker behaviour with a period of 1000ms
        // 【TickerBehaviour：每 1000ms 发一次】
        TickerBehaviour ticTacBehaviour = new TickerBehaviour(SenderAgent.this, 1000, a -> a.send(msgTic));

        // add the ticker behaviour in 5000 ms
        // 【5 秒后开始时钟】
        addBehaviour(new WakerBehaviour(this, 5000, a -> a.addBehaviour(ticTacBehaviour)));

        // creation of a behavior that will send the text 'b o o o m' in 10 secondes
        // -- creation of the INFORM message, tagged "BOOM"
        // 【构造 BOOM 消息】conversationId = "BOOM"
        final var msgBoom = new ACLMessage(ACLMessage.INFORM);
        msgBoom.setConversationId("BOOM");
        msgBoom.addReceiver("deminer");
        msgBoom.setContent("b o o m ! ! !");
        // create and add a WakerBehaviour (with  delay of 10000ms) that remove the TicTac behavior and send the boom
        // msg
        // 【10 秒后：停止时钟 + 发爆炸】
        addBehaviour(new WakerBehaviour(this, 10000, a -> {
            a.removeBehaviour(ticTacBehaviour);   // 【移除时钟行为】
            a.send(msgBoom);                       // 【发爆炸消息】
        }));
    }
}
