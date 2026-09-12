package ollama.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/** 【启动器】LLM 智能体：1 LLM 智能体（blablaAgent）+ 1 天气智能体（meteoAgent）
 *  前置条件：本机需装 Ollama 并拉取至少一个模型 */
public class LaunchAgents {
    public static void main(String[] args) {
        // prepare arguments for the Jade container
        Properties prop = new ExtendedProperties();
        // -- add a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // -- add the agents
        StringBuilder sb = new StringBuilder();
        sb.append("blablaAgent:ollama.AgentLLM(hello);meteoAgent:ollama.AgentMeteo;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
