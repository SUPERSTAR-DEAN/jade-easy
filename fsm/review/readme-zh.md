# Jade : 智能体与状态行为

## FSM：有限状态机

---

Jade 智能体编程课程材料

- 在包 [Review](https://github.com/EmmanuelADAM/jade/blob/master/fsm/review/) 中，智能体模拟了**研究论文投稿**的流程：
  - 作者把文章投稿给期刊
  - 期刊收到后把这篇文章发给 3 个审稿人
  - 当收到全部 3 个评分（0 到 2）时：
    - 如果有一个 0，文章被拒
    - 如果全是 2，文章被无修改接受
    - 否则建议作者修改
  - 如果作者同意修改，他把文档退回期刊（回到步骤 2）
  - 否则，他拒绝继续并通知期刊

<!--
```
@startuml fsmReview
!pragma layout smetana

hide empty description

[*] -> A:Submission
A:Submission -- > J:Reception
state JDispatch <<fork>>
J:Reception -- > JDispatch
JDispatch -- > R1:Reviewing
JDispatch -- > R2:Reviewing
JDispatch -- > R3:Reviewing
state JCollect <<fork>>
R1:Reviewing -- > JCollect
R2:Reviewing -- > JCollect
R3:Reviewing -- > JCollect
state resultat <<choice>>
JCollect -- > resultat
resultat -- > J:Refuse
resultat -- > J:Acceptation
resultat -- > J:Corrections
J:Refuse -- > [*]
J:Acceptation -- > [*]
J:Corrections -- > A:Decision
state decision <<choice>>
A:Decision -- > decision
A:ReSubmission <-- decision
A:Abandon <-- decision
J:Reception <-- A:ReSubmission
[*] <- A:Abandon
@enduml```
-->

<img src="fsmReview.png" alt="reseau v2" height="500"/>

### 给初学者的提示

这是 FSM 的**实战**案例：投稿→审稿→修改循环，是学术界每天在发生的事情。

- 3 个审稿人并行动作 → FSM 的 **fork / join**
- 期刊根据分数决定下一步 → FSM 的 **choice（判断）**
- 作者可以退回重改 → FSM 的**回环**

对照 [fsm/review/agents/](agents/) 下三个智能体类（作者 / 期刊 / 审稿人），
看他们如何用 FSM 状态和消息传递协同起来。
