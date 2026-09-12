package behaviorTests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class of an agent that contains 2 endless behaviors, one displaying 'hello', the other 'hi'.
 * the agent also has a delayed behavior that removes it from the platform
 *
 * 【三种内置行为的示范】本类在一个智能体里同时挂载三种 JADE 内置行为：
 *   1. 匿名内部类实现的 **Behaviour**：无限循环，每次打印 "Hello" 后最多等 300ms
 *   2. **TickerBehaviour**：每 500ms 触发一次，打印 "Hi"
 *   3. **WakerBehaviour**：2 秒后触发一次，然后让智能体下线
 *
 * 这三个行为的调度顺序、是否被中断，是 JADE 调度模型的重点。
 * 对照读 [AgentHelloEuropeenParallel](AgentHelloEuropeenParallel.java)（并行模式）
 * 和 [AgentHelloEuropeenSequentiel](AgentHelloEuropeenSequentiel.java)（串行模式）。
 *
 * @author emmanueladam
 */
public class AgentHelloSalut extends Agent {
    /**
     * main function.
     * launch 2 agents that acts "in parallel"
     *
     * 【启动两个智能体】用 jade.Boot 命令行方式启动，`-agents a1:...;a2:...` 表示创建两个实例
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:behaviorTests.AgentHelloSalut").append(";");
        sbAgents.append("a2:behaviorTests.AgentHelloSalut").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     *
     * 【setup】挂载三种行为
     */
    @Override
    protected void setup() {
        out.println("Me, Agent " + getLocalName() + ", my address is " + getAID());

        // add an "eternal" behavior which, on each pass, displays hello and pauses for at most 300 ms
        //(should be replaced by cyclic behavior, see next behavior)
        // 【自定义 Behaviour：无限循环】action() 每次被调用都会执行，done() 永远返回 false
        addBehaviour(new Behaviour(this) {
            public void action() {
                println("From agent " + getLocalName() + " : Hello everybody and especially you!");
                //pause at most for 300ms, or less if the agent receives a message
                // 【block(300)】最多阻塞 300ms——如果期间收到消息会立刻被唤醒，不用等满 300ms
                block(300);
            }

            /**this behavior never ends*/
            // 【永不结束】返回 false 表示"这个行为还没做完，下次还能再选它"
            public boolean done() {
                return false;
            }
        });

        // 【TickerBehaviour：周期性】每 500ms 由框架定时触发一次，用 Lambda 写回调
        addBehaviour(new TickerBehaviour(this, 500, a -> {
            println("From agent " + a.getLocalName() + " : Hi !!!");
        }));

        // 【WakerBehaviour：一次性延迟】2 秒后触发一次，然后让智能体下线
        addBehaviour(new WakerBehaviour(this, 2000, a -> {
            out.println("From agent " + a.getLocalName() + " : well, I'm leaving...");
            a.doDelete();
        }));
    }

    // 'clean-up' of the agent
    // 【takeDown】下线时打印告别语
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
