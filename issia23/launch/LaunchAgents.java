package issia23.launch;

import issia23.data.Product;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/** 【启动器】循环经济场景：2 用户 + 4 维修咖啡机 + 2 零件店 + 2 分销商
 *  注意：先调用 Product.getListProducts() 初始化产品目录（静态块加载） */
public class LaunchAgents {
    public static void main(String[] args) {
        //creer la liste des produits
        Product.getListProducts();
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++)
            sb.append("user_").append(i).append(":issia23.agents.UserAgent;");
        for (int i = 0; i < 4; i++)
            sb.append("coffee_").append(i).append(":issia23.agents.RepairCoffeeAgent;");
        for (int i = 0; i < 2; i++)
            sb.append("partsStore_").append(i).append(":issia23.agents.SparePartsStoreAgent;");
        for (int i = 0; i < 2; i++)
            sb.append("distributor_").append(i).append(":issia23.agents.DistributorAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);

    }
}
