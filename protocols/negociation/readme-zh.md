# Jade : 智能体与谈判

## 基础示例：一对一谈判

---

在 [PingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/) 代码的基础上，两个智能体
卖方（vendeur）和买方（acheteur）围绕价格进行谈判。

扩展以下类，完整编码谈判过程（考虑双方达成同意、拒绝、以及轮数超限的情况）。


- [Negociateur](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/negociation/Negociateur.java) ：
  一个智能体的代码，如果它叫 acheteur 则拥有 CompAcheteur 行为，如果它叫 vendeur 则拥有 CompVendeur 行为。
  - 谈判者有一个不愿突破的**阈值**
  - 以及一个理想的**基础价格**
  - 卖方主动发起谈判

- [CompAcheteur](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/negociation/CompAcheteur.java) ：
  买方行为的代码。
  - 它等待一个报价：
    - 如果报价不可接受，就拒绝（对方报价 > 阈值）
    - 如果报价可接受，就同意（对方报价 <= 自己上次发出的报价）
    - 否则提出一个新的报价，比之前的略高
  - 行为在同意、拒绝、或达到轮数上限时结束

- [CompVendeur](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/negociation/CompVendeur.java) ：
  卖方行为的代码。
    - 它等待一个报价：
        - 如果报价不可接受，就拒绝（对方报价 < 阈值）
        - 如果报价可接受，就同意（对方报价 >= 自己上次发出的报价）
        - 否则提出一个新的报价，比之前的略低
    - 行为在同意、拒绝、或达到轮数上限时结束


---
**基于 FSM（有限状态机）创建一个协议**

修改本目录的代码，以下是步骤顺序：
- 发起者提出一个价格，
- 提出的价格成为消息接收方的报价
- 该接收方：
  - 如果报价接近自己的理想价格，则同意
  - 如果报价超过阈值（对买方来说太高，对卖方来说太低），则拒绝
  - 发出反报价，把一个价格返回给伙伴

<!-- remove space between -- and > in the arrows to render the diagram in plantuml
```
@startuml negociation
State Initiateur{
[*] -> I:proposer
state IattenteOffre <<fork>>
I:proposer -- > IattenteOffre
state choixInitiateur <<choice>>
IattenteOffre -- > I:Etudier
I:Etudier -- > choixInitiateur : offre
choixInitiateur -- > I:rejet : [offre<seuil]
choixInitiateur -- > I:accord : [offre ±= prix désiré]
choixInitiateur -- > I:marchander : [seuil<offre<prix désiré]
state ITraiterAccord
state ITraiterRejet
}

State Répondeur{
I:proposer -> R:Etudier : prix
state choixRepondeur <<choice>>
R:Etudier -- > choixRepondeur : offre
choixRepondeur -- > R:rejet : [offre>seuil]
choixRepondeur -- > R:accord : [offre ±= prix désiré]
choixRepondeur -- > R:marchander : [prix désiré<offre<seuil]
R:marchander -- > IattenteOffre : prix
state RTraiterAccord
state RTraiterRejet
}
ITraiterAccord<-R:accord
ITraiterRejet<-R:rejet
I:accord-- >RTraiterAccord
I:rejet-- >RTraiterRejet
@enduml```
-->

<img src="negociation.png" alt="reseau v2" height="883"/>

---

### 给初学者的提示

这个模块是**自己动手实现协议**的机会——之前 [requests/](../requests/) 和 [bordaCount/](../bordaCount/) 都是
用 JADE 内置协议，这里则要**从零实现**一套"讨价还价"的协商机制。

**关键学习点**：
1. **CompAcheteur 和 CompVendeur 是 Behaviour 子类**，各自有独立的 action() / done()
2. **状态判断公式**：
   - 买方：`对方出价 <= 我的理想价` → 同意；`对方出价 > 我的上限` → 拒绝；否则还价
   - 卖方：`对方出价 >= 我的理想价` → 同意；`对方出价 < 我的下限` → 拒绝；否则还价
3. **对比阅读**：
   - [pingPong/negociation/](../../pingPong/negociation/)：线性讨价还价（1% 每次让步）
   - 本模块：有**阈值 + 理想价**的双端谈判
4. **FSM 改造**：readme 里的状态图给出了用 FSM 重写这个谈判的思路——尝试动手实现
