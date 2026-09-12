package protocols.negociation;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.awt.*;
import java.util.Properties;

import static java.lang.System.out;

/**
 * classe d'agent pour échange entre 2 agents de cette class. l'un s'appelle ping et initie un échange avec l'agent pong.
 *
 * 【谈判者】一个智能体类，根据名字扮演不同角色：
 *   - 名字 = "vendeur"（卖家）→ 挂上 CompVendeur 行为，由它主动发起谈判
 *   - 名字 = "acheteur"（买家）→ 挂上 CompAcheteur 行为，等待卖家的报价
 *
 * 两个实例使用同一个类——这是 JADE 里"参数决定行为"的经典范式。
 * 对比 [pingPong/negociation/](../../pingPong/negociation/) 目录：那个例子分了两个类，
 * 这里则一个类两个角色。
 *
 * @author emmanueladam
 */
public class Negociateur extends AgentWindowed {

    // 【阈值】谈判者不愿突破的心理底线
    double seuil;
    // 【理想价】谈判者理想的成交价
    double prixSouhaite = 1000;
    /**
     * Initialisation de l'agent
     *
     * 【setup】根据智能体名字选择挂载哪个行为
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        // si l'agent s'appelle vendeur,
        // ajout d'un comportement qui enverra le texte 'balle' à l'agent pong dans 10 secondes
        // 【卖家分支】
        if (getLocalName().equals("vendeur")) {
            window.setBackgroundTextColor(Color.lightGray);
            println("Cliquez pour lancer la négociation...");
            prixSouhaite = 100;   // 【卖家的理想价：100】

            var modele = MessageTemplate.MatchConversationId("MARCHE");
            // ajout d'un comportement à 30 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur après 300ms
            // 【挂卖方行为】
            addBehaviour(new CompVendeur(this, modele));
        }

        // 【买家分支】
        if (getLocalName().equals("acheteur")) {
            var modele = MessageTemplate.MatchConversationId("MARCHE");

            prixSouhaite = 60;    // 【买家的理想价：60】

            // ajout d'un comportement à 30 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur après 300ms
            // 【挂买方行为】
            addBehaviour(new CompAcheteur(this, modele));
        }

    }

    protected void println(String str){super.println(str);}

    // 'Nettoyage' de l'agent
    // 【takeDown】下线时打印告别语
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    /**
     * reaction to a gui event
     *
     * 【按钮事件】只有卖家点了按钮才发起谈判；买家没有主动发起路径
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case SimpleWindow4Agent.OK_EVENT -> sendMessages();
        }
    }

    /**
     * send messages to agents b, c & d
     *
     * 【发送开场报价】卖家把理想价发给买家
     */
    private void sendMessages() {
        var msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver("acheteur");                       // 【收件人：买家】
        msg.setContent(String.valueOf(prixSouhaite));       // 【内容：卖家理想价 100】
        msg.setConversationId("MARCHE");                    // 【会话 ID：MARCHE = 市场】
        send(msg);
        println("moi, " + getLocalName() + " je lance la négociation");
    }


    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        // 【程序化启动平台】
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        // 【两个实例】
        prop.setProperty(Profile.AGENTS, "acheteur:protocols.negociation.Negociateur;vendeur:protocols.negociation" +
                ".Negociateur");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }

}
