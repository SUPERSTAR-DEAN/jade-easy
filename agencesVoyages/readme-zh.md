# Jade : 智能体

## 旅行社

---

这些代码通过操作 Contract-Net 协议演示了一个小案例研究。

一位旅行者想从点 `a` 到点 `b`：

- 他向多家旅行社发出招标请求
- 其中一些专门经营公交、火车或汽车
- 这些旅行社发送它们可能的行程目录
- 客户做出选择，并根据标准（成本、时间、CO2 排放、……）组合不同的报价


- 已提供以下类：
    - [AgenceAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AgenceAgent.java) 类
      代表一家旅行社。一家旅行社有一个行程目录，它从 CSV 文件（bus.csv、car.csv 或 train.csv）中生成。
    - [TravellerAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/TravellerAgent.java) 类
      代表客户，它发出招标并进行选择，**需编码**
    - [AlertAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AlertAgent.java) 类
      代表一个能发出无线电警报（广播）通告某路段问题的智能体。
    - [LaunchSimu](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/launch/LaunchSimu.java) 类启动
      一个客户、3 个专业化旅行社智能体和 1 个警报智能体。
    - [ContractNetVente](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetVente.java) 类
      实现响应招标的行为，它只是发送完整的行程目录，可以被优化。
    - [ContractNetAchat](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetAchat.java) 类
      实现发起和管理招标的行为。

- 包 [agencesVoyages.data](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/data) 中包含
  构建可能路径的类
- 包 [agencesVoyages.gui](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/gui) 中包含
  构建与 agencesVoyages.agents 的对话框窗口的类

代码使用：

- opencsv 库（[http://opencsv.sourceforge.net](http://opencsv.sourceforge.net)）3.9 版，附在此文件夹中
- jadeUPHF.jar 库，请从此处下载："[JadeUPHF.jar](https://github.com/EmmanuelADAM/JadeUPHF/blob/master/JadeUPHF.jar)"

-----
代码原样可运行，但客户只能在提案的行程中选择最短时长的那个。

- **提出并编码** 每个行程的座位数递减逻辑（一辆车每次 3 个座位，一辆公交 50 个座位，一列火车每次 200 个座位）
- **提出并编码** 收到某路段警报后的反应行为：
    - 对旅行社（受影响行程的移除）
    - 对客户（若受影响则重新发起行程请求，购买完成行程所需的机票）

> 若代码正确 => +5 分

-----

<span style='color:red'>**拍卖、选择**：</span>

- **拍卖**：一个因道路封锁而受影响的客户，可能会持有无法退款的已购票。
    - 对每张已购票：
        - 他发起一个荷兰式拍卖，从购买价开始，递减直到 1€。若无买家，该票被放弃
        - 或者他发起一个英式拍卖（一轮版**简单**但不加额外分），或 n 轮经典版：
          拍卖随着每次叫价逐步上升，直到没有更多叫价为止，出价最高者胜出
    - 旅行者有时可能不得不放弃行程。提出对其窗口的改造，让他能发起拍卖
        - 一个行程可能突然变得非常抢手。一个旅行者可能决定**转卖行程**而非亲自使用以获利。实现一个一轮的升序拍卖（参见 Vickrey 拍卖）

> 若拍卖代码正确 => +5 分（每种其他拍卖类型 +4 分）
>
> 若修改客户窗口的代码正确 => +3 分

---

作为参考，可能的行程如下：

- 公交（图中黑色），在 a<->b、b<->c、b<->d、c<->d、c<->e、d<->e、e<->f 之间
- 火车（图中蓝色），在 a<->d、d<->f 之间
- 汽车（拼车，图中红色），在 a<->f、c<->f 之间
- 成本、速度、CO2 排放、舒适度……取决于所用交通工具


<!-- note, pour plantUml, ci-dessous retirer les espaces entre des tirets -- et le signe >
```
@startuml trajetsV1
hide empty description
rectangle A
rectangle B
rectangle C
rectangle D
rectangle E
rectangle F
A <-- > B
A <-[#blue]> D
B <-> C
B <-> D
C <-- > D
C <-> E
D <-> E
D <-[#blue]> F
A <-[#red]> F
C <-[#red]> F
E <-- > F


@enduml
```

-->

<img src="trajetsV1.png" alt="reseau v2" height="300"/>



---

## 其他旅行社

- 添加另外两家旅行社：一家公交使用 busAutre.csv 的行程，另一家汽车使用 carAutre.csv 文件
- 创建一个 PortailAgence 类。门户旅行社类表现得像旅行社，但没有交通方式。客户不再直接向旅行社发请求，而是向门户发请求
- 创建一个 PortailBus 智能体，作为客户与公交之间的中介，以及一个 PortailCar 智能体，为汽车旅行社做同样工作。火车旅行社智能体把自己定义为旅行社……

- 通过门户的购买会传递给下游的旅行社

> 若门户代码正确 => +5 分

以下是新的行程图（第二家汽车旅行社为橙色，第二家公交旅行社为灰色）

<!-- note, pour plantUml, ci-dessous retirer les espacesentre deux tirets -- et le signe >
```
@startuml trajetsV2
hide empty description
rectangle A
rectangle B
rectangle C
rectangle D
rectangle E
rectangle F
A <-- > B
A <--[#grey]> B
A <-[#blue]> D
B <-> C
B <-[#grey]> C
B <-> D
B <-[#grey]> D
C <-- > D
C <--[#grey]> D
C <-> E
C <-[#grey]> E
D <-> E
D <-[#grey]> E
D <-[#blue]> F
A <-[#red]> F
C <-[#red]> F
E <-- > F
B <--[#orange]> F
A <--[#orange]> E


@enduml
```

-->

<img src="trajetsV2.png" alt="reseau v2" height="300"/>


---
### 需提交的报告

代码提交时附一份简短的报告，包含：
- 一个类图，包含智能体类和它们的行为
- 详细展示不同模式的序列图：
    - 常规行程搜索
    - 通过"门户"旅行社的搜索
- 所提拍卖模式的状态图

你可以参考展示 TD 代码的示例图；若你想用该工具创建图，也可参考相关的 PlantUML 代码。


---

## 信任

**对服务的信任**

- 添加对路网的一种**信任**概念……
    - 对每个警报，添加一个信任值（对"A-B 问题"警报的信任值……，对"E-F 问题"的信任值）
    - 当一个警报被采信时，受影响行程被临时从目录中移除（接收或发送的目录）
    - 对警报的信任会随时间衰减：可假设该信息已过时
    - 每次"tick"，对警报的信任衰减：
      - $confiance_A \gets confiance_A \times \epsilon$，其中 $\epsilon \in [0,1]$
      - $\epsilon = 0$ 表示智能体不相信任何警报
      - $\epsilon = 1$ 表示智能体从不质疑警报
      - 若 $confiance_A < seuil$，则 $confiance_A \gets 0$
      - 若 $confiance_A \leq 0$，则警报 $A$ 从智能体的知识中被移除
    - 一个被提议使用某条轴的购买者会决定是否冒险使用这条轴，这取决于他对该轴警报的信任
      （通过随机抽取，若数值低于信任值，则警报被视为真实）
    - 旅行社同样会根据对警报的信任度，决定是否在目录中提供相应行程

> 若"对服务的信任"代码正确 => +5 分

**对另一方的信任**

- 对旅行社添加一个 0 到 10 的**评价**概念
    - 智能体现在可以有勇敢、谨慎或中性三种性格
    - 勇敢者不参考评价，无论旅行社评分如何，都选择最有趣的报价
    - 谨慎者主要根据评价而非报价的吸引力来做选择（比例为 90/10）：
      如果要求舒适且有一个低评价旅行社提供的汽车行程，旅行者可能会选择高评价旅行社提供的公交
    - 中性者对评价和报价吸引力采用 50/50 的比例来做选择

> 若"对另一方的信任"代码正确 => +5 分

---

### 给初学者的提示

这是本仓库**最综合**的案例——一个真正的期末大作业，把前面所有模块的技术串在一起：
- Contract-Net 协议（[voyagesEnVille/](../voyagesEnVille/) 已实现类似案例）
- 广播 / 频道（[radio/](../radio/)）
- 拍卖（[sealedEnglishAuction/](../protocols/sealedEnglishAuction/)、[vickrey/](../protocols/vickrey/)）
- 信任机制（**原创**：把"感知随时间衰减"应用到多智能体场景）

**建议路径**：
1. 先跑通 voyagesEnVille，它是最简版
2. 再回来看这里的三个附加任务：座位递减 / 拍卖 / 信任
3. 尝试只做"对服务的信任"，这是最能体现"多智能体感知"的一个任务
