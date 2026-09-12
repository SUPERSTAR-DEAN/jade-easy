# Jade : 智能体、协议与服务

## 示例：Jade 中的 FIPA Contract-Net 协议

### 应用于 Borda 投票

---

这里有一个通过
[FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) 协议通信的示例，
用于按 Borda 方法投票（投票者给选项评分排序，然后对分数求和）。

- *Borda 投票原理* **~**在*约一个回合内*迭代~**
    - a1 排序为 option1 > option3 > option2
    - a2 排序为 option1 > option3 > option2
    - a3 排序为 option3 > option2 > option1
    - 每个选项按其排名获得分数（在 n 个选项中，第 1 名得 n 分，……，最后一项得 1 分）
    - 这里投票站对分数求和
        - option1 : 3+3+1 = 7 分
        - option2 : 1+1+2 = 4 分
        - option3 : 2+2+3 = 7 分
    - 如果选项之间出现平票，则只在这些选项之间进行新一轮计票（此处是 option1 和 option3）：
      - 保留 a1 的 option1>option3、a2 的 option1>option3、a3 的 option3>option1
      - 这里投票站对分数求和
          - option1 : 2+2+1 = 5 分
          - option2 : 1+1+2 = 4 分 => option1 当选
    - 如果选项再次平票，则随机抽取

---

- 一个
  [AgentBureauVote](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java) 类型的智能体
  对餐厅名称发起投票征询：披萨店、素食、寿司、……
- 向
  [AgentParticipant](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java) 类型的智能体发出
- 协议要求：
    - 接收者必须回应（拒绝、错误、同意），如果同意，回应格式为：
      legumes>pizzeria>sushi>...
    - 投票站：
        - 汇总投票者发送的分数（在 n 个餐厅之间选择时，对每一票，第 1 名得 n 分，第 2 名 n-1 分，……）
        - 把当选选择发给所有投票者（如有平票则发平票选项）
        - 每位投票者确认收到并接受投票结果

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) ：
  **主类**，启动 Jade 并创建智能体
    - 启动时创建 1 个投票站和 5 个参与者。
    - 每次点击 'go'，投票站发起一次投票……

 ---

### 给初学者的提示

**"双重 Borda" vs "普通 Borda" 的区别**：

- 普通 Borda（[bordaCount/](../bordaCount/)）：平票时**重新发起完整投票**
- 双重 Borda（本模块）：平票时**只在平票选项之间**再做一次 Borda 计数

**为什么需要"双重"**：重新发起完整投票会让投票者重新表达偏好，可能得到不同结果；
而在平票选项之间做二次投票，只保留原偏好中与平票相关的部分——更接近"局部裁决"。

**动手实验**：
1. 修改 Restaurant 枚举或候选项排序，观察"平票后二次投票"何时触发
2. 阅读 `PollingStationAgent` 里做二次计票的代码，理解"部分排序"如何计算
