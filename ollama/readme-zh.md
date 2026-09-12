# Jade : 智能体（Agents）

## 智能体与 LLM

---

Jade 面向智能体的编程（Agent-Oriented Programming）课程资料

为了让一个智能体使用 LLM 与用户交互，一个好的方法是使用 OLLAMA。

1. 从主页下载 [ollama](https://ollama.com/)
2. 选择一个要下载到电脑上的 LLM（例如 ``ollama pull granite.3.``）
   - *模型列表：* [Ollama Models](https://github.com/ollama/ollama)*
   - [granite3.3](https://ollama.com/library/granite3.3) 是一个不错的模型（免费）
3. 将 ollama 作为服务器启动 ``ollama serve``

你可以尝试智能体 ``AgentLLM``，它只是通过 ollama 连接你的 LLM 来进行聊天……
  - 第一次执行总是很慢（取决于 LLM 和电脑），因为需要加载 LLM

- **TODO（待办事项）**：
- 一个人类与一个使用 LLM 回答的 BlaBla 智能体讨论
 - 讨论内容将围绕去餐厅吃饭
   - BlaBla 智能体将向一个天气智能体（weather agent，你需要构建它）询问天气（参见 Meteo 类）
     - 天气智能体将使用一个 API 来获取天气（见下文）
     - 默认使用巴黎这座城市。你可以在天气智能体中更改它。
     - 天气智能体将以天气性质（非常冷、冷、温和、热、非常热）和温度来回答 BlaBla 智能体
   - BlaBla 智能体获取用户的问题，并向一个 LLM 询问答案，同时带上天气信息
   
**对于 Meteo**：
   - 使用 [OpenWeatherMap API](https://openweathermap.org/api)
   - 在这里创建你自己的密钥（免费但有使用限制）：[获取 API Key](https://home.openweathermap.org/users/sign_up)
   - 并在 Meteo 类中替换该密钥
 

---
