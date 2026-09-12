package voyagesEnVille.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * launch the simulation of travelers and travel agencies
 *
 * 【启动器】城市出行规划综合案例：
 *   1 旅客（client1）+ 4 旅行社（自行车/汽车/公交/电车，各自读 CSV）+ 1 警报智能体
 *   旅行社创建时传入 CSV 路径作为参数，AgenceAgent 在 setup 里读取
 *   必须加载 TopicManagementService 才能使用广播警报
 *   日志输出到 ./simuAgences.xml
 *
 * @author emmanueladam
 */
public class LaunchSimu {

    public static final Logger logger = Logger.getLogger("simu");

    /**
     *
     */
    public static void main(String... args) {

        logger.setLevel(Level.ALL);
        Handler fh;
        try {
            fh = new FileHandler("./simuAgences.xml", false);
            logger.addHandler(fh);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // ******************JADE******************

        // allow to send arguments to the JADE launcher
        var pp = new ExtendedProperties();
        // add the gui
        pp.setProperty(Profile.GUI, "true");
        // add the Topic Management Service
        pp.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        var lesAgents = new StringBuilder();
        lesAgents.append("client1:voyagesEnVille.agents.TravellerAgent;");
        lesAgents.append("agentBike:voyagesEnVille.agents.AgenceAgent(voyagesEnVille/bike.csv);");
        lesAgents.append("agentCar:voyagesEnVille.agents.AgenceAgent(voyagesEnVille/car.csv);");
        lesAgents.append("agentBus:voyagesEnVille.agents.AgenceAgent(voyagesEnVille/bus.csv);");
        lesAgents.append("agentTram:voyagesEnVille.agents.AgenceAgent(voyagesEnVille/tram.csv);");
        lesAgents.append("alert1:voyagesEnVille.agents.AlertAgent");
        pp.setProperty(Profile.AGENTS, lesAgents.toString());
        // create a default Profile
        var pMain = new ProfileImpl(pp);

        // launch the main jade container
        Runtime.instance().createMainContainer(pMain);

    }

}
