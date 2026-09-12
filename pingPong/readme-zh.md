# Jade : 智能体与通信

## 基础示例：JADE 中的 "Ping"-"Pong"

---

Jade 智能体编程课程材料

- [AgentPingPong](https://github.com/EmmanuelADAM/jade/blob/english/pingPong/AgentPingPong.java) ：一个拥有两种通信行为的智能体：
  - 如果智能体名叫 'ping'，它在激活后几秒内向名为 "pong" 的智能体发送一条内容为 "ball" 的消息
  - 无论智能体叫什么名字，它都有一个运行 20 次的行为：
    - 读取邮箱（收消息）
    - 显示收到的消息内容及其发送者
    - 回复给发送方，内容为 "ball-x"，其中 x 是本次回复的序号
  - 该行为在循环一定次数后结束
  - 启动时会创建 2 个智能体：ping 与 pong

下面是玩家智能体的行为逻辑：

<!--
```
@startuml compPingPong

start
while (While agent alive) is (ok)
if (activatable behavior?) then ([yes, select one (the only one here)])
    partition "Behaviour:pingpong" {
      partition "action" {
          :msg <- takeMessage();
          if (msg ≠ none) then ([yes])
            :dsplay msg;
            :answers "ball-" + i;
            :i <- i + 1;
          endif
      }
      partition "done" {
          if (i=20) then ([yes])
            :behavior \nremoved;
          endif
      }
    }

 endif
 endwhile (deleted)
stop

@enduml```
-->
![](compPingPong.png)

下面是智能体之间交换的消息：

<!--
```
@startuml pinpong

Ping -> Pong: ball
Pong -> Ping: ball-1
Ping -> Pong: ball-1
...
Pong -> Ping: ball-20

@enduml```
-->


![](pinpong.png)

---

### 给初学者的提示

这是理解 JADE 消息机制的最佳入口。建议动手做的事：

1. **画出行为循环图**：`action()` 做了什么、`done()` 何时返回 true，两者如何配合决定一个行为的生命周期
2. **改 `step == 20`** 为别的数字，观察对话轮数变化
3. **把 `msg.setConversationId("SPORT")` 删掉**，再看 `MessageTemplate.MatchConversationId("SPORT")` 还能不能匹配上——体会会话 ID 的过滤作用
4. **改 performative**：把 `ACLMessage.INFORM` 换成 `ACLMessage.REQUEST`，观察 MessageTemplate 是否还能匹配（不能，因为模板里写死了 INFORM）

**下一站**：`pingPong/negociation/` 目录下的 [AgentAcheteur](negociation/AgentAcheteur.java) 和 [AgentVendeur](negociation/AgentVendeur.java) 把消息用成了"讨价还价"，是学会"自治决策"的第一步。
