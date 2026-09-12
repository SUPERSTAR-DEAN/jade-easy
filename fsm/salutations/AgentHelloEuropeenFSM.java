package fsm.salutations;

import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;


/**
 * class for an agent that contains 1 behavior defined with a finite state machine (6 etats)
 * A <---\
 * /   \    \
 * B     C    \
 * / \    |    |
 * D   \   |    |
 * \   \ /     |
 * \-->E-----/
 * |
 * F
 *
 * 【FSM 状态的智能体】6 个状态组成的有限状态机，用来演示：
 *   - 如何用图结构组织多个行为（而不是让它们各自并行/串行跑）
 *   - 状态之间的转移可以由 `onEnd()` 返回值**动态决定**
 *   - 状态回环时要**reset** 相关状态，否则它们不会再执行
 *
 * 状态转移图（对照上面的 ASCII 图）：
 *   A →(0) B, A →(1) C
 *   B →(0) D, B →(1) E
 *   C → E（无条件）, D → E（无条件）
 *   E →(0) F（终止）, E →(1) A（回环，重置所有状态）
 *
 * @author emmanueladam
 * @since 2021-11-24
 */
public class AgentHelloEuropeenFSM extends AgentWindowed {

    /**
     * main function
     * start some agents (of type AgentHelloEuropeenFSM )
     */
    // 【main】启动两个同类智能体实例 a1、a2
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:fsm.salutations.AgentHelloEuropeenFSM").append(";");
        sbAgents.append("a2:fsm.salutations.AgentHelloEuropeenFSM").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("I execute the behaviors according to a finite state machine");

        //a Finite State Machine behavior
        // 【FSM 外壳】这个 FSMBehaviour 只是"容器"，本身不做事；真正干活的是下面注册进去的状态行为。
        //   onEnd() 重写：整个 FSM 跑完时让智能体下线
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                println("FSM behaviour ended, I leave");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        //we store the states (ideally, names "A", "B", .. "F" should be in constants
        // 【注册状态】每个状态 = 一个 EuropeanBehaviour 实例 + 一个名字字符串
        //INITIAL STATE (2 x bonjour)
        // 【初始状态】registerFirstState 指定"从哪个状态开始"
        fsm.registerFirstState(new EuropeanBehaviour("bonjour", 2), "A");
        // other states
        // 【中间状态】
        fsm.registerState(new EuropeanBehaviour("hallo", 2), "B");
        fsm.registerState(new EuropeanBehaviour("buongiorno", 3), "C");
        fsm.registerState(new EuropeanBehaviour("buenos dias", 3), "D");
        fsm.registerState(new EuropeanBehaviour("Olá", 1), "E");
        //FINAL STATE (1 x saluton)
        // 【终态】registerLastState 指定"跑完这个状态后 FSM 结束"
        fsm.registerLastState(new EuropeanBehaviour("saluton", 1), "F");

        // TRANSITIONS
        // 【注册转移】registerTransition(源状态, 目标状态, onEnd 期望值)
        // from A, we go to B if the behavior linked to A returns 0 On End
        // 【A → B：当 A 结束时返回 0】
        fsm.registerTransition("A", "B", 0);
        // from A, we go to C if the behavior linked to A returns 1 On End
        // 【A → C：当 A 结束时返回 1】
        fsm.registerTransition("A", "C", 1);
        // from B, we go to D if the behavior linked to B returns 0 On End
        fsm.registerTransition("B", "D", 0);
        // from B, we go to E if the behavior linked to B returns 1 On End
        fsm.registerTransition("B", "E", 1);
        // after C, we go to E without any condition
        // 【registerDefaultTransition：无条件转移】不管 onEnd 返回什么值都走这条
        fsm.registerDefaultTransition("C", "E");
        // after D, we go to E without any condition
        fsm.registerDefaultTransition("D", "E");
        // from E, we go to F if the behavior linked to E returns 0 On End
        fsm.registerTransition("E", "F", 0);
        // from E, we go to A if the behavior linked to E returns 1 On End
        //then we indicate which behaviors have to be reset
        //if we do not "reset" them, the 'EuropeanBehaviors' will not reset the current number of cycles
        // 【E → A：回环转移】最后一个参数是要重置的状态列表。
        //   不加这个参数，A/B/C/D 因为之前 done() 返回过 true，就不会再执行——FSM 看起来"卡住"了。
        fsm.registerTransition("E", "A", 1, new String[]{"A", "B", "C", "D", "E", "F"});

        // add the fsm behavior in  100ms
        // 【延迟 100ms 挂载 FSM】先给智能体一点启动时间，再让 FSM 接手
        addBehaviour(new WakerBehaviour(this, 100, a -> a.addBehaviour(fsm)));
    }

}
