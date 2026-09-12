# Jade : 智能体与协议

## FIPA Request 协议示例

---
### Jade 智能体编程课程材料

---

---

这里有一个通过 [FIPA Request Interaction](http://www.fipa.org/specs/fipa00026/SC00026H.html) 协议通信的示例。

- [AgentRequestSender](https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/agents/AgentRequestSender.java)
  智能体向 [AgentRequestResponder](https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/agents/AgentRequestResponder.java)
  智能体发送请求（比如加法："34+12+45"），响应者可以**同意**或**拒绝**处理该请求。
- AchieveRE 协议**强制**响应者必须回应（拒绝 / 错误 / 同意），如果同意则必须发回一条结果消息（INFORM）。
- 因此发送方必须规划如何处理这些不同的返回消息。**协议让这种交互变得易于支持**。
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建 10 个智能体：1 个发送方 + 10 个响应方。

发送方的协议状态图：

<!--
```
@startuml RequestInitiator
!pragma layout smetana

hide empty description
state CreateRequest : nb receivers\ni<-0
[*] -- > CreateRequest
CreateRequest -- > WaitMsg
handleRefuse<-WaitMsg : refuse
state c <<choice>>
handleRefuse -- > c:i<-i+1
c-> WaitMsg:i<nb

WaitMsg->handleAgree : agree
state forkAgree   <<fork>>


handleAgree -- > forkAgree
WaitMsg <- handleAgree
WaitMsg -- > forkAgree
forkAgree -- > handleInform : inform msg \nfollowing an agree msg
handleInform -- > c:i<-i+1
c--- > handleAllResults:[i==nb]\nall results


handleAllResults -- > [*]

@enduml```
-->

![](RequestInitiator.png)

响应方的协议状态图：

<!--
```
@startuml RequestResponder

hide empty description
[*] -- > WaitRequest
state answerChoice <<choice>>
WaitRequest-- >answerChoice
answerChoice -- > Refuse
answerChoice -- > NotUnderstood
answerChoice -- > Accept
Accept-- > Inform
Refuse -- > [*]
NotUnderstood -- > [*]
Inform -- > [*]

@enduml```
-->

![](RequestResponder.png)

---

### 给初学者的提示

这个例子是理解"FIPA 协议"的入口——**协议 = 一组约定的消息交换规则**。

- **FIPA-Request** = 请求-响应式交互，JADE 用 `AchieveREInitiator` / `AchieveREResponder` 实现
- **好处**：不用手写"发消息→收消息→判断类型→再发下一条"的循环，框架帮你处理状态机
- **代价**：你必须写"响应者必须回复"的契约——如果你不写 `prepareResultNotification`，框架会报错

**动手实验**：
1. 改 `handleRequest` 里的 `hasard.nextBoolean()` 为固定 `true`，看所有响应者都接受
2. 改 `hasard.nextBoolean()` 为固定 `false`，看 `handleAllResultNotifications` 里的空列表分支被走到
3. 删掉 `prepareResultNotification`，观察框架行为（会怎样？）
4. 打开 [negociation/](../negociation/) 目录——那里是**自己实现**协商协议，与这里"用 JADE 内置协议"形成对比
