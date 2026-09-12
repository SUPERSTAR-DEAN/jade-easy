# Jade : 智能体

## 注册服务并使用"黄页"的示例

### Jade 智能体编程课程材料

---

- [HelloAgent](https://github.com/EmmanuelADAM/jade/blob/english/HelloWorldService/agents/HelloAgent.java) ：
  一个与图形窗口关联的智能体类
    - 该智能体注册到"cordiality"类型的服务中，随机注册到"receptiondesk"或"lobby"子服务
    - 该智能体可以向"receptiondesk"或"lobby"服务中的**所有智能体**发送消息
        - 为此，它向黄页询问某个服务里的智能体列表
    - 该智能体监听并显示收到的消息
  - [SimpleGui4Agent](https://github.com/EmmanuelADAM/jade/blob/english/HelloWorldService/gui/SimpleGui4Agent.java) ：
    *GuiAgent* 的一个小 Java Swing 窗口

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/helloWorldService/launch/LaunchAgents.java)
  ：**主类**，启动 Jade 并创建智能体

- 启动时创建 10 个智能体，数量不受限制（除受机器能力限制外）。

下面是智能体之间交换的消息示例：

<!--
```
@startuml helloService

participant sim1 #pink
participant sim3 #pink
participant sim5 #pink
participant "YellowPages(DF)" as df #yellow
participant sim2 #cyan
participant sim4 #cyan
participant sim6 #cyan

sim1 ->> df: 注册到 "cordiality-lobby"
df <<- sim2: 注册到 "cordiality-receptiondesk"
sim3 ->> df: 注册到 "cordiality-lobby"
df <<- sim4: 注册到 "cordiality-receptiondesk"
sim5 ->> df: 注册到 "cordiality-lobby"
df <<- sim6: 注册到 "cordiality-receptiondesk"
...
sim1 ->> df: 询问 "cordiality-receptiondesk" 成员
df -- >> sim1 : [sim2, sim4, sim6]
sim1 -> sim2: "hello !"
sim1 -> sim4: "hello !"
sim1 -> sim6: "hello !"
@enduml```
-->


![](helloService.png)

### 给初学者的提示

这个例子是"黄页服务发现"的关键。核心三步：

1. **注册服务**（`AgentServicesTools.register(this, "cordiality", "lobby")`）——让黄页知道自己提供了什么服务
2. **查询服务**（`AgentServicesTools.searchAgents(this, "cordiality", "receptiondesk")`）——按服务类型查找到同类智能体的地址列表
3. **按组群发**（`msg.addReceivers(...)`）——把消息发给查到的所有智能体

**动手实验**：
1. 把 `Math.random() < 0.5` 改成固定 `true`，看所有智能体都会注册到同一子服务
2. 尝试新增一个子服务类型（如 `"cordiality/frontdesk"`），看能否按新服务发消息
3. 关闭窗口前调用 `deregisterAll()` 是否必要？（观察：注销后再查询，还会不会被找到？）
