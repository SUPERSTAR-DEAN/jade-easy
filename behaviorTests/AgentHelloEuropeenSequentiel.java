package behaviorTests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class for an agent having 1 sequential behavior that contains several behaviors acting 3 times
 *
 * 【串行行为演示】一个智能体挂 1 个 SequentialBehaviour，里面放 6 个子行为。
 *
 * 【SequentialBehaviour 调度规则】
 *   - 只有"第一个还没 done 的子行为"会被调度
 *   - 该子行为 done 后，才轮到下一个
 *   - 输出顺序：bonjour(1) → bonjour(2) → bonjour(3) → hallo(1) → hallo(2) → ...
 *
 * 对比 [AgentHelloEuropeenParallel](AgentHelloEuropeenParallel.java)：并行时是所有子行为交错进行
 *
 * @author emmanueladam
 */
public class AgentHelloEuropeenSequentiel extends Agent {
    /**
     * main function
     * launch 2 agents whom the behaviors act in sequence
     *
     */
    // 【main】启动 2 个同类智能体
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:behaviorTests.AgentHelloEuropeenSequentiel").append(";");
        sbAgents.append("a2:behaviorTests.AgentHelloEuropeenSequentiel").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     *
     * 【setup】建 SequentialBehaviour 并挂 6 个子行为
     */
    @Override
    protected void setup() {
        out.printf("""
                I, Agent %s, my address is %s
                    I execute several behaviors in parallel%n""", getLocalName(), getAID());

        // 【串行容器】
        SequentialBehaviour seqB = new SequentialBehaviour();
        seqB.addSubBehaviour(europeanBehaviour("bonjour"));
        seqB.addSubBehaviour(europeanBehaviour("hallo"));
        seqB.addSubBehaviour(europeanBehaviour("buongiorno"));
        seqB.addSubBehaviour(europeanBehaviour("buenos dias"));
        seqB.addSubBehaviour(europeanBehaviour("Olá"));
        seqB.addSubBehaviour(europeanBehaviour("saluton"));

        // add a behavior that adds the parallel behavior in 100ms
        // 【延迟 100ms 挂载】
        addBehaviour(new WakerBehaviour(this, 100, a -> a.addBehaviour(seqB)));


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
                println("%s -> %s (%d/3)".formatted(getLocalName(), msg, (i + 1)));
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
