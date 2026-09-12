package issia23.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 【零件店智能体】循环经济场景中的"卖零件"方。
 *
 * 与 DistributorAgent 的区别：
 *   - DistributorAgent 卖**完整产品**
 *   - 本类卖**零件**（维修方 RepairCoffeeAgent 需要采购）
 *
 * 库存规模差异：
 *   - DistributorAgent：产品总数的 80%
 *   - 本类：零件总数的 60%，每种 3 件
 *   - RepairCoffeeAgent：零件总数的 5%，每种 2 件（维修方库存小）
 *
 * 【注意】本类只初始化库存，没实现"响应 CFP"逻辑——可能是课程作业的一部分
 *
 * @author eadam
 */
public class SparePartsStoreAgent extends AgentWindowed {
    // 【零件库存】
    List<Part> parts;

    // 【setup】注册服务 + 生成库存
    @Override
    public void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        this.window.setBackgroundTextColor(Color.cyan);
        println("hello, do you want a piece of something ?");

        //registration to the yellow pages (Directory Facilitator Agent)
        // 【注册为 repair/SparePartsStore 服务】
        AgentServicesTools.register(this, "repair", "SparePartsStore");
        println("I'm just registered as a Spare Parts Store");


        //distributors have 3 examples of some parts
        // 【随机生成库存】总零件数 × 60% 个种类，每种 3 件
        parts = new ArrayList<>();
        var allParts = Part.getListParts();
        var nb = allParts.size();
        var nbStock = (int) (nb * .6);
        for (int i = 0; i < nbStock; i++) {
            var rand = (int) (Math.random() * nb);
            for (int j = 0; j < 3; j++)
                parts.add(allParts.get(rand));
        }
        println("i have the following parts : ");
        for (var p : parts) println(p.getName() + " ");

    }

}
