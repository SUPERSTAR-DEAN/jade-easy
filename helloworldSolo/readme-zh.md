# Jade : Agents

## 基础示例：JADE 中的"你好世界"

---

Jade 智能体编程课程材料

- [AgentHello](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHello.java) ：最简单的智能体，
  在控制台打印一条消息（消息写死在代码里）
- [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHelloParametre.java) ：
  同样是在控制台打印消息的智能体，但消息内容作为**创建参数**传入

---

### 给初学者的提示

这两个文件是 JADE 的最小可运行单元。运行方式：在 IDE 中打开 `AgentHello.java`，
点 `main` 方法上方的 ▶ Run，会启动 JADE 平台并弹出控制窗口。

**建议动手实验**：
1. 把 `AgentHello` 的 `doDelete()` 删掉，观察智能体是否会一直留在平台上（看 JADE GUI 的 Agents 面板）
2. 在 `AgentHelloParametre` 里再创建第三个智能体，看不同参数如何产生不同输出
3. 对比两个类的 `main()`，理解 `-agents 名字:类名(参数);` 这种命令行格式
