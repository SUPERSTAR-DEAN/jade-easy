# Jade : 智能体

## 邮件箱中消息过滤的示例

---

- [SenderAgent](https://github.com/EmmanuelADAM/jade/blob/english/ticTac/AgentPosteur.java) ：
  一个智能体类的代码，它有 2 种通信行为：
    - 一个循环行为，每 1000ms 发送一条标记为 "CLOCK" 的消息
    - 一个延迟行为，10000ms 后发送一条标记为 "BOOM" 的消息
- [DeminerAgent](https://github.com/EmmanuelADAM/jade/blob/english/ticTac/AgentDemineur.java) ：
  一个智能体类的代码，它从邮件箱中取出不同类型的消息（CLOCK、BOOM）。
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/protocoles/voteBorda/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建 2 个智能体。

---
下面是两个智能体之间的通信图。
<!--
```
@startuml tictac
participant sender
participant deminer
group TickerBehaviour : TicTacBehaviour [each seconde]
  sender -> deminer  : "TicTac"
  deminer -> deminer : display "tictac"
end

group WakerBehaviour : [in 10 secondes]
    sender -> deminer: "boom"
    deminer -> deminer : display "alert!"
    sender -> sender : remove 'TicTacBehaviour'
end

@enduml```
-->

![](tictac.png)

### 给初学者的提示

**"消息过滤"是学习 `MessageTemplate` 的最佳例子**。

前面模块大多直接用 `receive()` 收任意消息；这里演示如何用过滤器**精确收某类消息**：
- `MessageTemplate.MatchPerformative(ACLMessage.INFORM)` → 按 performative 过滤
- `MessageTemplate.MatchSender(aid)` → 按发送者过滤
- `MessageTemplate.MatchContentPattern("CLOCK")` → 按内容匹配

**动手实验**：
1. 打开 [ticTac/](..) 源码，找到 `DeminerAgent` 里的 `MessageTemplate` 用法
2. 尝试把过滤器改成同时匹配 `CLOCK` 和 `BOOM`
3. 思考：如果两个 `receive()` 用同一过滤器，会发生什么？（消息争抢）
