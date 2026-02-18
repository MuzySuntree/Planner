# Planner MVP (Brain + Scheduler + StateHub + Organs)

## 目录结构
- `common/`：统一事件 Envelope、eventType 常量、payload schema、JSON 工具。
- `eventbus/`：in-memory EventBus（BlockingQueue + 多消费者）。
- `brain/`：BrainStub，接收用户命令并生成 PlanSubmitted。
- `scheduler/`：SchedulerEngine，执行计划并追踪 command 生命周期。
- `statehub/`：StateHub，Netty Server + 影子状态 + 等待池 + 去重 + 租约锁。
- `organs/console/`：EarMouthConsole 器官（console.readLine telemetry + console.print command）。
- `bootstrap/`：启动入口。

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

关键事件：
- `brain.userCommand`
- `scheduler.command`
- `organ.result`
- `organ.telemetry`

## 启动
> 推荐两进程：Core(Brain+Scheduler+StateHub) + ConsoleOrgan

### 1) 启动核心
```bash
mvn -q -DskipTests compile
java -cp target/classes bootstrap.CoreMain
```

### 2) 启动器官
```bash
java -cp target/classes bootstrap.ConsoleOrganMain
```

## 演示
在 ConsoleOrgan 的控制台输入：
- `help`
- `echo hello`
- `exit`

观察点：
- Scheduler 打印 `submit -> result/timeout` 生命周期日志。
- StateHub 打印 route、去重、注册信息。
- 程序退出时打印 shadow 快照（desired/reported）。
- commandId/correlationId 在日志链路可跟踪。

## 互斥锁
StateHub 在命令路由时对 `resourceKey=organId` 加简化租约锁，演示同一器官互斥执行。
