package protocols.requests.agents;


import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.awt.*;
import java.util.Random;


/**
 * class for an agent that waits for a message from the AchieveRE protocol, prepares the response, and returns it
 *
 * 【FIPA-Request 协议的响应方】对称的另一半。
 *
 * `AchieveREResponder` 封装了"接收 REQUEST → 决定 AGREE/REFUSE → 若 AGREE 则发 INFORM"的完整流程。
 * 你只需覆写两个方法：
 *   - `handleRequest(request)` → 返回一条 AGREE 或 REFUSE 消息
 *   - `prepareResultNotification(request, response)` → 返回一条 INFORM 消息（只在 AGREE 时调用）
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentRequestResponder extends AgentWindowed {

    /**
     * agent set-up
     * add an AchieveRE behavior for responder
     *
     * 【setup】注册服务 + 挂 AchieveREResponder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        //register to the service "calculator-sum"
        // 【注册为 calculator/sum 服务】发送方用 searchAgents 能找到它
        AgentServicesTools.register(this, "calculator", "sum");
        Random hasard = new Random();


        //stamp on the request
        // 【只处理会话 ID = "123" 的请求】必须与发送方匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("123");

        //add a AchieveRE behavior that wait for a request and reply
        // 【挂载协议响应行为】
        AchieveREResponder init = new AchieveREResponder(this, model) {

            //function triggered by a REQUEST msg :
            // the agents decides to refuse or to agree with the request
            //if it agrees it hase to send an inform message next
            //return the answer to the request
            // 【覆写 handleRequest】收到 REQUEST 时决定 AGREE 还是 REFUSE
            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                window.setBackgroundTextColor(Color.WHITE);
                window.println("received  " + request.getContent());
                // 【createReply】自动带上会话 ID，方便发送方按 key 追踪
                ACLMessage answer = request.createReply();
                //parfois l'agent choisi de refuser la demande
                // 【50% 概率同意】
                if (hasard.nextBoolean()) {
                    answer.setPerformative(ACLMessage.AGREE);
                    window.setBackgroundTextColor(Color.PINK);
                    println("I'm ok to answer...");
                    println("-".repeat(40));
                } else
                {
                    // 【50% 概率拒绝】
                    answer.setPerformative(ACLMessage.REFUSE);
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I refuse the request !");
                    println("(o)".repeat(20));
                }
                return answer;
            }

            //Function used to return a result by a message
            //param : request = initial initial request, response = agreement I just sent
            // 【覆写 prepareResultNotification】只在 AGREE 之后被调用，发回计算结果
            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                // 【解析加法式】内容形如 "34+12+45"，按 '+' 分割后求和
                String content = request.getContent();
                String[] strValues = content.split("\\+");
                int sum = 0;
                for (String strV : strValues)
                    sum += Integer.parseInt(strV);

                ACLMessage answer = request.createReply();
                answer.setPerformative(ACLMessage.INFORM);   // 【必须用 INFORM】
                answer.setContent("result = " + sum);
                println("I send: " + sum);
                println("(o)".repeat(20));
                return answer;
            }
        };

        // 【加入行为栈】
        addBehaviour(init);
    }

    /**deregister to the service and close the window when leaving*/
    // 【takeDown】下线前注销服务 + 关窗口
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        window.setVisible(false);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }
}
