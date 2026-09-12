package radio.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.MessageTemplate;

/**
 * agent linked to a window that listens continuously for messages on a topic
 *
 * 【收听者智能体】持续监听某频道的广播消息。
 *
 * 关键 API：
 *   - `MessageTemplate.MatchTopic(topic)` 构造只匹配"发到该 topic 的消息"的模板
 *   - `ReceiverBehaviour(agent, -1, template, true, callback)` 无限期阻塞监听
 *
 * 与广播者配合：**广播者不订阅**，只用 topic 作为收件人；
 * **收听者要订阅**，用 MatchTopic 过滤后接收。
 *
 * @author eadam
 */
public class ListenerAgent extends AgentWindowed {
    // 【频道地址】
    AID topic = null;


    // 【setup】生成频道地址并挂载监听行为
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready. ");
        //Search a "radio channel" with the name 'BestAgentsCharts'
        // 【生成同名频道地址】这样监听的就是广播者发的同一个频道
        topic = AgentServicesTools.generateTopicAID(this, "BestAgentsCharts");
        //cyclic listening on the channel
        // 【构造消息模板】只匹配发到该 topic 的消息
        final MessageTemplate mt = MessageTemplate.MatchTopic(topic);
        // 【挂载监听行为】超时=-1（无限期），阻塞=true，回调里打印收到的消息
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a, msg) -> println("received \"%s\" on the topic channel '%s', sent by %s".
                        formatted(msg.getContent(), topic.getLocalName(), msg.getSender().getLocalName()))));

    }

    //fermer la fenetre lorsque l'agent est arrete
    // 【takeDown】下线时关窗口
    protected void takeDown() {
        window.dispose();
    }


}
