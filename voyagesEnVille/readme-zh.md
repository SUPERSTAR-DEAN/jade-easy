# Jade : 智能体（Agents）

## 城市之旅（Voyages en ville）

---

这些代码演示了一个小型的案例研究，用来练习 ContractNet（合同网）协议。

一个旅行者（voyageur）希望从点 `a` 前往点 `b`：

- 他向多家旅行社（agences de voyages）发起招标（appel d'offre），
- 其中一些旅行社专门经营自行车（vélos）、公交车（bus）、火车（trains）或汽车（voitures）。
- 这些旅行社发送它们可提供的旅行目录（catalogues）
- 客户做出选择，并可以根据自己的标准（成本、时间、CO2 排放量……）组合不同的报价


- 已经提供了一些类：
    - [AgenceAgent](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/agents/AgenceAgent.java) 类
      表示一家旅行社。一家旅行社拥有一个旅行目录，它根据
      一个 csv 文件（bus.csv、car.csv 或 train.csv）来创建该目录。
    - [TravellerAgent](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/agents/TravellerAgent.java) 类
      表示客户，他发起招标并做出选择 *（需要修改为“按耗时选择”）*，
    - [AlertAgent](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/agents/AlertAgent.java) 类
      表示一个能够发出无线电呼叫（broadcast，广播）的智能体，用于报告某一路段（tronçon）上的警报。
    - [LaunchSimu](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/launch/LaunchSimu.java) 类会启动
      一个客户、3 个专业旅行社智能体，以及 1 个警报智能体。
    - [ContractNetVente](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/comportements/ContractNetVente.java) 类
      编写了响应招标的行为，它只是简单地发送完整的旅行目录，
      这个行为还可以被优化。
    - [ContractNetAchat](https://github.com/EmmanuelADAM/jade/blob/master/voyagesEnVille/comportements/ContractNetAchat.java) 类
      编写了发起并管理一次招标的行为。

- 在 [voyagesEnVille.data](https://github.com/EmmanuelADAM/jade/tree/master/voyagesEnVille/data) 包中，有
  那些构造可能路线（chemins）的类。
- 在 [voyagesEnVille.gui](https://github.com/EmmanuelADAM/jade/tree/master/voyagesEnVille/gui) 包中，有
  那些构造与智能体对话窗口的类。

  
代码使用了
- opencsv 库
- json 库
- jadeUPHF 库。
- 这些库位于 [external-libraries](https://github.com/EmmanuelADAM/jade/tree/english/external-libraries) 目录中

-----
代码可以直接运行，但客户只能在所提议的旅行中选择耗时最短的一个。

可以改进的代码：
- 每个旅行可售座位数的递减：
  - 可以假设每趟汽车行程有 3 个座位，每个区域有 20 辆可用自行车，每辆公交车有 50 个座位，每辆电车有 200 个座位，
  - 当某个旅行被选中时，可用座位数会递减，
  - 如果可用座位数为零，该旅行就不再被提供。
  - 自行车的特殊情况：使用一辆自行车时，出发区域的自行车数递减，并在到达区域递增 **在到达日期之后（它将在预计到达日期之后重新可用）**
- 考虑天气（météo）：
  - 如果下雨和/或风非常大，则不提供自行车行程，
  - 如果风大，自行车行程的耗时增加 50% 后被提供，
  - 如果下雪，汽车行程的耗时增加 50%、成本增加 20% 后被提供。
    - **根据城市的天气**，traveller 智能体的提议应该相应地调整。
- 提出一个 **收到某个路段上的警报消息后做出反应** 的行为：
    - 对于旅行社（移除受影响的行程），
    - 对于客户（如果受影响，则重新发起一次行程请求，购买能够补全行程的车票）。
- 或者其他改进……
-----

### 交通网络


供参考，可能的行程如下：

- 在自行车道（véloroute，下图中绿色）上，在 a--b、b--c、c--d、d--e、e--f 区域之间
- 乘坐公交车（下图中黑色），在 a--b、b--c、b--d、c--e 和 e--f 区域之间，
- 乘坐电车（下图中蓝色），在 a--d、d--f 区域之间，
- 乘坐汽车（拼车 covoiturage）（下图中红色），在 a--f、c--f 区域之间
- 成本、速度、co2 排放量、舒适度……取决于所用的交通工具以及气候（下雨、刮风……）


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
A <--[#green]> B
A <-- > B
A <-[#blue]> D
A <-[#red]> F
B <-[#green]> C
B <-> C
B <-> D
B <-[#green]> D
C <--[#green]> D
C <-> E
C <-[#green]> E
C <-[#red]> F
D <-[#green]> E
D <-[#blue]> F
D <-[#green]> F
E <--[#green]> F
E <-- > F


@enduml 
```

-->

<img src="trajetsV1.png" alt="reseau v1" height="300"/>


---

