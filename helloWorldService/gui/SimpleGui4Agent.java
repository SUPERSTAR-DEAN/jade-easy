package helloWorldService.gui;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * a simple window for a Jade GuiAgent with two texts areas to display
 * informations
 *
 * 【GuiAgent 的自定义窗口模板】这是 JADE 自定义 GUI 的经典模式，值得记住。
 *
 * 核心要点：
 *   1. 继承 `JFrame`，实现 `ActionListener`
 *   2. 定义**事件码常量**（QUITCODE / SENDLOBBY / ...）——用 int 而不是字符串传给 GuiEvent
 *   3. 定义**命令字符串常量**（QUITCMD / SENDCMDLOBBY / ...）——按钮的 actionCommand
 *   4. 在 `actionPerformed()` 里把用户点击转成 `new GuiEvent(this, 事件码)` 并 `myAgent.postGuiEvent(ev)`
 *   5. 智能体那边覆写 `onGuiEvent(GuiEvent ev)` 处理
 *
 * 这套模式在整个仓库里被大量复用（voyagesEnVille、agencesVoyages 都有类似 GUI 类）
 *
 * @author emmanuel adam
 * @version 1
 */
public class SimpleGui4Agent extends JFrame implements ActionListener {
    /**
     * code associated to the Quit button
     */
    // 【退出按钮的事件码】
    public static final int QUITCODE = -1;
    /**
     * code associated to the "send to lobby" button
     */
    // 【"发给 lobby"按钮的事件码】
    public static final int SENDLOBBY = 1;
    /**
     * code associated to the "send to reception desk" button
     */
    // 【"发给 receptiondesk"按钮的事件码】
    public static final int SENDRECEPTIONDESK = 2;
    /**
     * string associated to the Quit button
     */
    // 【退出按钮的命令字符串】
    private static final String QUITCMD = "-1";
    /**
     * string associated to the send to lobby button
     */
    // 【"发给 lobby"按钮的命令字符串】
    private static final String SENDCMDLOBBY = "1";
    /**
     * string associated to the send to reception desk button
     */
    // 【"发给 receptiondesk"按钮的命令字符串】
    private static final String SENDCMDRECEPTIONDESK = "2";
    /**
     * nb of windows created
     */
    // 【已创建窗口数】用来在屏幕上错开排列窗口
    static int nb = 0;
    /**
     * Low Text area
     */
    // 【下方输入框】用户在这里输入要发送的消息
    public JTextField lowTextArea;
    /**
     * Main Text area
     */
    // 【主显示区】显示收到的消息和智能体状态
    public JTextArea mainTextArea;
    /**
     * monAgent linked to this frame
     */
    // 【关联的智能体】
    GuiAgent myAgent;
    /**
     * no of the window
     */
    // 【本窗口的序号】
    int no;

    /**
     * creates a window and displays it in a free space of the screen
     */
    // 【默认构造】根据窗口序号自动错开位置，避免所有窗口重叠
    public SimpleGui4Agent() {
        final int preferedWidth = 500;
        final int preferedHeight = 300;
        no = nb++;

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        // 【网格排列】(no * width) % screenW = 每行排满后换行
        int x = (no * preferedWidth) % screenWidth;
        int y = (((no * preferedWidth) / screenWidth) * preferedHeight) % screenHeight;

        setBounds(x, y, preferedWidth, preferedHeight);
        buildGui();
        setVisible(true);
    }

    // 【关联智能体的构造】标题显示智能体名字
    public SimpleGui4Agent(GuiAgent agent) {
        this();
        myAgent = agent;
        setTitle(myAgent.getLocalName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * build the gui : a text area in the center of the window, with scroll bars
     */
    // 【构建界面】用 BorderLayout 分三区：南=输入框，中=滚动显示区，北=按钮组
    private void buildGui() {
        getContentPane().setLayout(new BorderLayout());
        // 【中央显示区】
        mainTextArea = new JTextArea();
        mainTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(mainTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);
        // 【下方输入框】
        lowTextArea = new JTextField();
        jScrollPane = new JScrollPane(lowTextArea);
        getContentPane().add(BorderLayout.SOUTH, jScrollPane);

        // 【按钮区】
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(0, 3));
        // (just add columns to add button, or other thing...
        // 【退出按钮】
        JButton button = new JButton("--- QUIT ---");
        button.addActionListener(this);
        button.setActionCommand(QUITCMD);
        jpanel.add(button);
        // 【发给 lobby 按钮】
        button = new JButton("SEND LOBBY");
        button.addActionListener(this);
        button.setActionCommand(SENDCMDLOBBY);
        jpanel.add(button);
        // 【发给 reception 按钮】
        button = new JButton("SEND RECEPTION");
        button.addActionListener(this);
        button.setActionCommand(SENDCMDRECEPTIONDESK);
        jpanel.add(button);

        getContentPane().add(BorderLayout.NORTH, jpanel);
    }

    /**
     * add a string to the main text area
     */
    // 【在显示区追加一行】
    public void println(final String chaine) {
        String texte = mainTextArea.getText();
        texte = texte + chaine + "\n";
        mainTextArea.setText(texte);
        // 【自动滚动到最新行】
        mainTextArea.setCaretPosition(texte.length());
    }

    /**
     * add a string to a text area  (main parameter is no more used)
     * @param chaine text to add
     * @param main if true text is added to the main text area, if false, text is set in the small text area
     *
     * 【追加文本：可选区域】main=true 追加到显示区，false 覆盖输入框内容
     */
    public void println(final String chaine, final boolean main) {
        if(main)println(chaine);
        else {
            lowTextArea.setText(chaine);
        }
    }

    /**
     * reaction to the button event and communication with the agent
     *
     * 【按钮点击处理】核心：把 Swing 事件转成 GuiEvent 发给智能体
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        final String source = evt.getActionCommand();
        // 【只处理我们定义的三个命令】
        if (source.equals(QUITCMD) || source.equals(SENDCMDLOBBY) || source.equals(SENDCMDRECEPTIONDESK) ) {
            // 【把字符串命令转成 int 事件码】
            GuiEvent ev = new GuiEvent(this, Integer.parseInt(source));
            // 【发送给智能体】智能体那边覆写 onGuiEvent 处理
            myAgent.postGuiEvent(ev);
        }
    }

}
