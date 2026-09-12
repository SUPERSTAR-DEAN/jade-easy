package ticTac;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/**
 * class that launch the jade platform and 2 agents (deminer and sender).
 *
 * 【启动器】消息过滤演示：1 发送者（sender，发 CLOCK/BOOM）+ 1 接收者（deminer）
 *
 * @author emmanueladam
 */
public class LaunchAgents extends Agent {

    public static void main(String[] args) {
        // prepare arguments for the Jade container
        Properties prop = new ExtendedProperties();
        // -- add a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // -- add the agents
        prop.setProperty(Profile.AGENTS, "sender:ticTac.SenderAgent;deminer:ticTac.DeminerAgent");
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
