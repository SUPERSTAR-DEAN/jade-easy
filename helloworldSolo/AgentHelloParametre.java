package helloworldSolo;

import jade.core.Agent;

import static java.lang.System.out;

/**
 * A simple agent that display a text found in its parameters
 *
 * 【带参数的智能体】和 AgentHello 几乎一样，区别在于：
 * 欢迎语不是写死的，而是创建智能体时作为参数传进来的。
 *
 * 这是 JADE 的重要机制——同一个类可以创建出多个行为/数据不同的实例。
 * 比如创建 agentA 传 "Hi"、创建 agentB 传 "Hello"，它们跑的是同一段代码。
 *
 * @author emmanueladam
 */
public class AgentHelloParametre extends Agent {
    /**
     * this main launch JADE plateforme and asks it to create an agent
     *
     * 【一次创建多个智能体】注意这里拼了两个智能体，用分号分隔，
     * 并且括号里带了构造参数：
     *     agentA:helloworldSolo.AgentHelloParametre(Hi)
     *     agentB:helloworldSolo.AgentHelloParametre(Hello)
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        // 每个智能体格式为 "实例名:类全名(参数)"，多个之间用 ";" 分隔
        sbAgents.append("agentA:helloworldSolo.AgentHelloParametre(Hi)").append(";");
        sbAgents.append("agentB:helloworldSolo.AgentHelloParametre(Hello)").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     *
     * 【读取创建参数】getArguments() 返回创建时传进来的参数数组。
     */
    @Override
    protected void setup() {
        String texteHello = null;
        // 【取参数】创建时传的 "Hi" / "Hello" 就在这里被取出来
        Object[] params = this.getArguments();
        // 有参数就用参数；没参数就用默认值（防御性写法）
        if (params.length > 0) texteHello = (String) params[0];
        else texteHello = "Hello everybody and especially you !";

        println("From agent " + getLocalName() + " : " + texteHello);
        println("My address is " + getAID());
        // 注意：这里不调用 doDelete()，所以两个智能体会一直留在平台上
    }

    // 'Nettoyage' de l'agent
    // 【下线清理】智能体离开平台时执行
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
