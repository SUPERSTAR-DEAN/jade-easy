package ollama;


import jade.core.*;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * 【天气智能体】查询真实天气并响应其他智能体的天气询问。
 *
 * 工作流：
 *   1. setup 启动后 10ms 查询 Belém（贝伦，巴西城市）的天气
 *   2. 监听会话 ID = "METEO" 的 REQUEST 消息
 *   3. 收到询问就回一条 INFORM，内容是当前天气描述
 *
 * 关键点：
 *   - 用 [Meteo](Meteo.java) 类调用真实天气 API
 *   - 把数值温度转成"很冷/冷/温和/热/很热/极热"等自然语言
 *
 * @author emmanueladam
 */
public class AgentMeteo extends AgentWindowed {

    // 【当前天气描述】初值是占位符，启动后会被真实数据替换
    String laMeteo = "très chaud, 29 degrés C";
    /**
     * agent setup, adds its behaviours
     *
     * 【setup】查询天气 + 挂监听行为
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(this);

        println(getLocalName() + " -> Hello, my address is " + getAID());
        // if the agent names "ping"
        // add a behaviour that will send the first "ball" msg in 10 sec. to "pong" agent
        long temps = 10;
        out.println(getLocalName() + " -> I start in" + temps + " ms");
        // 【10ms 后查天气】WakerBehaviour 延迟一次触发
        addBehaviour(new WakerBehaviour(this, temps) {
            protected void onWake() {
                // 【查 Belém 的真实天气】
                laMeteo = getNatureTemperature("Belém");
                println("I have this information about the weather : " + laMeteo);
            }
        });

        // 【消息过滤器】会话 ID = "METEO" 且 performative = REQUEST
        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("METEO"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        // add a behaviour that wait for an eventual failure msg
        // 【监听天气询问】收到 REQUEST 就回 INFORM 带天气描述
        addBehaviour(new ReceiverBehaviour(this, -1, modele, true, (a, msg) -> {
            var reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(laMeteo);
            a.send(reply);
            println(" -> I send a msg to " + msg.getSender().getLocalName() + " with content: " + laMeteo);
        }
        ));
    }


    // 【把温度数值转成自然语言】
     String getNatureTemperature(String town) {
        // 【调用真实天气 API】见 Meteo.java
        Meteo service = new Meteo();
        Meteo.WeatherData weather = service.getWeatherByCity(town);
        if (weather != null && weather.isValid()) {
            double temp = weather.getTemperature();
            // 【按温度区间分类】
            if (temp < 0) {
                return "très froid";
            } else if (temp < 10) {
                return "froid";
            } else if (temp < 17) {
                return "tempéré";
            } else if (temp < 26) {
                return "chaud";
            } else if (temp < 35) {
                return "très chaud";
            } else {
                return "extrêmement chaud";
            }
        } else {
            return "données météo non disponibles";
        }
    }

    /**I inform the user when I leave the platform*/
    // 【takeDown】下线时告别
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}
