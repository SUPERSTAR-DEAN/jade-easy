# Jade : 智能体与协议

## FIPA Contract-Net 协议示例

---
### Jade 智能体编程课程材料
#### 密封英式拍卖


--- 

这里有一个通过著名的 Contract-Net 协议 [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) 通信的示例。
"**第一价格密封投标**"是一种只有一轮的英式拍卖，事实上就是一种简单的招标。
提出最有趣报价的参与者赢得拍卖。<br>
"最好"报价如何定义，由你来决定。


- 一个 [AuctioneerAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AuctioneerAgent.java) 智能体向 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AgentParticipant.java) 智能体发起投标征询
- 协议要求：
    - 参与者必须回应（拒绝、错误、报价）
    - 拍卖方：
        - 选择一个报价并通知：
          - 中标者（如有），告诉他们已被选中
          - 其他参与者，他们的报价被拒绝
    - 每位中标者（如存在）确认或取消自己的报价

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch/LaunchAgents.java) *主类*，启动 Jade 并创建智能体
    - 创建 1 个拍卖方和 10 个参与者。
    - 每次点击 'go' 按钮，拍卖方发起一次拍卖……
    - 参与者可以决定拒绝，不提交出价

#### ContractNetInitiator 行为的状态图
![](ContractNetInitiator.png)

#### ContractNetResponder 行为的状态图
![](ContractNetResponder.png)

#### 序列图示例：拍卖方与若干参与者之间的通信
![ContractNetExchanges.png](ContractNetExchanges.png)

---
<!-- for plantuml, remove the space between - and > in the arrows
```
@startuml ContractNetInitiator
!pragma layout smetana

hide empty description
state CreateCFP : nb receivers\ni<-0
[*] -- > CreateCFP
CreateCFP -- > WaitMsg
WaitMsg-- >handleRefuse : refuse
WaitMsg-- >handlePropose : propose
state c <<choice>>
handleRefuse -- > c:i<-i+1
handlePropose -- > c:i<-i+1
c- > WaitMsg:i<nb

c--- > handleAllResults:[i==nb]\nall results


handleAllResults -- > sendReject : nbr\n nb rejets
handleAllResults -- > sendAcceptation :nba\n nb acceptations
sendAcceptation -- > handleConfirm
sendAcceptation -- > handleFailure 
state c2 <<choice>>
sendReject -- > c2
handleFailure -- > c2
handleConfirm -- > c2
c2 -- > [*] : nbr + nba = nb

@enduml```
-->  

<!--
```
@startuml ContractNetResponder
!pragma layout smetana

hide empty description
state HandleCFP 
[*] -- > HandleCFP
state c1 <<choice>>
HandleCFP -- > c1
c1 -- > SendRefuse
SendRefuse -- > [*]
c1 -- > SendProposal
SendProposal -- > HandleRejectProposal
SendProposal -- > HandleAcceptProposal
state c2 <<choice>>
HandleAcceptProposal-- >c2
c2-- >SendConfirmation : ok
c2-- >SendFailure : pb (no more stock, change my mind, ...)
SendConfirmation -- > [*]
SendFailure -- > [*]
HandleRejectProposal -- > [*]
@enduml```
-->


<!--
```
@startuml ContractNetExchanges
!pragma layout smetana
participant auctioneer as a #FFEEBB
participant participant1 as p1
participant participant2 as p2
participant participant3 as p3
participant participant4 as p4
a- > p4 : book "how to be a millionaire with artificial agents ?"
a- > p3
a- > p2
a- > p1
a<-p4 : 100€
a<[#AA0000]-p3  : refuse 
a<-p2 : 60 €
a<-p1 : 89 € 
a<-a : choice
a-[#AA0000]>p2 : reject proposal
a-[#AA0000]>p1 : reject proposal
a-[#0000AA]>p4 : accept proposal
a<-p4 : confirm

@enduml```
-->

---
你可以修改本页代码来设计一个"经典"的英式拍卖：

- 拍卖方：
  - 提出一个带有初始标价的物品
  - 等待出价
  - 用最好的出价重新提出该物品
  - 如此反复，直到有出价被提出

 ---

### 给初学者的提示

**"密封英式" vs "Vickrey" vs "经典英式" 三种拍卖的对比**：

| 拍卖类型 | 轮数 | 谁赢 | 支付多少 |
|---|---|---|---|
| 密封英式（本模块） | 1 | 出价最高者 | 自己的出价 |
| Vickrey | 1 | 出价最高者 | **第二高**的出价 |
| 经典英式 | 多轮 | 最后一轮最高者 | 自己最后一次的出价 |

**动手实验**：
1. 找出 `AuctioneerAgent.handleAllResponses()` 里选中标者的代码，改一下规则（比如改为最低价中标）
2. 让参与者随机决定"是否参拍"，观察拒绝率
3. 阅读 `ContractNetInitiator.png` 状态图，把状态对应到 JADE 代码里的哪个方法
