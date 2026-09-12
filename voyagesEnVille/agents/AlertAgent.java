package voyagesEnVille.agents;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import voyagesEnVille.gui.AlertGui;

import java.awt.*;

/*
 * 【警报智能体】这个类代表一个警报/广播智能体：当某一路段发生问题（如堵车、事故）时，
 * 它通过“话题（topic）”向所有订阅该话题的智能体广播一条警报消息。
 */
/**
 * Journey searcher
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
    // 【setup 方法】启动时创建窗口，并创建/注册“TRAFFIC NEWS”话题以便广播警报
    @Override
    protected void setup() {
        this.window = new AlertGui(this);
        window.setColor(Color.orange);
        window.println("Hello!  Agent d'alertes " + this.getLocalName() + " est pret. ");
        window.setVisible(true);

        TopicManagementHelper topicHelper = null;
        try {
            // 通过 TopicManagementHelper 创建并注册一个名为 "TRAFFIC NEWS" 的广播话题
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = topicHelper.createTopic("TRAFFIC NEWS");
            topicHelper.register(topic);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    /**
     * get event from the GUI
     */
    // 【onGuiEvent 方法】接收 GUI 事件：退出，或把“起点-终点”打包成一条警报消息广播出去
    @Override
    protected void onGuiEvent(final GuiEvent eventFromGui) {
        if (eventFromGui.getType() == AlertAgent.EXIT) {
            doDelete();
        }
        if (eventFromGui.getType() == AlertAgent.ALERT) {
            // 构建一条 INFORM（告知）消息，内容是“起点,终点”，发送到广播话题，所有订阅者都能收到
            ACLMessage alert = new ACLMessage(ACLMessage.INFORM);
            var start = (String) eventFromGui.getParameter(0);
            var stop = (String) eventFromGui.getParameter(1);
            alert.setContent(start + "," + stop);
            alert.addReceiver(topic);
            send(alert);
            println("j'ai envoyé une alerte de pb entre " + start + " et " + stop + "...");
        }
    }


    // 'Nettoyage' de l'agent
    // 【takeDown 方法】关闭窗口、离开平台
    @Override
    protected void takeDown() {
        if(window!=null) {
            window.dispose();
            System.out.println(getLocalName() +   ">>> I leave the platform. ");
        }
    }

    ///// SETTERS AND GETTERS

    /**
     * @return agent gui
     */
    public AlertGui getWindow() {
        return window;
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
