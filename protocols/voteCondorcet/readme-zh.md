# Jade : 智能体与协议

## FIPA Contract-Net 协议示例

---
### Jade 智能体编程课程材料
#### Condorcet 投票

---

这里有一个通过著名的 Contract-Net 协议 [FIPA Contract Net]
(http://www.fipa.org/specs/fipa00029/SC00029H.html) 进行通信的示例，用于
**Condorcet 投票**（投票者给选项排序评分，然后计算两两对决的胜场数）


- *Condorcet 方法* ***在单张选票中***
  - a1 提出 o6 > o4 > o5 > o7 > o1 > o3 > o2
  - a2 提出 o7 > o6 > o3 > o1 > o4 > o2 > o5
  - a3 提出 o1 > o2 > o3 > o4 > o6 > o7 > o5
  - a4 提出 o4 > o7 > o2 > o1 > o6 > o5 > o3
  - a5 提出 o1 > o2 > o4 > o3 > o5 > o7 > o6


- 投票站根据"胜场数 - 负场数"给每个选项赋值。
    - 例如：option1 有 4 次被排在 option2 之前、1 次排在之后，所以对决值 option1 vs option2 = 3
      反过来 option2 vs option1 = -3（在 Condorcet 的原始方案里，不计负场）
    - 下面是对决值的矩阵。
      - option1、option4 和 option7 分别在 5 个其他选项面前获胜


|   - | o1  | o2  | o3  | o4  | o5  | o6  | o7  | 胜场数 | 分数 |
|----:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
|  o1 | 0   |  3  |  3  |  1  | 3   |  1  | -1  |      5       |   10   |
|  o2 | -3  |  0  |  1  | -1  |  3  |  1  | -1  |      3       |   0    |
|  o3 | -3  | -1  |  0  | -1  |  1  | -1  | -1  |      1       |   -6   |
|  o4 | -1  |  1  |  1  |  0  |  5  |  1  |  3  |      5       |   10   |
|  o5 | -3  | -3  | -1  | -5  |  0  | -3  | -1  |      0       |  -16   |
|  o6 | -1  | -1  |  1  | -1  |  3  |  0  | -1  |      2       |   0    |
|  o7 |  1  |  1  |  1  | -3  |  1  |  1  |  0  |      5       |   2    |

   

- 按照 Condorcet 投票原则，胜出的选项是**在每一个其他选项面前都能赢最多系列对决**的那个。
- 如果出现平票，则根据分数（胜场数-负场数）决定赢家
  - 这里 option1 和 option4 都有 10 分，option 7 只有 2 分。
  - 因此需要在前两者之间决定。

- 文献中有许多方案试图解决这类情况
- 这里，我们**只基于平票选项**（此处是 option1 和 option4）创建新的收益矩阵。

| -   | o1  | o2  | o3  | o4  | o5  | o6  | o7  | 胜场数 | 分数 |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
| o1  |  0  |  0  |  0  |  1  |  0  |  0  |  0  |      1       |   1    |
| o2  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o3  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o4  | -1  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   -1   |
| o5  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o6  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o7  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |

  - option1 赢得了最多的系列对决（对 option4 是 1 胜，具体是 3 次 o1>o4 对 2 次 o4>o1），
    因此最终宣告 option1 当选。

- 如果第二阶段之后选项之间仍平票，则在这些选项之间进行随机抽签。

----
### 编码

- 一个 [PollingStationAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteCondorcet/agents/PollingStationAgent.java) 智能体
   对餐厅发起投票征询：披萨店、素食、寿司、……
- 向 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteCondorcet/agents/ParticipantAgent.java) 智能体
- 协议要求：
    - 接收者：必须回应（拒绝、错误、同意），如果同意，回应格式为：
      legumes>pizzeria>sushi>...
    - 投票站：
      - 创建对决矩阵：
          - 把当选选择发给所有投票者（如有平票则发平票选项）
          - 每位投票者确认收到并接受投票结果
    - 如果平票（胜场数和分数都相同），则在未决选项之间重新投票

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch/LaunchAgents.java) ：
  **主类**
    - 创建 1 个投票站和 5 个投票智能体。
    - 每次点击 'go' 按钮，投票站发起一次投票征询……


 ---

### 给初学者的提示

**Condorcet 方法的经济意义**：让选项之间**两两对决**，看谁在整体上最能"打败别人"。
这解决了"Borda 计数可能选出并非多数偏好的选项"的问题。

**三种投票方法对比**：

| 方法 | 计分方式 | 胜者定义 | 平票处理 |
|---|---|---|---|
| Borda | 排名累加 | 总分最高 | 重新投票 |
| Condorcet | 两两对决胜场 | 胜场最多 | 缩小候选集再决 |
| 双重 Borda | 先 Borda，平票时局部 Borda | 局部总分最高 | 随机 |

**动手实验**：
1. 修改 `PollingStationAgent` 里的胜负值计算，理解 `duels gagnés` 与 `points` 的差别
2. 阅读 [voteCondorcet/agents/PollingStationAgentOld.java](agents/PollingStationAgentOld.java)——原作者标注"Old"的版本，对比新旧实现对平票处理的不同
