package behaviorTests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class for an agent having 1 parallel behavior that contains several behaviors acting 3 times
 * s'activant 3 fois
 *
 * 【并行行为演示】一个智能体挂 1 个 ParallelBehaviour，里面放 6 个子行为。
 *
 * 【ParallelBehaviour 调度规则】
 *   - 每次调度时，遍历所有子行为，凡是"还没 done"的都会被调用 action()
 *   - 所以 6 个"你好"会**交错出现**：bonjour(1) → hallo(1) → buongiorno(1) → ... → saluton(1) → bonjour(2) → ...
 *   - 全部子行为 done() 之后，ParallelBehaviour 才 done
 *
 * 对比 [AgentHelloEuropeenSequentiel](AgentHelloEuropeenSequentiel.java)：串行时是同一条消息跑完 3 次，才轮到下一条
 *
 * @author emmanueladam
 */
public class AgentHelloEuropeenParallel extends Agent {
    /**
     * main function
     * launch 2 agents whom the behaviors act in parallel
     *
     */
    // 【main】启动 2 个同类智能体
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:behaviorTests.AgentHelloEuropeenParallel").append(";");
        sbAgents.append("a2:behaviorTests.AgentHelloEuropeenParallel").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    // 【setup】建 ParallelBehaviour 并挂 6 个子行为
    @Override
    protected void setup() {
        out.println("I, Agent " + getLocalName() + ", my address is " + getAID());
        out.println("I execute several behaviors in parallel");

        //create a parallel behavior
        // 【并行容器】addSubBehaviour 添加子行为
        ParallelBehaviour paraB = new ParallelBehaviour();
        paraB.addSubBehaviour(europeanBehaviour("bonjour"));
        paraB.addSubBehaviour(europeanBehaviour("hallo"));
        paraB.addSubBehaviour(europeanBehaviour("buongiorno"));
        paraB.addSubBehaviour(europeanBehaviour("buenos dias"));
        paraB.addSubBehaviour(europeanBehaviour("Olá"));
        paraB.addSubBehaviour(europeanBehaviour("saluton"));


        // add a behavior that adds the parallel behavior in 100ms
        // 【延迟 100ms 挂载】
        addBehaviour(new WakerBehaviour(this, 100, a -> a.addBehaviour(paraB)));

    }

    /**
     * create a behavior that displays a message; this behavior can be executed 3 times
     * @param msg the msg to display
     * @return  a simple behavior that can be executed 3 times
     *
     * 【工厂方法：生成一个问候行为】每个行为打印 3 次后 done
     */
    private Behaviour europeanBehaviour(String msg) {
        Behaviour b = new Behaviour(this) {
            int i = 0;

            @Override
            public void action() {
                // 【打印"名字 -> 问候语 (n/3)"】
                printf("%s -> %s (%d/3)\n", new Object[]{getLocalName(), msg, (i + 1)});
                i++;
            }

            @Override
            public boolean done() {
                return i >= 3;   // 【3 次后 done】
            }
        };
        return b;
    }
}
