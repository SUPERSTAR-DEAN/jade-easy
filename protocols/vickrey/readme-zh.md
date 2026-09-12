# Jade : 智能体与协议

## FIPA Contract-Net 协议示例

---
### Jade 智能体编程课程材料
#### Vickrey 拍卖

---

这里有一个通过著名的 Contract-Net 协议 [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) 进行通信的示例，
用于 **Vickrey 拍卖**。
Vickrey 拍卖是一种密封投标的次高价拍卖（SBSPA）：只有一轮，出价最高的智能体赢得拍卖，
但**支付第二高的价格**……


- 一个
 [AuctioneerAgent.java](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/vickrey/agents/AuctioneerAgent.java) 智能体向
 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/vickrey/agents/ParticipantAgent.java) 智能体发起投标征询
- 协议要求：
    - 接收者：必须回应（拒绝、错误、同意）
    - 拍卖方智能体：做出选择并：
      - 向出价被拒的智能体发送拒绝消息
      - 向胜出者发送同意消息
    - 胜出者：确认自己的出价

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch/LaunchAgents.java) ：
  **主类**
  - 创建 1 个拍卖方和 10 个参与者。
  - 每次点击 'go' 按钮，拍卖方发起一次拍卖……
  - 参与者可以决定拒绝，不提交出价

---

### 给初学者的提示

**Vickrey 拍卖的经济意义**：因为支付的是第二高价而不是自己的出价，参与者有动机**如实出价**（说真话是最优策略）。
这是机制设计（mechanism design）的经典案例——设计一个规则让自利者做出集体最优的行为。

**对比阅读**：
- [sealedEnglishAuction/](../sealedEnglishAuction/)：一次密封投标，最高者赢并支付自己的出价
- [vickrey/](../vickrey/)：一次密封投标，最高者赢但支付第二高价
- 两个都用 Contract-Net 协议实现，只是"如何挑选中标者"的规则不同

**动手实验**：
1. 在 Vickrey 里把"支付第二高价"改成"支付自己的出价"，看参与者会不会调整策略
2. 把参与者的出价改成随机，观察谁是"真心想买"（心理价 = 出价）
