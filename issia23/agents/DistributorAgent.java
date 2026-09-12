package issia23.agents;

import issia23.data.Product;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 【产品分销商智能体】循环经济场景中的"卖新品"方。
 *
 * 与 RepairCoffeeAgent / SparePartsStoreAgent 的区别：
 *   - DistributorAgent 卖**完整产品**（用户可能需要买新的）
 *   - SparePartsStoreAgent 卖**零件**（维修方采购用）
 *   - RepairCoffeeAgent 提供**维修服务**
 *
 * 三者都注册为 "repair" 服务，但子服务不同：
 *   - repair/distributor（本类）
 *   - repair/coffee（RepairCoffeeAgent）
 *   - repair/SparePartsStore（SparePartsStoreAgent）
 *
 * 【注意】本类只初始化库存，没实现"响应 CFP"逻辑——可能是课程作业的一部分
 *
 * @author eadam
 */
public class DistributorAgent extends AgentWindowed {
    // 【产品库存】用 Set 因为产品是唯一标识的
    Set<Product> products;

    // 【setup】注册服务 + 生成库存
    @Override
    public void setup() {
        this.window = new SimpleWindow4Agent(getAID().getName(), this);
        this.window.setBackgroundTextColor(Color.GRAY);
        println("hello, do you want a new object  ?");

        //registration to the yellow pages (Directory Facilitator Agent)
        // 【注册为 repair/distributor 服务】
        AgentServicesTools.register(this, "repair", "distributor");
        println("I'm just registered as a Distributor");

        // 【随机生成库存】总产品数 × 80% 个种类
        products = new HashSet<>();
        var allProducts = Product.getListProducts();
        var nb = allProducts.size();
        var nbStock = (int) (nb * .8);
        for (int i = 0; i < nbStock; i++) {
            var rand = (int) (Math.random() * nb);
            products.add(allProducts.get(rand));
        }
        println("i have the following products : ");
        for (var p : products) println(p.getName() + " ");

    }

}
