package window.agents;


import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

/**
 * agent bound to a window, which waits for messages and displays them
 *
 * 【带窗口的接收者】持续监听收到的消息，并把内容打印到窗口的文本区域。
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ReceiverAgentWithWindow extends AgentWindowed {

    /**
     * agent set-up
     * adds a cyclic message waiting behavior
     *
     * 【setup】建窗口 + 挂一个无限期监听所有消息的行为
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent I'm ready. ");


        // add a receiver behaviour that wait, without time limit (-1), a message with no particular signature (null)
        // , and continuously (true)
        // 【监听所有消息】模板=null 表示不过滤，超时=-1 表示无限期
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a, msg) ->
                println("received " + msg.getContent() + ", from " + msg.getSender().getLocalName())));
    }


}
