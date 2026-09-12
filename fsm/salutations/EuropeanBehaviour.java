package fsm.salutations;

import jade.core.behaviours.Behaviour;
import jade.gui.AgentWindowed;

/**
 * minimal behavior that display nbCycles times a msg
 *
 * 【FSM 状态的基本单元】本类是有限状态机里"某一个状态"对应的行为：
 * 一个行为 = 一个状态，执行 nbCycles 次后结束，结束时随机返回 0 或 1 作为转移依据。
 *
 * 三个关键回调（都是 Behaviour 的扩展点）：
 *   - action()：每次调度时被调用，这里是"计数 + 打印"
 *   - done()：返回 true 表示这个状态结束
 *   - onEnd()：状态结束后被框架调用，返回值决定 FSM 走哪条转移边
 *
 * @author emmanueladam
 * @version 2021-11-24
 */
class EuropeanBehaviour extends Behaviour {
    /**
     * msg to display
     */
    // 【要打印的消息文本】例如 "bonjour"
    String msg;
    /**
     * nb cyles of execution
     */
    // 【总循环次数】决定这个状态跑几次
    int nbCycles;
    /**
     * current cycle
     */
    // 【当前进度】每 action() 一次 +1
    int i = 0;

    // 【构造】由 FSM 在 registerState 时创建，msg 是要说的话、stop 是次数
    EuropeanBehaviour(String msg, int stop) {
        this.msg = msg;
        this.nbCycles = stop;
    }

    /**
     * if the behaviour is "reseted", current cycle goes to 0
     */
    // 【reset()】状态被"重置"时调用（见 FSM 的 registerTransition 里传了要 reset 的状态数组）
    //   如果不调用 reset，这个行为因为 done() 已经返回过 true 而不会重新跑
    @Override
    public void reset() {
        i = 0;
    }

    /**
     * display the agent name, the msg, the current cycle and the nb of cycles to do
     */
    // 【action()】每被调度一次，打印一次问候，并推进计数
    @Override
    public void action() {
        i++;
        ((AgentWindowed) myAgent).getWindow().println(msg + " " + i + "/" + nbCycles);
    }

    /**
     * behaviour done if current cycle = nb of cycles to do
     */
    // 【done()】次数跑满就结束这个状态
    @Override
    public boolean done() {
        return i >= nbCycles;
    }


    /**
     * at the end, return randomly an integer, 0 or 1
     */
    // 【onEnd()】状态结束后的返回值。FSM 用这个值决定走哪条转移边。
    //   这里返回 0 或 1（Math.random()+0.5 四舍五入），使转移具备随机性。
    //   注意：如果 onEnd() 返回的整数不在任何 registerTransition 的期望值里，
    //   FSM 会找 registerDefaultTransition 的默认转移。
    @Override
    public int onEnd() {
        return (int) (Math.random() + 0.5);
    }

}
