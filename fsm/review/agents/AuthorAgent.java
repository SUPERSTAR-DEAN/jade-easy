package fsm.review.agents;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;

import static java.lang.System.out;


/**
 * class for an author agent that use a Finite State MAchine behavior to manage its process of submission of an article
 *
 * 【作者智能体】用 FSM 管理"投稿 → 等结果 → 修改再投 / 放弃 / 庆祝"的完整流程。
 *
 * 状态转移图：
 *   SUBMIT →(默认) WAITRESULT
 *   WAITRESULT →(0=被拒) STOP
 *   WAITRESULT →(2=全通过) CELEBRATE
 *   WAITRESULT →(1=需修改) CONTINUE
 *   CONTINUE →(0=决定放弃) STOP
 *   CONTINUE →(1=决定再改) WAITRESULT（重置 WAITRESULT/CONTINUE）
 *
 * 关键设计点：
 *   - 每个状态是一个 OneShotBehaviour（一次性行为），通过工厂方法生成
 *   - 用 HashMap<String,Object> `ds` 在多个行为之间**共享数据**（这里是消息的会话 key）
 *   - `onEnd()` 返回值就是转移条件——把业务规则表达成数字返回
 *
 * @author eadam
 */
public class AuthorAgent extends AgentWindowed {

    // 【状态名常量】FSM 里状态名传字符串，用常量避免拼错
    final String SUBMIT = "submission";
    final String WAITRESULT = "wait_decision";
    final String CONTINUE = "continue?";
    final String STOP = "abandon";
    final String CELEBRATE = "celebrate";

    FSMBehaviour fsm;

    /**
     * an author agent submits an article, waits for the decision and then celebrates, stops or resubmits as appropriate
     *
     * 【setup】只是"装配"FSM——定义状态和转移，并不启动它。
     *   真正的启动在 onGuiEvent()（用户点按钮时）。
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.setButtonActivated(true);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        // 【共享数据盒】FSM 的多个状态之间如何共享变量？把它们塞进一个 Map，
        //   然后把 Map 本身传给每个行为的工厂方法
        HashMap<String, Object> ds = new HashMap<>();


        //creation of finite state machine type behavior
        // 【FSM 外壳】onEnd 时下线
        fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour ended, I'm leaving");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        // INITIAL STATE
        // 【初始状态】点击按钮后从这里开始
        fsm.registerFirstState(submit(ds), SUBMIT);
        // other states
        fsm.registerState(waitEvaluation(ds), WAITRESULT);
        // 【CONTINUE 是"决定要不要再改"的状态】
        fsm.registerState(resubmit(ds), CONTINUE);
        //FINAL STATES
        // 【两个终态】registerLastState 意味着跑完这个状态 FSM 就结束了
        fsm.registerLastState(arreter(), STOP);
        fsm.registerLastState(celebrate(), CELEBRATE);

        //____TRANSITIONS
        // 【投稿后直接等结果，无条件转移】
        fsm.registerDefaultTransition(SUBMIT, WAITRESULT);
        //If the behavior related to Wait results returns 0, it's over
        // 【WAITRESULT 返回 0 → STOP】0 = 被拒
        fsm.registerTransition(WAITRESULT, STOP, 0);
        //If the behavior related to Wait results returns 0, it's over but I celebrate
        // 【WAITRESULT 返回 2 → CELEBRATE】2 = 全部高分，直接通过
        fsm.registerTransition(WAITRESULT, CELEBRATE, 2);
        //If the behavior related to Wait results returns 1, I decide if I submit again
        // 【WAITRESULT 返回 1 → CONTINUE】1 = 需要修改
        fsm.registerTransition(WAITRESULT, CONTINUE, 1);
        //If the behavior related to the reflexion about resubmission returns 0, I decide to stop
        // 【CONTINUE 返回 0 → STOP】作者决定放弃
        fsm.registerTransition(CONTINUE, STOP, 0);
        //If the behavior related to the reflexion about resubmission returns 0, I try a new submission, and I reset some behaviors
        // 【CONTINUE 返回 1 → 再投一次】回环到 WAITRESULT，并 reset 相关状态
        //   如果不 reset，WAITRESULT 因为之前 done() 返回过 true，不会再执行
        fsm.registerTransition(CONTINUE, WAITRESULT, 1, new String[]{WAITRESULT, CONTINUE});


    }

    /**click on the button => submission (only 1 click allowed)*/
    // 【按钮事件】重置 FSM 并挂上——只在用户点击时启动
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        // 【reset()】把 FSM 所有状态归零，可以重复点击按钮重跑
        fsm.reset();
        addBehaviour(fsm);
        window.setButtonActivated(false);   // 【按钮置灰】防止重复触发
    }

    /**
     * Submit an article to the journal agent
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that sends a msg to the journal agent, and store its key
     */
    // 【状态：投稿】构造一次性行为——发一条 PROPOSE 消息给期刊智能体 j
    private Behaviour submit(HashMap<String, Object> ds) {
        return new OneShotBehaviour(this, a -> {
            // 【生成唯一会话 key】用时间戳当消息 ID，方便之后按 key 匹配回复
            String key = "msg" + System.currentTimeMillis();
            ds.put("key", key);   // 【存入共享盒】让后续行为能取到这个 key
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setContent("here is my original prose...");
            msg.setConversationId(key);
            msg.addReceiver("j");   // 【收件人】期刊智能体名叫 "j"
            send(msg);
            println("I've sent \"%s\" with the key (%s)".formatted(msg.getContent(), ds.get("key")));
        });
    }

    /**
     * wait for a response corresponding to the article sent
     * @param ds the data store containing the keys linked to the msg sent
     * @return a one shot behaviour that wait for a response
     */
    // 【状态：等评价】阻塞接收，直到收到 key 匹配的评价消息
    private Behaviour waitEvaluation(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            // 【决策结果】0=拒, 1=修改, 2=通过
            int evaluation = 1;
            String key;
            MessageTemplate mt;

            @Override
            public void onStart() {
            }

            // 【reset()】回环时把结果重置，允许新一轮判断
            @Override
            public void reset() {
                evaluation = 1;
            }

            @Override
            public void action() {
                // 【从共享盒取 key】
                key = String.valueOf(ds.get("key"));
                // 【构造过滤器】只接收会话 ID 匹配的消息
                mt = MessageTemplate.MatchConversationId(key);
                println("I wait for a response with this key : " + mt);
                ACLMessage msg = null;
                // 【阻塞接收 + 空转等待】
                //   blockingReceive 无消息时立刻返回 null，block() 让出 CPU
                //   这个 while 循环就是"等到有匹配的消息为止"
                while ((msg = blockingReceive(mt)) == null) block();
                // 【解析评分】消息内容格式是 "评分:文字说明"
                evaluation = Integer.parseInt(msg.getContent().split(":")[0]);
                println("---> I received this evaluation: \"" + msg.getContent() + "\"");
            }

            // 【onEnd() 返回评分本身】FSM 据此决定走 STOP / CELEBRATE / CONTINUE
            @Override
            public int onEnd() {
                return evaluation;
            }

        };
        return b;
    }


    /**
     * Decision to continue or not the submission
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a One-shot behavior that sends an MSG to the journal agent indicating whether the author is abandoning or proposing a new version
     */
    // 【状态：决定是否再改】30% 概率放弃，70% 概率再修改一版
    private Behaviour resubmit(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            // 【决策】0=放弃, 1=再改
            int decision = 1;
            // 【修改版本号】每次再改时递增
            int i = 1;

            @Override
            public void reset() {
            }

            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                // 【沿用原来的会话 key】让期刊知道这是同一篇稿子的修订
                msg.setConversationId(String.valueOf(ds.get("key")));
                msg.addReceiver(new AID("j", AID.ISLOCALNAME));
                if (Math.random() < 0.3) {
                    // 【30% 概率放弃】发 CANCEL
                    msg.setPerformative(ACLMessage.CANCEL);
                    msg.setContent("I propose to stop and cancel the submission...regards.");
                    println("I decided to stop and cancel the submission...");
                    decision = 0;
                } else {
                    // 【70% 概率再改】发 PROPOSE，内容带版本号
                    msg.setContent("here is my revision R#" + i);
                    println("I send my revision R#" + i + " with the key " + msg.getConversationId());
                    i++;
                }
                println("-".repeat(40));
                send(msg);
            }

            // 【返回决策】0=放弃, 1=继续
            @Override
            public int onEnd() {
                return decision;
            }
        };
        return b;
    }

    /**
     * Abandon
     * @return a One-shot behavior that displays a msg indicating the abandon of the process
     */
    // 【终态：放弃】只是打印一句鼓励的话
    private Behaviour arreter() {
        Behaviour b = new OneShotBehaviour(this) {
            int i = 0;

            @Override
            public void reset() {
                i = 0;
            }

            @Override
            public void action() {
                println("Courage and try again later...");
                println("~".repeat(40));
            }
        };
        return b;
    }

    /**
     * Celebration
     * @return a One-shot behavior that displays a msg indicating the author is happy
     */
    // 【终态：庆祝】用 Lambda 写法最简洁
    private Behaviour celebrate() {
        return new OneShotBehaviour(this, a -> {
            println("Great !! My new article has been accepted !!");
            println("~".repeat(40));
        });
    }

}
