# Planner MVP (Scheduler + StateHub + Organs + AI via thinking.Control)

## 目录结构
- `common/`：统一事件 Envelope、eventType 常量、payload schema、JSON 工具。
- `eventbus/`：
  - `EventBus`：thinking 模块使用的静态事件总线（发布 `AI_EVENT` 用）。
  - `InMemoryEventBus`：StateHub/Organs MVP 的内存事件总线。
- `thinking/`：AI 连接与任务调度（你的 Planner/Brain 实现）。
- `scheduler/control/`：AI 回复到功能动作映射调度器。
- `statehub/`：Netty Server + 影子状态 + 等待池 + 去重 + 租约锁。
- `organs/console/`：EarMouthConsole 器官（console.readLine telemetry + console.print command）。
- `bootstrap/`：启动入口。

## 关键说明
不再使用 `brain` 包。AI 决策能力由 `thinking.Control` 提供。

你只需要向 `EventBus` 发布 topic 为 `AI_EVENT` 的事件：
- 输入：`new Event(Event.Topic.AI_EVENT, new AIEvent(priority, systemPrompt, userPrompt))`
- 输出：`AiScheduler` 会发布 `AI_RESPONSE_EVENT`，随后 `scheduler.control.Scheduler` 将其解析为决策并发布状态事件。

## 启动
### AI 调度链路（推荐，已内置启动 StateHub:9000）
```bash
mvn -q -DskipTests compile
java -cp target/classes bootstrap.CoreMain
```

> `CoreMain` 会保持运行并监听 `9000`，可随后启动 `ConsoleOrganMain` 接入。

### 传统入口（等价）
```bash
java -cp target/classes Main
```

### Console Organ / StateHub 演示（可选）
```bash
java -cp target/classes bootstrap.StateHubMain
java -cp target/classes bootstrap.ConsoleOrganMain
```

## 事件协议
统一 Envelope 字段：
- `eventType`
- `timestamp`
- `correlationId`
- `commandId`
- `source`
- `target`
- `payload`
- `error`
