## TD 第一价格公开拍卖

复用谈判代码，将其扩展为一个经典的英式拍卖（First Price, Open Cry）。

- 一个**组织者**（或拍卖师）以一个初始价格提出一个待售物品
- **竞拍者**可以出价，且必须高于上次听到的价格
- 在最后一次听到价格后经过"一段时间"，拍卖停止
- 出价最高者赢得拍卖

<div hidden>
<!--
```
@startuml FPOC
Participant  Organiser#FFFF00
Organiser -> Bidder1: book,100
Organiser -> Bidder2: book,100
Organiser -> Bidder3: book,100
Organiser -> Bidder4: book,100
Organiser <- Organiser : wait\nmax 3s
Bidder1 -> Organiser: 110
Bidder1 -> Bidder2: 110
Bidder1 -> Bidder3: 110
Bidder1 -> Bidder4: 110
Organiser <- Organiser : wait\nmax 3s
Bidder3 -> Organiser: 120
Bidder3 -> Bidder1: 120
Bidder3 -> Bidder2: 120
Bidder3 -> Bidder4: 120
Organiser <- Organiser : wait\nmax 3s
Bidder2 -> Organiser: 150
Bidder2 -> Bidder1: 150
Bidder2 -> Bidder3: 150
Bidder2 -> Bidder4: 150
Organiser <- Organiser : wait\nmax 3s
Organiser -> Bidder1: 已售给 Bidder2，价格 150
Organiser -> Bidder2: 已售给 Bidder2，价格 150
Organiser -> Bidder3: 已售给 Bidder2，价格 150
Organiser -> Bidder4: 已售给 Bidder2，价格 150
@enduml```
-->
</div>

![FistPriceOpenCry](FPOC.png)

### 指南
有多种可能性！
- 最简单的方式：使用一个**广播频道**（例如 "MarketPlace"）。
  这样每个智能体都能收到该频道上广播的所有消息。
  这也最符合 OpenCry 的语义……

- 另一种方案是：拍卖消息到达组织者，然后由组织者通知其他潜在竞拍者。

- 提供了代码草稿，你可以补充完善……

### 给初学者的提示

**对比几种拍卖**：
- [sealedEnglishAuction](../../protocols/sealedEnglishAuction/)：一次性密封投标
- 本作业（open cry）：多轮公开竞价，每次必须高于前一次
- 关键点：**多轮**——这是与密封拍卖最大的区别
