package protocols.requests.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * class of an agent that submits a sum request to other agents and handles the exchange through the AchieveRE
 * protocol
 *
 * 【FIPA-Request 协议的发起方】这是 JADE 内置协议的第一课。
 *
 * 传统"发 REQUEST → 自己收 INFORM"的写法很啰嗦（要处理 AGREE/REFUSE/INFORM 多种情况），
 * JADE 的 `AchieveREInitiator` 把这些都封装好了——你只需继承并覆写几个回调。
 *
 * 消息流：
 *   Sender --REQUEST-->  Responder1..N
 *                        |--REFUSE (可选)-->  Sender.handleRefuse()
 *                        |--AGREE  (可选)-->  Sender.handleAgree()
 *                        |--INFORM (收到 AGREE 后必须)-->  Sender.handleInform()
 *                        ...
 *                        [全部收齐]-->  Sender.handleAllResultNotifications()
 *
 * @author eadam
 */
public class AgentRequestSender extends AgentWindowed {

    /**
     * agent set-up
     *
     * 【setup】只是建窗口，真正的协议行为由 GUI 事件触发（见 onGuiEvent）
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add an AchieveRE protocol behavior initiator.
     * send a request message and handle the responses
     * @param id stamp for identify the message
     * @param computation order in the form 3+2+4+5...
     *
     * 【创建协议行为】核心方法。构造一条 REQUEST 消息，然后把它交给 AchieveREInitiator。
     */
    private void createRequest(String id, String computation) {
        // 【构造 REQUEST 消息】
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(id);
        msg.setContent(computation);

        // 【服务发现】找到所有注册为 calculator/sum 服务的响应者
        var adresses = AgentServicesTools.searchAgents(this, "calculator", "sum");
        msg.addReceivers(adresses);
        println("Calculator agents found : " + Arrays.stream(adresses).map(AID::getLocalName).toList());

        println("_".repeat(40));
        println("I send a request about:" + msg.getContent());
        println("_".repeat(40));
        //msg sent as soon as the behavior is activated
        // 【挂载协议行为】消息不会立刻发出——等到该行为被激活时才会发出。
        //   覆写下面 4 个回调来处理不同回应
        AchieveREInitiator init = new AchieveREInitiator(this, msg) {
            //function triggered by a AGREE msg : the sender accept the resquest and will send an INFORM message with
            // its result
            // 【收到 AGREE】响应者同意参与，之后会发 INFORM 带结果
            @Override
            protected void handleAgree(ACLMessage agree) {
                window.println("agreement received from " + agree.getSender().getLocalName());
            }

            //function triggered by a REFUSE msg, the sender refuse to participate in the request
            // 【收到 REFUSE】响应者拒绝
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                window.println("refuse received from " + refuse.getSender().getLocalName());
            }

            //function triggered by an INFORM msg, the sender send its result
            // 【收到 INFORM】响应者发回结果
            @Override
            protected void handleInform(ACLMessage inform) {
                window.println("from " + inform.getSender().getLocalName() +
                        ", I received this result: " + inform.getContent());
            }


            //function triggered when all the responses (INFORM) have been received following the agreements
            //@param responses all the received INFORM msg
            // 【全部收齐后触发】所有响应者都回应完毕时被调用
            @Override
            protected void handleAllResultNotifications(List<ACLMessage> responses) {
                println("~".repeat(40));
                // 【汇总所有 INFORM】打印每个响应者的结果
                StringBuilder sb = new StringBuilder("ok! I received all the responses... " +
                        "To resume: \n");
                if (responses != null && !responses.isEmpty())
                    for (ACLMessage msg : responses) {
                        sb.append("\t-from ").append(msg.getSender().getLocalName()).append(" : ").append(msg.getContent()).append("\n");
                    }
                else {
                    // 【全部拒绝】没有任何响应者接受请求
                    sb.append("\t-no one accept your request !!!");
                }
                println(sb.toString());
                println("(o)".repeat(20));
            }
        };

        // 【加入行为栈】这个行为会被框架调度执行
        addBehaviour(init);
    }


    /**function triggered when an event is sent by the GUI.
     * create a request in the form 3+23+11+22....
     * a sum of 3 to 7 integers between 1 and 100
     * *
     * 【GUI 事件】生成一个 3~7 项、每项 1~100 的加法式，例如 "34+12+45"
     */
    protected void onGuiEvent(GuiEvent ignoredEvent) {
        Random r = new Random();
        int nb = r.nextInt(3, 7);
        StringBuilder sb = new StringBuilder(String.valueOf(r.nextInt(1, 100)));
        for (int i = 1; i < nb; i++)
            sb.append("+").append(r.nextInt(1, 100));
        // 【"123" 是消息的会话 ID】要与响应者端的 MessageTemplate.MatchConversationId("123") 匹配
        createRequest("123", sb.toString());
    }

}
