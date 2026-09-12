# Jade : 智能体与通信

## 基础示例："Ping"-"Pong" 出错版

---

Jade 智能体编程课程材料

- [AgentPingPlouf](https://github.com/EmmanuelADAM/jade/blob/english/pingPong/AgentPingPlouf.java) ：一个向
  **平台上根本不存在的智能体**发消息，并且傻等回复的智能体。最终它收到的回复会来自
  "白页服务"（AMS，Agent Management Service），告诉它那个地址上没人……

---

### 给初学者的提示

这个例子刻意演示**错误情况**：目标智能体不存在时会发生什么。

JADE 里有两个"通讯录"，务必分清：
- **AMS（白页 / Agent Management Service）**：管理智能体本身，能查到"哪个地址有哪个智能体"
- **DF（黄页 / Directory Facilitator）**：管理**服务**，智能体把自己注册成某类服务供别人发现

这里查的是"智能体在哪"，所以是 AMS 的事——它回一条 `FAILURE` 消息告诉你地址查无此人。
这正是真实系统里需要处理异常和失败回复的原因。

建议对照 [pingPong](../pingPong/) 模块看正常情况，再回来看这里，对比效果最明显。
