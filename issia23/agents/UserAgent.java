package issia23.agents;

import issia23.data.Product;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import issia23.behaviours.ContacterRepairCafe;

import java.util.*;

/**
 * 【用户智能体】循环经济场景的入口——用户有一个产品需要维修。
 *
 * 工作流程：
 *   1. 用户点击按钮（onGuiEvent）
 *   2. 通过黄页查询所有 "repair" 服务的智能体（维修店、零件店等）
 *   3. 选一个待修产品，向所有维修方发 CFP（招标）
 *   4. 用 ContacterRepairCafe 行为处理响应
 *
 * 【skill 字段】用户的"自己修"能力等级 0-3：
 *   - 0 = 完全不会
 *   - 3 = 像维修工一样熟
 *   这个值决定用户倾向自己修还是找智能体修。
 *
 * @author eadam
 */
public class UserAgent extends AgentWindowed {
    /**
     * skill in "repairing" from 0 (not understand) to 3 (repairman like)
     */
    // 【维修技能等级】0-3 随机
    int skill;
    /**
     * list of potential helpers (agent registered in the type of service "repair"
     */
    // 【候选帮助者】查询黄页得到的所有 repair 服务智能体
    List<AID> helpers;

    // 【用户持有的产品】随机抽 3 个
    List<Product> products;

    // 【setup】初始化技能和产品清单
    @Override
    public void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        window.setButtonActivated(true);

        // 【随机生成技能等级】
        skill = (int) (Math.random() * 4);
        println("hello, I have a skill = " + skill);
        helpers = new ArrayList<>();

        // 【从产品目录中随机挑 3 个】
        products = new ArrayList<>();
        var allProducts = Product.getListProducts();
        var nb = allProducts.size();
        for (int i = 0; i < 3; i++) {
            var rand = (int) (Math.random() * nb);
            products.add(allProducts.get(rand));
        }
        println("i have the following products : ");
        for (var p : products) println(p.getName() + " ");
    }

    // 【按钮事件】点击"go"就发起招标
    @Override
    public void onGuiEvent(GuiEvent evt) {
        //I suppose there is only one type of event, clic on go
        //search about repairing agents
        // 【查询黄页】找出所有注册为 "repair" 类型的智能体（不管子服务）
        helpers.addAll(Arrays.stream(AgentServicesTools.searchAgents(this, "repair", null)).toList());

        println("-".repeat(30));
        for (AID aid : helpers)
            println("found this agent : " + aid.getLocalName());
        println("-".repeat(30));

        addCFP();
    }

    /**add a CFP from user to list of helpers*
     * 【发 CFP】用 Contract-Net 协议向所有 helpers 招标
     */
    private void addCFP() {
        // 【构造 CFP】会话 ID = "id"（简单起见就用固定值）
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId("id");
        // 【从用户产品里随机挑一个作为招标对象】
        int randint = (int) (Math.random() * products.size());
        msg.setContent(products.get(randint).getName());

        msg.addReceivers(helpers.toArray(AID[]::new));
        println("-".repeat(40));

        // 【声明 Contract-Net 协议 + 1 秒截止】
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        // 【挂载响应行为】处理维修方的报价
        var contacterRepairCafe = new ContacterRepairCafe(this, msg);
        addBehaviour(contacterRepairCafe);
    }

    /**here we simplify the scenario. A breakdown is about 1 elt..
     * so whe choose a no between 1 to 4 and ask who can repair at at wich cost.
     * *
     * 【空方法】作者预留但没实现——原本打算模拟"某个具体产品坏了"
     */
    private void breakdown() {

    }

    public void println(String s) {
        window.println(s);
    }

    // 【takeDown】下线时告别
    @Override
    public void takeDown() {
        println("bye !!!");
    }
}
