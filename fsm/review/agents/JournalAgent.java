package fsm.review.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.out;


/**
 * class for an journal agent that use a Finite State MAchine behavior to manage its process of submission of an article
 *
 * 【期刊智能体】整个投稿流程的**编排者**，FSM 状态最多。
 *
 * 状态转移图：
 *   WAIT_ARTICLE →(默认) SEND_TO_3REVIEWERS →(默认) WAIT_3REVIEWS →(默认) SEND_DECISION
 *   SEND_DECISION →(0=拒/2=通过) END
 *   SEND_DECISION →(1=需修改) WAIT_ACK
 *   WAIT_ACK →(0=作者放弃) END
 *   WAIT_ACK →(1=作者再改) SEND_TO_3REVIEWERS（重置 WAIT_3REVIEWS/SEND_DECISION/WAIT_ACK）
 *
 * 关键教学点：
 *   1. **聚合决策**：`val = val * mark` 是"任何一个审稿人给 0 就是 0"的乘法技巧
 *   2. **共享数据盒**：`volatile HashMap ds` 是多个行为之间的数据总线
 *   3. **回环重置**：修改再投时，reset 让"等评分/发结果"重新可用
 *
 * @author eadam
 */
public class JournalAgent extends AgentWindowed {
    // 【状态名常量】
    final String WAIT_ARTICLE = "wait_proposal";
    final String SEND_TO_3REVIEWERS = "send_for_review";
    final String WAIT_3REVIEWS = "wait_reviews";
    final String SEND_DECISION = "send_decision";
    final String WAIT_ACK = "wait_acknowledgment_of_receipt";
    final String END = "terminate_session";
    // 【共享数据盒】volatile 保证多个行为线程看到同一份数据
    volatile HashMap<String, Object> ds = new HashMap<>();

    // 【当前注册的审稿人数】用于判断"评分收齐了没有"
    int nbReviewers;

    /**
     * A journal waits for an article, forwards it to reviewers, sends the result to the author
     * and stops if return is acceptance or refusal; or awaits feedback from the author whether  to resubmit
     *
     * 【setup】装配 FSM 后延迟 100ms 挂载
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(false);
        window.setBackgroundTextColor(Color.ORANGE);


        //creation of finite state machine type behavior
        // 【FSM 外壳】onEnd 时下线
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour terminé, je m'en vais");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        // INITIAL STATE
        // 【初始状态：等投稿】
        fsm.registerFirstState(waitForProposal(ds), WAIT_ARTICLE);
        // other states
        fsm.registerState(sendForReviewing(ds), SEND_TO_3REVIEWERS);
        fsm.registerState(attendreReviews(ds), WAIT_3REVIEWS);
        fsm.registerState(sendResult(ds), SEND_DECISION);
        // 【等作者答复】
        fsm.registerState(attendreAvisAuteur(ds), WAIT_ACK);
        //FINAL STATES
        fsm.registerLastState(arreter(ds), END);

        //____TRANSITIONS
        // 【线性主流水】收稿 → 派稿 → 收评 → 出结果
        fsm.registerDefaultTransition(WAIT_ARTICLE, SEND_TO_3REVIEWERS);
        fsm.registerDefaultTransition(SEND_TO_3REVIEWERS, WAIT_3REVIEWS);
        fsm.registerDefaultTransition(WAIT_3REVIEWS, SEND_DECISION);
        //if decision is to accept (2) or reject (0), terminate the behavior
        // 【出结果后：拒/通过都是终态】
        fsm.registerTransition(SEND_DECISION, END, 0);
        fsm.registerTransition(SEND_DECISION, END, 2);
        //if decision is to propose to send a revision (1), wait for the author decision
        // 【需修改 → 等作者决定】
        fsm.registerTransition(SEND_DECISION, WAIT_ACK, 1);
        //if author decision is to cancel (0), terminate the behavior
        // 【作者放弃 → 结束】
        fsm.registerTransition(WAIT_ACK, END, 0);
        //if author decision is to send a new version (1), reset the behaviors and go back to send to reviewers step
        // 【作者再改 → 回到派稿】
        //   注意 reset 列表：让 WAIT_3REVIEWS / SEND_DECISION / WAIT_ACK 三个状态重新可用
        fsm.registerTransition(WAIT_ACK, SEND_TO_3REVIEWERS, 1, new String[]{WAIT_3REVIEWS, SEND_DECISION, WAIT_ACK});

        // add the FSM behavior in 100ms
        // 【延迟挂载】
        addBehaviour(new WakerBehaviour(this, 100, a -> a.addBehaviour(fsm)));
    }


    /**
     * Wait for a message containing an ""article""
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that listen for a "propose" msg, and store its key
     */
    // 【状态：等投稿】阻塞接收一条 PROPOSE 消息，把稿件和会话 key 存进共享盒
    private Behaviour waitForProposal(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            MessageTemplate mt;

            @Override
            public void onStart() {
                // 【只接收 PROPOSE 类消息】
                mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            }

            @Override
            public void action() {
                ACLMessage msg = null;
                // 【阻塞接收】没消息就 block() 让出 CPU
                while ((msg = blockingReceive(mt)) == null) block();
                // 【存入共享盒】供后续状态取用
                ds.put("article", msg);
                ds.put("key", msg.getConversationId());

                println("---> from %s, I received this \"%s\" with the key %s".formatted(msg.getSender().getLocalName(),
                        msg.getContent(), msg.getConversationId()));
            }
        };
        return b;
    }

    /**
     * search for 3 reviewers and send them an article to evaluate
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that send a msg to 3 reviewer agents
     */
    // 【状态：派稿给审稿人】通过黄页查询"journal/reviewer"服务下的智能体，然后把稿件转发给他们
    private Behaviour sendForReviewing(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {

            @Override
            public void action() {
                // 【服务发现】查到所有注册为审稿人的智能体
                var reviewers = AgentServicesTools.searchAgents(myAgent, "journal", "reviewer");
                nbReviewers = reviewers.length;
                ACLMessage msg = (ACLMessage) ds.get("article");
                String key = (String) ds.get("key");
                // 【createReply 后清接收人】借用回复的元数据，但重新指定收件人为所有审稿人
                ACLMessage forward = msg.createReply();
                forward.clearAllReceiver();
                forward.setContent(msg.getContent());
                forward.addReceivers(reviewers);
                forward.setConversationId(key);
                myAgent.send(forward);
                var localNames = Arrays.stream(reviewers).map(AID::getLocalName).toArray(String[]::new);
                println("I've sent the article to evaluate (with the key " + key + ") to  " + Arrays.toString(localNames));
            }
        };
        return b;
    }

    /**
     * wait for 3 evaluations from the reviewer; compute the global evaluation (2 if all reviewers give the best
     * mark, 0 if at least one of them has rejected the article and 1 in other cases)
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that wait for 3 msg on a given key
     */
    // 【状态：收集评分】收集 3 个审稿人的评分，用乘法聚合：
    //   - 只要有一个 0，乘积就是 0 → 拒稿
    //   - 全部是 2（3 个人），乘积是 8 → 通过
    //   - 其他情况 → 需修改
    private Behaviour attendreReviews(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            int i = 0;    // 【收到几个评分】
            int val = 1;  // 【评分乘积】初值 1，因为乘法单位元
            MessageTemplate mt;

            @Override
            public void onStart() {
                String key = (String) ds.get("key");
                mt = MessageTemplate.MatchConversationId(key);
                println("I wait for messages from reviewers on this key " + key);
            }

            // 【reset()】回环时归零，允许新一轮收集
            @Override
            public void reset() {
                i = 0;
                val = 1;
            }

            @Override
            public void action() {
                ACLMessage msg = blockingReceive(mt);

                if (msg != null) {
                    i++;
                    // 【核心技巧：乘法聚合】任何一个人给 0 就把整个乘积打成 0
                    val = val * Integer.parseInt(msg.getContent());
                    println("--> I received the mark  %s from %s".formatted(msg.getContent(),
                            msg.getSender().getLocalName()));
                } else block();
            }

            /**done when all the reviewers answered*/
            // 【done()】收齐所有审稿人的评分就结束
            @Override
            public boolean done() {
                return i == nbReviewers;
            }

            // 【onEnd()】把乘积映射到 0/1/2
            @Override
            public int onEnd() {
                if (val == 8) val = 2;          // 2*2*2 = 8 → 全通过
                else if (val != 0) val = 1;     // 不是 0 也不是 8 → 部分通过
                ds.put("eval", val);
                println("Evaluation finished with this evaluation : " + val);
                return val;
            }
        };
        return b;
    }

    /**
     * send the result of the evaluation to the author
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that send a msg to the author
     */
    // 【状态：发结果】根据聚合评分给作者回复
    private Behaviour sendResult(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int val = 0;

            @Override
            public void action() {
                val = (Integer) (ds.get("eval"));
                ACLMessage msg = (ACLMessage) ds.get("article");
                // 【createReply】回复给原投稿者
                ACLMessage reply = msg.createReply();
                // 【消息内容格式：评分:文字】这个格式是作者那边 waitEvaluation 解析的依据
                switch (val) {
                    case 0 -> reply.setContent("0: Sorry, your article has not been accepted.... Persevere and try again next time");
                    case 1 -> reply.setContent("1: The article is accepted subject to change ...");
                    case 2 -> reply.setContent("2: It's a pleasure to inform you that your article is accepted !");
                }
                myAgent.send(reply);
                println("I send the result to '" + msg.getSender().getLocalName() + "' : \"" + reply.getContent() + "\"");
            }

            // 【返回聚合评分】FSM 据此决定是否 END 或 WAIT_ACK
            @Override
            public int onEnd() {
                return val;
            }
        };
        return b;
    }

    /**
     * wait the decision of the author (new revision or abandon)
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that wait for a decision msg from the author
     */
    // 【状态：等作者决定】收到 CANCEL=放弃，收到 PROPOSE=再改
    private Behaviour attendreAvisAuteur(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            boolean end = false;   // 【是否收到决定】
            int val = 0;           // 【0=放弃, 1=再改】
            String key;
            MessageTemplate mt;

            @Override
            public void onStart() {
                key = String.valueOf(ds.get("key"));
                mt = MessageTemplate.MatchConversationId(key);
                window.setButtonActivated(false);
            }

            @Override
            public void reset() {
                key = String.valueOf(ds.get("key"));
                mt = MessageTemplate.MatchConversationId(key);
            }


            @Override
            public void action() {
                ACLMessage msg = blockingReceive(mt);
                if (msg != null) {
                    println("I received this: \"" + msg.getContent() + "\"");
                    // 【用 performative 区分意图】CANCEL=放弃，PROPOSE=再改
                    if (msg.getPerformative() == ACLMessage.CANCEL) {
                        println("--> The author does not wish to continue  ...");
                        val = 0;
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        // 【更新稿件和 key】把新版本存回共享盒
                        ds.put("key", msg.getConversationId());
                        ds.put("article", msg);
                        println("--> The author submits a new version.");
                        val = 1;
                    }
                    end = true;
                    println("-".repeat(40));
                } else block();
            }

            @Override
            public boolean done() {
                return end;
            }

            @Override
            public int onEnd() {
                return val;
            }

        };
        return b;
    }

    /**
     * Close the process
     * @return a One-shot behavior that displays a msg indicating the process is finished
     */
    // 【终态】打印结束语
    private Behaviour arreter(HashMap<String, Object> ds) {
        return new OneShotBehaviour(this, a -> {
            println("the submission process is finished ... ");
            println("~".repeat(40));
        });
    }


}
