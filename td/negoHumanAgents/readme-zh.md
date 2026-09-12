## TD 智能体与人类之间的谈判

在 [PingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/) 代码的基础上，
卖方和买方智能体围绕一个价格进行谈判。

定义好消息交换，已知卖方主动发起谈判，提出一个价格。

- 卖方拥有：
    - 一个提出的价格
    - 一个阈值（低于此值则终止谈判）
    - 一个终止谈判前的最大轮数

- 买方拥有：
    - 一个提出的价格
    - 一个阈值（高于此值则终止谈判）
    - 一个终止谈判前的最大轮数

- 对买方：
    - 如果轮数超过最大值，回复拒绝；
    - 如果收到的价格高于上限阈值，回复拒绝；
    - 如果收到的价格与自己提出的价格相近，回复确认；
    - 如果价格介于自己提出的价格和阈值之间，买方把初始出价上调 x%。

- 对卖方：
    - 如果轮数超过最大值，回复拒绝；
    - 如果收到的价格低于下限阈值，回复拒绝；
    - 如果收到的价格与自己提出的价格相近，回复确认；
    - 如果价格介于自己提出的价格和阈值之间，卖方把初始出价下调 x%。

看一下已提供的类，然后运行 `Main` 类的 `main` 方法。

**问题 1：**
- 只有 1 个买方是真人，其他都是智能体。
- 添加一个类支持启动多个买方（1 个真人 + n 个智能体）。
  两个买方（1 个真人和 1 个智能体）的序列图示例：
  ![](FPOC.png)

<!--
```
@startuml pinpong
!pragma teoz true
participant  AVendeur
actor PAcheteur
participant  AAcheteur1
participant  AAcheteur2
AVendeur -> PAcheteur: PROPOSE(INIT)-手表,100
&AVendeur -> AAcheteur1: PROPOSE(INIT)-手表,100
& AVendeur -> AAcheteur2: PROPOSE(INIT)-手表,100
PAcheteur ->o AVendeur++: PROPOSE-50
&PAcheteur -- > AAcheteur1: PROPOSE-50
&PAcheteur -- > AAcheteur2: PROPOSE-50
AAcheteur1 ->o AVendeur: PROPOSE-55
&AAcheteur1 -- > AAcheteur2: PROPOSE-55
&AAcheteur1 -- > PAcheteur: PROPOSE-55

AVendeur -> AAcheteur1: PROPOSE-90
&AVendeur -> PAcheteur: PROPOSE-90
&AVendeur -> AAcheteur2--: PROPOSE-90

PAcheteur ->o AVendeur++: 60
PAcheteur -- > AAcheteur1: 60
&PAcheteur -- > AAcheteur2: 60
AAcheteur1 -> AVendeur: 65
AAcheteur1 -- > PAcheteur: 65
&AAcheteur1 -- > AAcheteur2: 65
AVendeur -> AAcheteur1: 80
&AVendeur -> PAcheteur: 80
&AVendeur -> AAcheteur2--: 80

AAcheteur2 ->o AVendeur++: 接受 80
AAcheteur2 -- > AAcheteur1: 接受 80
&AAcheteur2 -- > PAcheteur: 接受 80
AVendeur -- > PAcheteur: 确认 80 成交（对 AAcheteur2）
&AVendeur -- > AAcheteur1: 确认 80 成交（对 AAcheteur2）
AVendeur -> AAcheteur2--: 确认 80 成交（对 AAcheteur2）

@enduml```
-->


**问题 2**
- 买方智能体必须是可配置的（出价、最高价、循环次数、系数）。
    - 那么需要为买方智能体创建一个专门的窗口，并将其与智能体关联。

**问题 3**
- 用于降价和涨价的百分比 $\epsilon$ 应基于初始出价、阈值价格和允许的循环数计算。
    - 例如：基础价 = 100，最高价 = 200，循环数 = 10，那么 $\epsilon$ = 8%：
    - 100, 108, 116, 124, 132, 140, 148, 156, 164, 172, 180, 188, 196

**问题 4**
- 最多有 4 个物品要买，所有买方智能体和真人买方都有 250€ 的固定预算。
- 待售物品的基础价都是 100€。
- 目标是**最大化购买的物品数量**，如出现平票则最大化剩余金额。


