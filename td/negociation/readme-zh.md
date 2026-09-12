# Jade : TD1 - 谈判

## 基础示例：一对一谈判

---

在 [PingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/) 代码的基础上，
卖方和买方智能体围绕一个价格进行谈判。

**消息交换如下**，已知卖方主动发起谈判，提出一个价格：

 - 卖方拥有：
   - 一个提出的价格
   - 一个阈值（低于此值则终止谈判）
   - 一个终止谈判前的最大轮数
   - 直接可用 `Scanner` 类读取键盘输入：
   - ```java
       Scanner scanner = new Scanner(System.in);
       System.out.print("请输入你的出价：");
       int prixPropose = scanner.nextInt();
     // 若偏好，也可用浮点数或字符串
     ```

 - 买方拥有：
   - 一个提出的价格
   - 一个阈值（高于此值则终止谈判）
   - 一个终止谈判前的最大轮数

 - 对买方：
   - 如果轮数超过最大值，回复拒绝；
   - 如果收到的价格高于上限阈值，回复拒绝；
   - 如果收到的价格与自己提出的价格相近，回复确认
   - 如果价格介于自己提出的价格和阈值之间，买方把初始出价上调 x%

- 对卖方：
  - 如果轮数超过最大值，回复拒绝；
  - 如果收到的价格低于下限阈值，回复拒绝；
  - 如果收到的价格与自己提出的价格相近，回复确认
  - 如果价格介于自己提出的价格和阈值之间，卖方把初始出价下调 x%

看一下已提供的类，然后运行 `Main` 类的 `main` 方法。

**问题：** 修改现有代码，让用户扮演买方角色；卖方仍是智能体。
- 然后再添加对话框窗口（参考其他示例）。


![谈判原理的状态图](negociation.png)
---

### 给初学者的提示

这是**最原始**的谈判作业版本——用 `Scanner` 从控制台输入。
后续版本（[negociationWindow](../negociationWindow/)、[negociationInteractionWindow](../negociationInteractionWindow/)）逐步升级为 GUI。
建议按顺序做，理解"从控制台到 GUI"的演进过程。
