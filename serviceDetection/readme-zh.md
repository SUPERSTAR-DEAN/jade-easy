# Jade : 智能体

## 服务注册/注销的检测

### Jade 智能体编程课程材料

---

- [IncomingAgent](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/agents/IncomingAgent.java) ：一个智能体类，
  在经过一段时间后会注册到某个服务。它会显示收到的消息。关闭窗口会让智能体下线，并从服务中注销。
- [ScribeAgent](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/agents/ScribeAgent.java) ：
  一个智能体类，它向黄页（Yellow Pages）订阅某服务的注册/注销事件。每当有成员加入或离开时，
  它会把当前群组的完整成员名单广播给群里所有人。
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建智能体

- 初始启动 10 个智能体：1 个 scribe 和 9 个 incoming。

执行示例：
<!--
```
@startuml serviceDetection

participant scribe #cyan
participant "YellowPages(DF)" as df #yellow
participant sim1 #pink
participant sim2 #pink
participant sim3 #pink

scribe ->> df: subscription to \n'get info on "traveller-quiet" service'
...
sim1 ->> df: **register** to \n"traveller-quiet" service
df->> scribe: new member=sim1
scribe -> sim1: "members of the group=[sim1]"
...
sim2 ->> df: **register** to \n"traveller-quiet" service
df->> scribe: new member=sim2
scribe -> sim1: "members of the group=[sim1,sim2]"
scribe -> sim2: "members of the group=[sim1,sim2]"
...
sim3 ->> df: **register** to \n"traveller-quiet" service
df->> scribe: new member=sim3
scribe -> sim1: "members of the group=[sim1,sim2,sim3]"
scribe -> sim2: "members of the group=[sim1,sim2,sim3]"
scribe -> sim3: "members of the group=[sim1,sim2,sim3]"
...
rnote over sim2:leave\nthe platform
sim2 ->> df: **deregister** from \n"traveller-quiet" service
df->> scribe:  member removed=sim2
scribe -> sim1: "members of the group=[sim1,sim3]"
scribe -> sim3: "members of the group=[sim1,sim3]"

@enduml```
-->


![](serviceDetection.png)

### 给初学者的提示

这个例子演示了 JADE 中**主动查询**（`searchAgents`）之外的另一种方式——**被动订阅**（`DFSubscriber`）。

两者的对比：

| 方式 | 何时得到信息 | 适用场景 |
|---|---|---|
| 主动查询（[helloWorldService](../helloWorldService/)） | 调用时才知道 | 需要"现在这一刻"的名单 |
| 被动订阅（本模块） | 有变更时才收到通知 | 需要"跟随群体变化"，如聊天室成员同步 |

**动手实验**：
1. 运行后关掉一个 IncomingAgent 的窗口，看 scribe 和其余智能体收到的"成员列表更新"
2. 打开 [ScribeAgent.java](agents/ScribeAgent.java)，找 `DFSubscriber` 的两个回调，理解"事件驱动"和"轮询"的区别
3. 思考：如果两个智能体几乎同时注册，列表更新会怎样？（JADE 是串行处理事件的，顺序由框架决定）
