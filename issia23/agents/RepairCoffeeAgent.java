package issia23.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.MessageTemplate;

import issia23.behaviours.CafeRepondreUtilisateur;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 【维修咖啡机的智能体】循环经济场景中的维修方。
 *
 * 工作流：
 *   1. 注册为 "repair/coffee" 服务
 *   2. 拥有**少量零件库存**（每种零件的总数 × 5% 数量，每种 2 件）
 *   3. 收到用户的 CFP 招标后，用 CafeRepondreUtilisateur 行为响应
 *      ——如果它有相关零件就报价，否则拒绝
 *
 * @author eadam
 */
public class RepairCoffeeAgent extends AgentWindowed {
    // 【零件库存】
    List<Part> parts;

    // 【setup】注册服务 + 生成库存 + 挂监听行为
    @Override
    public void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        this.window.setBackgroundTextColor(Color.orange);
        println("hello, do you want coffee ?");

        //registration to the yellow pages (Directory Facilitator Agent)
        // 【注册为 repair/coffee 服务】
        AgentServicesTools.register(this, "repair", "coffee");
        println("I'm just registered as a repair-coffee");

        //distributors have 2 examples of some parts
        // 【随机生成库存】总零件数 × 5% 个种类，每种 2 件
        parts = new ArrayList<>();
        var allParts = Part.getListParts();
        var nb = allParts.size();
        var nbStock = (int) (nb * .05);
        for (int i = 0; i < nbStock; i++) {
            var rand = (int) (Math.random() * nb);
            for (int j = 0; j < 2; j++)
                parts.add(allParts.get(rand));
        }
        println("i have the following parts : ");
        for (var p : parts) println(p.getName() + " ");


        // 【挂监听 CFP 的行为】
        addListeningACFP();

    }

    // 【构造监听 CFP 的行为】
    private void addListeningACFP() {

        // 【只处理会话 ID = "id" 的 CFP】与 UserAgent 匹配
        MessageTemplate model = MessageTemplate.MatchConversationId("id");

        // 【CafeRepondreUtilisateur 是核心的 Contract-Net 响应方】
        var attenteDemandeUtilisateur = new CafeRepondreUtilisateur(this, model);

        addBehaviour(attenteDemandeUtilisateur);
    }


    // 【包装 window.println 方便调用】
    public void println(String s) {
        window.println(s);
    }

}
