<meta name="description" content="用 Java 进行多智能体编程：使用更新版的 Jade 平台。Jade 教程材料：通信、协议、投票、服务、行为等" />

# 用 Jade 进行智能体编程

[(网页版)](https://emmanueladam.github.io/jade/)
[(法语版)](https://github.com/EmmanuelADAM/jade/tree/master/)

这是 JADE 平台多智能体编程课程/教程的一组源码。

要运行这些代码，必须导入库 "[JadeUPHF.jar](https://github.com/EmmanuelADAM/JadeUPHF/blob/master/JadeUPHF.jar)"。
这个库是基于 Tilab 官方最后一版 [Jade](https://jade.tilab.com) 的升级版（基于 JAVA 25），并增加了一些功能，使智能体的实现更简单。
该版本使用**虚拟线程**来运行智能体及其行为，从而可以在一台机器上运行大量智能体（多达 10000 个）。

这个新版本的源码和说明可以从这里访问：[JadeUPHF](https://emmanueladam.github.io/JadeUPHF/)。

*该库可以从这里获取：[JadeUPHF.jar](https://github.com/EmmanuelADAM/jade/blob/english/external-libraries/JadeUPHF.jar)。*

_因此，以下代码需要这个库以及 Java >= 25。_

><small>*注意：如果你在 IDE 中启动某个示例时遇到这个错误：*</small>
>><small>*SEVERE: Communication failure while joining agent platform: No ICP active*</small>
>
><small>*原因是你没有关闭上一个 JADE 实例。在启动下一个实例之前，请先完全停止/关闭上一个 JADE 运行。*</small>

----

## 基础：Agent、Behavior 与消息发送

### 没有行为或通信的 Agent

- 测试 JADE 安装的经典 HelloWorld
    - [AgentHello](https://github.com/EmmanuelADAM/jade/tree/english/helloworldSolo/AgentHello.java)：一个在控制台显示消息的简单智能体
    - [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/tree/english/helloworldSolo/AgentHelloParametre.java)：同样是在控制台显示消息的简单智能体，但消息是作为参数传入的

### 带行为的 Agent

- 给智能体添加简单行为的示例
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests)：拥有简单行为的智能体
    - [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests)：启动 2 个拥有**顺序执行**行为的智能体
    - [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests)：启动 2 个拥有**并行执行**行为的智能体

### 通信智能体

- 两个智能体之间通信测试的经典示例：ping-pong
    - [pingPong](https://github.com/EmmanuelADAM/jade/tree/english/pingPong)：启动两个相互通信的智能体。ping 智能体把一个球发给 pong 智能体，pong 再把球传回给 ping，如此往复……
    - [AgentPingPlouf](https://github.com/EmmanuelADAM/jade/tree/english/pingPlouf)：一个发送随机消息并等待响应的智能体……响应将来自白页智能体（AMS），告诉它指定的地址上没有人……
- 带消息过滤的示例：
    - [ticTac](https://github.com/EmmanuelADAM/jade/tree/english/ticTac)：一个智能体发送带有 2 种不同类型标记的消息；另一个智能体接收它们，并根据类型进行不同的处理。

---

## 交互式智能体

- 使用小型图形界面来方便与用户对话
    - [window](https://github.com/EmmanuelADAM/jade/tree/english/window)：与窗口绑定的智能体代码
    - [radio](https://github.com/EmmanuelADAM/jade/tree/english/radio)：演示广播通信的代码。智能体不再针对特定接收者发送，而是发给其他智能体正在监听的一个"频道"

---

## 服务管理

- [helloWorldService](https://github.com/EmmanuelADAM/jade/tree/english/helloWorldService)：每个智能体都与一个窗口绑定。这些智能体通过搜索服务来互相发现，并相互发送简单消息
- [serviceDetection](https://github.com/EmmanuelADAM/jade/tree/english/serviceDetection)：一些智能体陆续向黄页（DF）注册某项服务。另一个智能体订阅了 DF，以便在有智能体注册或注销该服务时收到通知。

---

## 带 FSM 行为的智能体

- 行为按有限状态机（FSM）组织的智能体：[fsm](https://github.com/EmmanuelADAM/jade/tree/english/fsm)。给出了两个示例：
  - [salutations](https://github.com/EmmanuelADAM/jade/tree/english/fsm/salutations)：一个智能体内欧洲问候方式的行为序列，
  - [review](https://github.com/EmmanuelADAM/jade/tree/english/fsm/review)：作者智能体、期刊智能体和三个审稿人智能体之间提交与审阅文章的流程

---

## 交互协议

- [Requests](https://github.com/EmmanuelADAM/jade/tree/english/protocols/requests)：使用 AchieveRE 协议进行请求通信的代码。
- [English Auction](https://github.com/EmmanuelADAM/jade/tree/english/protocols/sealedEnglishAuction)：使用 ContractNet 通信协议模拟密封英式拍卖（1 轮）的代码。
- [Vote Borda](https://github.com/EmmanuelADAM/jade/tree/english/protocols/bordaCount)：演示智能体之间通过 ContractNet 协议进行 Borda 计票投票的代码。
- [Vote Borda 5000](https://github.com/EmmanuelADAM/jade/tree/english/protocols/bordaCount5000)：演示**5000 个智能体**之间通过 ContractNet 协议进行 Borda 计票投票的代码。
- [Vote DoubleBorda](https://github.com/EmmanuelADAM/jade/tree/english/protocols/voteDoubleBorda)：演示智能体之间通过 ContractNet 协议、在需要时进行两轮 Borda 计票投票的代码。

---

## LLM？

- [AgentLLM](https://github.com/EmmanuelADAM/jade/tree/english/ollama)：一个连接到你本地 LLM 进行对话的智能体

---

## ISSIA 23 - 场景

一个基于真实循环经济项目的（非常）小场景：[这里](https://github.com/EmmanuelADAM/jade/tree/english/issia23)
