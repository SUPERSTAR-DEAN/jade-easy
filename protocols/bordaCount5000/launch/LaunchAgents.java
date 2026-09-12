package protocols.bordaCount5000.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/** 【启动器】大规模 Borda 投票：1 投票站 + 5000 投票者
 *  注意：智能体数量 >100 时需调大 jade_domain_df_maxresult，否则黄页查询会截断 */
public class LaunchAgents {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        StringBuilder sb = new StringBuilder("bureau:protocols.bordaCount5000.agents.PollingStationAgent;");
        for (int i = 0; i < 5000; i++)
            sb.append("votant_").append(i).append(":protocols.bordaCount5000.agents.ParticipantAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        //si plus de 100 agents, augmenter la taille du domaine de JADE
        profMain.setParameter("jade_domain_df_maxresult", "10000");
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
