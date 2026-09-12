<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# 用 Jade 编程智能体
## 教程

使用 JADE 平台进行多智能体编程的一组练习。

要运行这些代码，需要导入库 [JadeUPHF.jar](https://github.com/EmmanuelADAM/JadeUPHF/blob/master/JadeUPHF.jar)。
该库是对 Tilab 官方最后版 [Jade](https://jade.tilab.com) 的 **Java 17 更新版本**，
并添加了一些便于智能体实现的功能。

新版源码、库和说明可从这里访问：[JadeUPHF](https://emmanueladam.github.io/JadeUPHF/)。

_需要 Java >= 17。_


----


### 协议

- 拍卖，[englishAuctionOpenCry](https://github.com/EmmanuelADAM/jade/tree/english/td/englishAuctionOpenCry) ：
  在时限未到之前，听取出价；否则把拍卖品交给出价最高者。
- 谈判，[negociationWindow](https://github.com/EmmanuelADAM/jade/tree/english/td/negociationWindow) ：
  一个人通过窗口与一个智能体交互，就购买某产品进行谈判。

### 约束

- 排课，[timeTable](https://github.com/EmmanuelADAM/jade/tree/english/td/timeTable) ：
  一个共享约束求解问题，由老师和分组智能体协作创建课程表。

### 游戏

- TicTacMoe：3 人版井字棋。1 人对 1 人对 1 个智能体。或 1 人对 2 个智能体。
  - 复用 [OXO](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/MCTS/OXO) 的代码嵌入到智能体中。
  - 界面为纯文本以简化。
    - 棋子为 X、O、S
    - 棋盘为 4x4 或 5x3
    - 3 枚棋子连成一线即获胜。

### 市场

- 效用解、社会福利解：
  复用简单投票的代码（如 [BordaCount](https://github.com/EmmanuelADAM/jade/tree/english/protocols/bordaCount)）。
  这次发起方（市场）向每个智能体询问餐厅的效用（打一个分），
  然后选择效用最高的方案或社会福利方案。

### 婚姻

- 稳定婚姻：
  - 每个智能体把自己的特征发给其他智能体。
  - 目标是形成稳定的配对。
  - 每个智能体对其他智能体有一个偏好（基于小指平均长度；目前可以基于这一点筛选，
    没有"小指歧视"）。
  - 集中式方案：一个"婚介"智能体收集所有偏好，应用经典稳定婚姻算法：

```
所有智能体都是单身
当：存在一个人 c 想配对，且仍有向某个伴侣 p 提出申请的意愿（无骚扰：不能两次向同一智能体申请）
    如果 p 是单身：
        (c, p) 组成一对
    否则：
        已存在一对 (c', p)
        如果 p 更喜欢 c 而非 c'：
            (c, p) 组成一对
            c' 变回单身
        否则 (c', p) 保持一对
向智能体通告他们所属的配对。
```
