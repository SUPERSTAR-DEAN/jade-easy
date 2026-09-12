# Jade : 智能体与状态行为

## FSM：有限状态机

---

Jade 智能体编程课程材料

- [AgentHelloEuropeenFSM](https://github.com/EmmanuelADAM/jade/blob/master/fsm/salutations/AgentHelloEuropeenFSM.java) ：
  一个智能体，它把 6 个问候行为按**有限状态机**的方式组织起来。
  - 从行为 A（初始状态）开始，然后根据 A 的返回结果执行 B 或 C
  - 根据 B 的返回值，执行 D 或 E
  - C 结束、D 结束都指向 E
  - E 走向 F（终态）或者回到 A

<!--
```
@startuml fsmSalutations
!pragma layout smetana
hide empty description

[*] -> A
state choiceFromA <<choice>>
A -- > choiceFromA
choiceFromA -- > B
choiceFromA -- > C
state choiceFromB <<choice>>
B -- > choiceFromB
choiceFromB -- > D
choiceFromB -- > E
C-> E
D -- > E
state choiceFromE <<choice>>
E -- > choiceFromE
A <-- choiceFromE
choiceFromE -> F
F -> [*]

@enduml```

-->
<img src="fsmSalutations.png" alt="reseau v2" height="400"/>

### 给初学者的提示

FSM 是把"多个行为的执行顺序"从代码里剥离出来的关键工具。**动手必做**：

1. **在 `AgentHelloEuropeenFSM.setup()` 里逐行读状态和转移**，对照右侧的状态图，把每个转移连到图上的箭头
2. **理解 `registerTransition` 的第三个参数**——它是 `onEnd()` 返回值的匹配条件
3. **删掉 `fsm.registerTransition("E", "A", 1, new String[]{...})` 里最后那个 `String[]` 参数，重跑**——观察 FSM 会不会卡住
4. **把 `onEnd()` 里的随机数换成固定值**，看状态路径变成什么形状
