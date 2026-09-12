# Jade : 智能体与协议

## FIPA Contract-Net 协议示例

---

### Jade 智能体编程课程材料

---

**应用于 Borda 投票**

---

这里有一个通过 [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) 协议进行通信的示例，
用于"Borda"计票投票：投票者给选项排序评分，然后对分数求和。


- *"de Borda" 投票原理*
  - a1 排序为 option1 > option3 > option2
  - a2 排序为 option1 > option3 > option2
  - a3 排序为 option3 > option2 > option1
  - 每个选项按其排名获得分数（在 n 个选项中，第 1 名得 n 分，……，最后一项得 1 分）
  - 这里，投票站对分数求和
      - option1 : 3+3+1 = 7 分
      - option2 : 1+1+2 = 4 分
      - option3 : 2+2+3 = 7 分
  - 如果出现平票，则在平票选项之间发起新一轮投票（这里就是 option1 和 option3）

---


- 一个 [PollingStationAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java) 智能体对餐厅名称发起投票征询：披萨店、素食、寿司，
- 向
 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java) 智能体发出
- 该协议要求：
    - 协议要求接收者必须回应（拒绝、错误、同意）。
      若同意，回应格式为：vegetables > pizzeria > sushi > ...
    - 投票站：
        - 汇总投票者发送的分数（在 n 个餐厅之间选择时，对每一票，第 1 名得 n 分，第 2 名 (n-1) 分，……）
        - 把当选的选择发给所有投票者（如有平票则发平票选项）
        - 每位投票者确认收到并接受投票结果
    - 如果平票，则在未决选项之间重新发起投票

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建智能体：
    - 启动 1 个投票站和 5 个参与者。
    - 点击 'go' 按钮时投票站开始新一轮投票……

 ---

### 给初学者的提示

这是 Contract-Net 协议的第一课。**动手必做**：

1. **对比 FIPA-Request 和 Contract-Net**：
   - Request 用 `AchieveREInitiator` / `AchieveREResponder`
   - Contract-Net 用 `ContractNetInitiator` / `ContractNetResponder`
   - 消息流：Request 是 `REQUEST→AGREE/REFUSE→INFORM`，Contract-Net 是 `CFP→PROPOSE/REFUSE→ACCEPT/REJECT_PROPOSAL→INFORM`

2. **理解 `msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)` 的作用**：
   - 不设这个字段，`ContractNetResponder` 会认为这不是 Contract-Net 消息，直接忽略
   - 这是 JADE 协议的关键——**协议标识是握手的前提**

3. **平票递归**：`if ((best.toString()).split(",").length > 1)` 触发新投票。
   尝试修改 Restaurant 枚举让所有选项分数相同，看递归会发生什么。

4. **Borda 计数 vs Condorcet vs 双重 Borda**：本目录还有两个变体：
   - [voteCondorcet](../voteCondorcet/)：两两对决
   - [voteDoubleBorda](../voteDoubleBorda/)：两次 Borda 计数
