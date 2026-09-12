# Jade : 智能体、窗口与通信

## 通过广播发布信息的示例

---
### Jade 智能体编程课程材料


---

- [BroadcasterAgent](https://github.com/EmmanuelADAM/jade/blob/english/radio/agents/BroadcasterAgent.java) ：
  一个**广播**智能体，它在一个无线电频道上广播信息
- [ListenerAgent](https://github.com/EmmanuelADAM/jade/blob/english/radio/agents/ListenerAgent.java) ：
  一个**收听**智能体，它连接到一个无线电频道，收听并显示消息
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/english/radio/launch/LaunchAgents.java) ：**主类**，
  启动 Jade 并创建智能体

- 初始启动 10 个智能体：1 个发射器、9 个收听者。数量不限，只受机器能力限制。

广播通信的示意图如下：
<!--
```
@startuml broadcasting
participant Sim_1
participant Sim_2
participant Sim_3
participant Sim_4
participant Sim_5
Sim_1 ->> TopicServer: registerTo 'BAC'channel
Sim_2 ->> TopicServer: registerTo 'BAC'channel
Sim_3 ->> TopicServer: registerTo 'BAC'channel
Sim_4 ->> TopicServer: registerTo 'BAC'channel
Sim_5 ->> TopicServer: registerTo 'BAC'channel
TopicServer <- BroadcasterAgent : "Hello !" to 'BAC' channel
Sim_1 -> TopicServer: "Hello !"
Sim_2 <- TopicServer: "Hello !"
Sim_3 <- TopicServer: "Hello !"
Sim_4 <- TopicServer: "Hello !"
Sim_5 <- TopicServer: "Hello !"

@enduml```
-->


![](broadcasting.png)

### 给初学者的提示

**"广播"（topic）和"点对点消息"的区别**：

| 方式 | 收件人 | 用途 |
|---|---|---|
| 点对点（前面所有模块） | 明确指定地址 | "我要跟 x 说话" |
| 广播（本模块） | 频道里的所有人 | "我要跟所有人说" |

**JADE 广播的实现**：通过 `TopicManagementHelper` 服务，先建一个 topic，
收听者用 `subscribeToTopic()` 订阅，广播者用 `sendTopicMessage()` 发布。

**动手实验**：
1. 打开 [BroadcasterAgent.java](agents/BroadcasterAgent.java) 和 [ListenerAgent.java](agents/ListenerAgent.java)，
   对比 `sendTopicMessage` 和 `subscribeToTopic`
2. 建**两个不同频道**，看同一个收听者能否同时订阅两个频道
3. 广播场景的经典应用：聊天室、警报系统、行情推送——都是"一对多实时同步"
