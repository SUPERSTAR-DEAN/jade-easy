# Jade : 智能体与协议

## FIPA Contract-Net 协议示例

---

### Jade 智能体编程课程材料

---

**应用于 Borda 投票**

---

这里有一个通过 [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) 协议进行通信的示例，
用于"Borda"计票投票：
**5000 位投票者**给选项评分排序，然后对分数求和。

这里的参与者**没有窗口**，只是在控制台显示投票结果。

- *Borda 投票原理*
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

**这个模块是"性能版" Borda 投票**——5000 个参与者，没有 GUI 窗口。

**与 [bordaCount/](../bordaCount/) 的关键差别**：
- bordaCount：5 个参与者，每个用 `AgentWindowed` 建窗口
- bordaCount5000：5000 个参与者，每个改用 `Agent`（无窗口），否则 5000 个窗口会撑爆系统

**动手实验**：
1. 改 `LaunchAgents` 里的参与者数量，测试 100 / 1000 / 5000 / 50000 时平台的资源占用
2. 观察控制台输出速度，理解多智能体系统的规模瓶颈在哪里
