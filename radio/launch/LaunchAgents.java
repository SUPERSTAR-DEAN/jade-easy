package radio.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/** 【启动器】广播演示：1 广播者（a）+ 99 收听者（sim_1~99）
 *  注意：必须加载 TopicManagementService，否则 generateTopicAID 会失败 */
public class LaunchAgents {
    public static void main(String[] args) {
        // prepare arguments for the Jade container
        Properties prop = new ExtendedProperties();
        // -- add a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // -- add the Topic Management Service
        prop.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
        // -- add the agents
        StringBuilder sb = new StringBuilder("a:radio.agents.BroadcasterAgent;");
        for (int i = 1; i < 100; i++)
            sb.append("sim_").append(i).append(":radio.agents.ListenerAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
