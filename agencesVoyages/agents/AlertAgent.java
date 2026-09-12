package agencesVoyages.agents;

import agencesVoyages.gui.AlertGui;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * Journey searcher
 *
 * 【警报智能体（综合案例版）】对应 [voyagesEnVille/agents/AlertAgent](../../voyagesEnVille/agents/AlertAgent.java)。
 *
 * 职责：
 *   - 创建"TRAFFIC NEWS"广播 topic
 *   - 用户在窗口输入"起点-终点"后，广播一条警报
 *   - 所有订阅该 topic 的智能体（旅行社、旅客）都会收到
 *
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class AlertAgent extends GuiAgent {
    /**
     * code pour ajout de livre par la gui
     */
    public static final int EXIT = 0;
    /**
     * code pour achat de livre par la gui
     */
    public static final int ALERT = 1;

    /**
     * liste des vendeurs
     */
    protected AID[] vendeurs;

    /**
     * topic on which the alert will be send
     */
    AID topic;

    /**
     * gui
     */
    private AlertGui window;

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        this.window = new AlertGui(this);
        window.setColor(Color.orange);
        window.println("Hello!  Agent d'alertes " + this.getLocalName() + " est pret. ");
        window.setVisible(true);

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = topicHelper.createTopic("TRAFFIC NEWS");
            topicHelper.register(topic);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        window.println("Je quitte la plateforme. ");
    }

    ///// SETTERS AND GETTERS

    /**
     * @return agent gui
     */
    public AlertGui getWindow() {
        return window;
    }

    /**
     * get event from the GUI
     */
    @Override
    protected void onGuiEvent(final GuiEvent eventFromGui) {
        if (eventFromGui.getType() == AlertAgent.EXIT) {
            doDelete();
        }
        if (eventFromGui.getType() == AlertAgent.ALERT) {
            ACLMessage alert = new ACLMessage(ACLMessage.INFORM);
            var start = (String) eventFromGui.getParameter(0);
            var stop = (String) eventFromGui.getParameter(1);
            alert.setContent(start + "," + stop);
            alert.addReceiver(topic);
            send(alert);
            println("j'ai envoyé une alerte de pb entre " + start + " et " + stop + "...");
        }
    }

    /**
     * print a message on the window lined to the agent
     *
     * @param msg text to display in th window
     */
    public void println(final String msg) {
        window.println(msg);
    }


}
