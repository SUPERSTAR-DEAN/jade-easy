# Jade : 智能体与窗口

## 通过对话框窗口直接通信的基础示例

---

Jade 智能体编程课程材料

这里的智能体都是 `AgentWindowed` 类型，它们有一个简单的窗口（`SimpleWindow4Agent`）用于显示消息。
 - 这个窗口还有一个可激活的按钮。
 - 点击按钮会向智能体发送一个 `GuiEvent` 类型的事件（`SimpleWindow4Agent.OK_EVENT`，值为 1）
 - 关闭窗口会向智能体发送一个 `GuiEvent` 类型的事件（`SimpleWindow4Agent.QUIT_EVENT`，值为 -1）
 - 默认情况下，事件反应函数 `protected void onGuiEvent(GuiEvent ev)` 在关闭窗口时会终止智能体


- [SenderAgentWithWindow](https://github.com/EmmanuelADAM/jade/blob/english/window/agents/SenderAgentWithWindow.java) ：
  类型为 *AgentWindowed* 的智能体
    - 对按钮点击做出响应，向 3 个智能体 "b"、"c" 和 "d" 发送一条简单的文本消息
- [ReceiverAgentWithWindow](https://github.com/EmmanuelADAM/jade/blob/english/window/agents/ReceiverAgentWithWindow.java) ：
  类型为 *AgentWindowed* 的智能体
    - 智能体有一个循环行为，在其窗口上显示收到的消息
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/english/window/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建智能体

- 启动时创建 4 个智能体："a" 能发消息；"b"、"c"、"d" 等待消息

---

### 给初学者的提示

**这是学习 `AgentWindowed` 的入口**。对比 `Agent`（无窗口）和 `AgentWindowed`（带窗口）：

- `Agent`：只在控制台输出，适合大规模（5000+）智能体
- `AgentWindowed`：每个智能体有自己的 GUI 窗口，可以**接收按钮点击、关闭窗口等用户操作**，适合小规模演示

**关键代码模式**：
1. `window = new SimpleWindow4Agent(...)` 在 `setup()` 里创建
2. 覆写 `onGuiEvent(GuiEvent ev)` 处理用户交互
3. 在 `takeDown()` 里调用 `window.dispose()` 关闭窗口

**动手实验**：
1. 运行本模块，看到 4 个窗口，尝试点"发送"按钮
2. 关闭某个接收者窗口，看它是否从平台上消失
3. 修改 `onGuiEvent` 让它响应不同按钮
