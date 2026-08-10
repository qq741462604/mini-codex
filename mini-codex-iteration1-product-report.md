# mini-codex 迭代一产品报告

## 一、项目概述

mini-codex 是一个面向企业 Java 项目的代码改造 Agent。系统接收需求后，先做 skill 匹配，再创建本次任务专用上下文，随后驱动规划、工具执行、验证和结果汇总，最终输出代码变更结果。

## 二、迭代一目标与成果

本次迭代完成了以下基础能力：

1. 支持单条需求和批量需求串行执行。
2. 通过 skill 驱动改造，未知需求直接兜底返回，不进入执行链路。
3. 目标工作区与 mini-codex 自身目录分离，避免误改助手工程。
4. 代码修改引入文件保护机制，降低覆盖和越界风险。
5. 验证阶段形成闭环，能返回本次执行的变更和错误信息。

## 三、核心流程

1. 调用方请求 `/agent/run`。
2. `AgentController` 判断是单条请求还是批量请求。
3. `SkillMatcher` 判断需求是否命中可执行 skill。
4. 命中后进入 `Agent` 执行链路并开始 trace。
5. `AgentContextFactory` 创建本次运行上下文。
6. `AgentRuntime` 和 `AgentExecutor` 驱动 `AgentLoop`。
7. `Planner` 生成工具计划，`ToolExecutor` 执行文件和搜索工具。
8. `VerifyEngine` 对结果进行验证。
9. `CodeChangeExtractor` 汇总代码变更并返回。

## 四、关键代码流程

### 1. 请求入口

- [AgentController.java](src/main/java/com/minicodex/controller/AgentController.java)
- [AgentBatchService.java](src/main/java/com/minicodex/service/AgentBatchService.java)

`AgentController` 是统一入口。它先判断是否为批量请求，再用 `SkillMatcher` 判断当前任务是否有可执行 skill。批量任务由 `AgentBatchService` 串行处理，任一任务失败即停止后续任务。

### 2. Agent 运行入口

- [Agent.java](src/main/java/com/minicodex/agent/Agent.java)
- [AgentContextFactory.java](src/main/java/com/minicodex/agent/AgentContextFactory.java)

`Agent.run()` 只负责两件事：启动和结束 trace，以及把任务交给运行链路。  
`AgentContextFactory` 负责创建新的 `AgentContext`，初始化：

- `agentId`
- `task`
- `trace`
- `phase`
- `memories`
- `observations`
- `skills`

这样每次运行都是独立上下文，不会把上一次任务的数据带到下一次。

### 3. 执行与阶段流转

- [AgentRuntime.java](src/main/java/com/minicodex/runtime/AgentRuntime.java)
- [AgentExecutor.java](src/main/java/com/minicodex/runtime/AgentExecutor.java)
- [AgentLoop.java](src/main/java/com/minicodex/runtime/AgentLoop.java)
- [PhaseManager.java](src/main/java/com/minicodex/agent/phase/PhaseManager.java)

`AgentExecutor` 负责把 `AgentLoop` 的执行结果转换成最终响应。  
`AgentLoop` 是核心状态机，按阶段在分析、编码、验证、修复、完成之间流转。它会：

- 让 `Planner` 生成工具计划
- 让 `ToolExecutor` 执行工具
- 让 `ProjectIndexer` 重新整理项目索引
- 让 `VerifyEngine` 在验证阶段检查结果

### 4. 工具执行与保护

- [ToolExecutor.java](src/main/java/com/minicodex/runtime/ToolExecutor.java)
- [WorkspaceService.java](src/main/java/com/minicodex/workspace/WorkspaceService.java)
- [ExecutionGuard.java](src/main/java/com/minicodex/guard/ExecutionGuard.java)

工具执行阶段不是直接改文件，而是先经过工作区解析和保护判断。核心约束包括：

- 路径统一落到 `codex.workspace.root`
- `patch_file` 必须先读文件
- 创建文件不能覆盖已有文件
- 工具执行结果会同步进 `observations`

### 5. 验证与结果汇总

- [VerifyEngine.java](src/main/java/com/minicodex/verify/VerifyEngine.java)
- [CodeValidator.java](src/main/java/com/minicodex/verify/CodeValidator.java)
- [CodeChangeExtractor.java](src/main/java/com/minicodex/agent/result/CodeChangeExtractor.java)

验证阶段负责检查修改后的 Java 文件和变更结果。最终返回内容包含：

- 成功或失败状态
- 错误信息
- 本次执行观察结果
- 本次代码变更清单 `changes`

## 五、关键技术点

### 1. Skill 驱动

需求必须先命中 skill，未命中直接兜底返回，避免无约束的模型开发。这让系统从一开始就具备可控的业务边界。

### 2. 上下文工厂化

`AgentContext` 不做单例 bean，而是由工厂每次创建，避免任务状态串扰，也让上下文字段扩展集中在一个位置处理。

### 3. 阶段式执行

`AgentLoop` 按分析、编码、验证、修复、完成阶段流转，便于控制执行边界，也方便后续插入新的阶段策略。

### 4. 工作区隔离

所有目标项目操作通过 `WorkspaceService` 解析，限制在 `codex.workspace.root` 内，避免误操作 mini-codex 自身工程。

### 5. 文件保护

`patch_file` 需要先读文件，`create_file` 不能覆盖已有文件。这个约束把危险文件操作前置成规则，而不是事后补救。

### 6. 验证闭环

工具结果、验证错误和代码变更统一沉淀到上下文和最终响应，便于追踪一次任务到底改了什么、失败在哪里。

## 六、技术架构价值

这一版的重点不是堆功能，而是把 Agent 的执行链路收拢成可控、可追踪、可扩展的结构。这样后续新增业务 skill、扩展工具集或调整验证策略时，改动面更小，风险更可控。

## 七、下一步方向

后续可以继续增强以下能力：

- skill 热加载
- 批量任务摘要
- 失败分类
- 执行统计
- 管理界面

这些能力会让项目从“可用”走向“可运营”。

