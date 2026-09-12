# Jade : 智能体与行为（Behaviours）

## FSM 常规、串行与并行行为

---
### Jade 智能体编程课程材料

---

- [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/blob/english/behaviorTests/AgentHelloSalut.java) ：一个拥有 3 种行为的智能体：
  - 一个每隔 300ms 打印 "hello" 且永不结束的行为
  - 一个每 500ms 激活一次的循环（cyclic）行为
  - 一个 2000ms 后触发、导致智能体被移除的延迟行为
  - 初始启动 2 个智能体

<!--
```
@startuml helloSalut

start
while (While agent alive) is (ok)
if (activatable behavior ?) then ([select next behavior])
    fork
    partition "Behaviour" {
      partition "action" {
          ::display "Hello everybody";
          :pause 300ms;
      }
      partition "done" {
          :return False;
      }
    }
    fork again
    partition "CyclicBehaviour: each 500ms" {
      partition "onTick" {
          ::display "Hi !";
      }
    }
    fork again
    partition "WakerBehaviour: in 2000ms" {
      partition "onWake" {
          ::delete Agent;
      }
    }
    end fork
 else(no)
 endif
 endwhile (deleted)
stop

@enduml```
-->

![](helloSalut.png)

- [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/blob/english/behaviorTests/AgentHelloEuropeenParallel.java) ：
  启动 2 个智能体，每个智能体的行为**并行**执行。这些行为可以被激活 3 次，并打印欧洲各国语言的问候语。
- 结果显示：JADE 会在智能体及其行为之间共享资源，各个行为获得相同的调度优先级。

- 以下是并行执行时行为的视角图。


<!--
```
@startuml HelloEuropeenParallel

start
while (While agent alive) is (ok)
if (activatable behavior?) then ([choose a next behavior])
    fork
    partition "EuropeanBehaviour A" {
      partition "action" {
          :display "bonjour"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
    fork again
    partition "EuropeanBehaviour B" {
      partition "action" {
          :display "hallo"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
    fork again
    partition "EuropeanBehaviour C" {
      partition "action" {
          :display "buongiorno"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
    end fork
else (no)
 endif
 endwhile (deleted)
stop

@enduml```
-->

![](HelloEuropeenParallel.png)

```
a1 -> bonjour (1/3)
a2 -> bonjour (1/3)
a1 -> hallo (1/3)
a2 -> hallo (1/3)
a1 -> buongiorno (1/3)
a2 -> buongiorno (1/3)
a1 -> buenos dias (1/3)
a2 -> buenos dias (1/3)
a1 -> Olá (1/3)
a2 -> Olá (1/3)
a1 -> saluton (1/3)
a2 -> saluton (1/3)
a1 -> bonjour (2/3)
a2 -> bonjour (2/3)
a1 -> hallo (2/3)
a2 -> hallo (2/3)
a1 -> buongiorno (2/3)
a2 -> buongiorno (2/3)
a1 -> buenos dias (2/3)
a2 -> buenos dias (2/3)
...
```

- [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/blob/english/behaviorTests/AgentHelloEuropeenSequentiel.java) ：
  启动 2 个智能体，每个智能体的行为**串行**执行。这些行为可以被激活 3 次，并打印欧洲各国语言的问候语。
- 结果显示：JADE 在智能体之间共享资源，行为获得相同的优先级。但这一次，对每个智能体来说，
  同一个行为会被一直调用直到它完成，其余行为按声明顺序依次执行。

- 以下是串行执行时行为的视角图。
<!--
```
@startuml HelloEuropeenSequentiel

start
while (While agent alive) is (ok)
  if (EuropeanBehaviour A
  exists) then (true)
    partition "EuropeanBehaviour A" {
      partition "action" {
          :display "bonjour"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
  elseif (EuropeanBehaviour B
exists) then (true)
    partition "EuropeanBehaviour B" {
      partition "action" {
          :display "hallo"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
  elseif (EuropeanBehaviour C
exists) then (true)
    partition "EuropeanBehaviour C" {
      partition "action" {
          :display "buongiorno"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove
        behaviour;
      else (false)
      endif
      }
    }
  endif
  endwhile (deleted)
stop

@enduml```
-->

![](HelloEuropeenSequentiel.png)

```
a1 -> bonjour (1/3)
a2 -> bonjour (1/3)
a1 -> bonjour (2/3)
a2 -> bonjour (2/3)
a2 -> bonjour (3/3)
a1 -> bonjour (3/3)
a2 -> hallo (1/3)
a1 -> hallo (1/3)
a2 -> hallo (2/3)
a1 -> hallo (2/3)
a2 -> hallo (3/3)
a1 -> hallo (3/3)
a2 -> buongiorno (1/3)
a1 -> buongiorno (1/3)
a2 -> buongiorno (2/3)
a1 -> buongiorno (2/3)
a2 -> buongiorno (3/3)
a1 -> buongiorno (3/3)
a2 -> buenos dias (1/3)
a1 -> buenos dias (1/3)
a2 -> buenos dias (2/3)
a1 -> buenos dias (2/3)
a2 -> buenos dias (3/3)
a1 -> buenos dias (3/3)
a2 -> Olá (1/3)
...
```

### 给初学者的提示

本模块是理解 JADE 调度模型的关键。**并行 vs 串行**是行为机制的核心区别，务必动手验证：

1. **跑 `AgentHelloEuropeenParallel` 和 `AgentHelloEuropeenSequentiel`，对比输出顺序**
   - 并行：`bonjour / hallo / buongiorno` 交错出现
   - 串行：一个行为 3 次跑完，才轮到下一个
2. **理解 `done()` 的含义**：`done()` 返回 true → 该行为被移除；返回 false → 下次还可选它
3. **理解 `block()` vs `doWait()`**：
   - `block()` 让出 CPU，收到消息会立刻唤醒
   - `doWait(毫秒)` 是硬等待，不管有没有消息都睡满
4. **思考题**：如果 `done()` 返回 false 且 `action()` 里没有 `block()`，会发生什么？（会空转烧 CPU）

**下一站**：[fsm](../fsm/) 模块介绍有限状态机（FSM）行为——把串行/并行行为组织成状态图。
