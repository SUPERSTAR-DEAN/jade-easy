/*
 * 【helloworldSolo 模块】JADE 入门最基础的例子：两个"你好世界"智能体。
 * 本目录是学习 JADE 的第一个台阶，先搞清楚"一个智能体由什么构成"、
 * "怎么把智能体放进平台"。
 *
 * 两个类对比着看：
 *   - AgentHello          ：消息写死在代码里
 *   - AgentHelloParametre ：消息作为创建智能体时的参数传进来
 *
 * 建议先读 AgentHello.java，再读 AgentHelloParametre.java，体会"参数化"这个区别。
 */
package helloworldSolo;

import jade.core.Agent;

/**
 * A simple agent that display a text
 *
 * 【第一个智能体】最简单的 JADE 智能体：在控制台打印一句"你好"。
 *
 * 关键点：
 *   - 继承 jade.core.Agent 就是一个智能体
 *   - main() 负责启动 JADE 平台并创建智能体（这里用 jade.Boot 命令行方式）
 *   - setup() 是智能体上线后被调用的第一个方法（相当于构造函数）
 *   - takeDown() 是智能体离开平台前的最后一步
 *
 * @author emmanueladam
 */
public class AgentHello extends Agent {
    /**
     * this main launch JADE plateforme and asks it to create an agent
     *
     * 【启动平台】这一行 jade.Boot.main(...) 等价于在命令行敲：
     *     java jade.Boot -gui -agents myFirstAgent:helloworldSolo.AgentHello
     * 其中 "-agents 名字:类全名(参数);名字2:类2(...)" 是告诉平台要创建哪些智能体。
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        // 拼接 "智能体名:类全限定名;" 格式的字符串
        sbAgents.append("myFirstAgent:helloworldSolo.AgentHello").append(";");
        jadeArgs[0] = "-gui";   // 打开 JADE 自带的图形调试界面
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);   // 真正启动平台
    }

    /**
     * agent set-up
     *
     * 【上线初始化】智能体被创建后自动执行。这里只做打印，然后主动下线。
     */
    @Override
    protected void setup() {
        String texteHello = "Hello everybody and especially you !";

        // getLocalName() 取智能体名；getAID() 取它在整个平台中的完整地址
        println("From agent " + getLocalName() + " : " + texteHello);
        println("My address is " + getAID());
        // 【主动下线】让平台把自己销毁；不调用这句，智能体会一直活着
        doDelete();
    }

    // 'clean-up' of the agent
    // 【下线清理】智能体离开平台时执行，可以理解为"析构函数"
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
