package protocols.bordaCount.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/** 【启动器】Borda 投票：1 投票站（bureau）+ 50 投票者（votant_0~49） */
public class LaunchAgents {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        StringBuilder sb = new StringBuilder("bureau:protocols.bordaCount.agents.PollingStationAgent;");
        for (int i = 0; i < 50; i++)
            sb.append("votant_").append(i).append(":protocols.bordaCount.agents.ParticipantAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        //si plus de 100 agents, augmenter la taille du domaine de JADE
        //profMain.setParameter("jade_domain_df_maxresult", "10000");
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
