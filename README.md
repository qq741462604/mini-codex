# mini-codex

`mini-codex` 是一个面向企业 Java 项目的代码改造 Agent。它接收需求后，先做技能匹配和上下文组装，再驱动规划、工具执行、验证和结果汇总，最终输出本次改造涉及的代码变更。

## 迭代一版本目标

本版本重点完成了三件事：

1. 支持单条需求和批量需求串行执行。
2. 通过 skill 驱动改造，未知需求直接兜底返回，不进入执行链路。
3. 将目标项目路径、工具执行、验证和上下文创建收敛到清晰的代码流程里，便于后续扩展。

## Skill 核心设计

skill 是本项目的核心。它把“什么需求可以做、要改哪里、怎么改、不能做什么”提前写成结构化规则，让 Agent 不靠临场猜测，而是按既定 skill 执行。

一个 skill 通常包含：

- `name`：skill 名称
- `keywords`：触发匹配关键词
- `Target`：目标类和方法
- `Rules`：改造约束
- `Implementation`：执行步骤
- `Forbidden`：禁止行为

### Skill 文件排版说明

skill 文件统一写在 `.ai/skills/<skill-name>/skill.md`。推荐排版顺序如下：

```text
name:
keywords:
Target:
class:
method:
Rules:
Implementation:
Forbidden:
```

当前项目里的 skill 写法有几个硬约束：

- `Target` 要明确到真实类和方法。
- `Rules` 要写清楚可做和不可做。
- `Implementation` 要按步骤落地，方便 Agent 按顺序执行。
- `Forbidden` 要直接列出禁止项，避免越权改动。
- 公共 skill 优先只做一类改造，不混写多个无关目标。

## 核心流程

1. 调用方请求 `/agent/run`。
2. `AgentController` 判断是单条请求还是批量请求。
3. `SkillMatcher` 判断需求是否命中可执行的业务 skill。
4. 命中后进入 `Agent` 执行链路，开始 trace。
5. `AgentContextFactory` 创建本次任务专用的 `AgentContext`。
6. `AgentRuntime` 调用 `AgentExecutor`。
7. `AgentLoop` 根据阶段状态驱动 `Planner`、`ToolExecutor` 和 `VerifyEngine`。
8. `ToolExecutor` 执行 `search_code`、`read_file`、`patch_file`、`write_file` 等工具。
9. `CodeChangeExtractor` 汇总代码变更，返回给调用方。

## 关键代码流程

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
- [QwenPlanner.java](src/main/java/com/minicodex/planner/QwenPlanner.java)

`AgentExecutor` 负责把 `AgentLoop` 的执行结果转换成最终响应。  
`AgentLoop` 是核心状态机，按阶段在分析、编码、验证、修复、完成之间流转。它会：

- 让 `Planner` 生成工具计划
- 让 `ToolExecutor` 执行工具
- 让 `ProjectIndexer` 重新整理项目索引
- 让 `VerifyEngine` 在验证阶段检查结果

`QwenPlanner` 是当前的主要规划实现。它会把项目摘要、匹配到的 skill、任务内容、阶段规则、最近观察结果和验证错误拼成 prompt，再交给模型生成 `CodePlan`。之后它还会对计划做两层修正：

- 如果计划要修改已有文件，但工具写成了 `write_file`，会回退成 `patch_file`
- 如果 `patch_file` 的 `oldText` 不合法或和真实文件不匹配，会回退为先 `read_file`

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

## 技术栈

- Java 8
- Spring Boot 2.7.2
- Maven
- Lombok
- Jackson
- OkHttp
- JavaParser

## 配置

配置文件位于 `src/main/resources/application.yml`。

```yaml
server:
  port: 8080

minicodex:
  home: D:/ideaProject/AI/code-auto/mini-codex

codex:
  workspace:
    root: D:/ideaProject/test

llm:
  api:
    url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
    key: ${LLM_API_KEY}
  model: qwen-plus
```

关键配置说明：

- `minicodex.home`：`mini-codex` 自身目录，用于加载 prompt 和核心 skill。
- `codex.workspace.root`：目标项目目录，所有代码读取和修改都应落在这里。
- `llm.api.key`：模型调用密钥，建议通过环境变量注入。

## 项目边界

- 需求必须先命中 skill，未知需求不会直接进入自动开发流程。
- 批量任务当前按顺序串行执行。
- 目标项目代码修改必须在工作区内完成。
- 当前版本以 agent 内部验证为主，是否编译由后续流程决定。

## Skill 示例

例如 `rtd-results` skill 主要用于 `ScoreAnalysisEventHandler.handle` 的返回数据加工，它会在规则里写明：

- 目标类和方法必须固定
- 只能基于已有上下文增量追加返回字段
- 不能重写原有返回逻辑
- 不能新增无关类

这类写法可以把重复改造沉淀成稳定模板，也方便后续扩展更多业务 skill。

## 本次产出

- 项目说明：`README.md`
- 领导汇报文档：`mini-codex-iteration1-product-report.md`
