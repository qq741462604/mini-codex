# mini-codex

`mini-codex` 是一个面向企业 Java 项目的代码改造 Agent。它接收需求后，先做技能匹配和上下文组装，再驱动规划、工具执行、验证和结果汇总，最终输出本次改造涉及的代码变更。

## 当前架构

源码采用四层架构：`interfaces` 负责 HTTP 适配，`application` 编排 Agent 用例，`domain` 承载状态与规则，`infrastructure` 提供 LLM、工作区、Prompt、skill 与工具实现；`bootstrap` 只负责 Spring 启动和装配。

应用层通过 `WorkspacePort`、`SkillRepository`、`PromptRepository`、`LlmClient`、`MemoryStore` 和 `AgentTool` 访问外部能力，不直接依赖基础设施实现。完整类职责见 `docs/architecture/class-catalog.md`。

### 四层之间怎么理解

可以把这套架构理解成“入口、编排、数据、实现”四个职责区域：

```text
外部 HTTP 请求
      |
      v
interfaces  接口层：把 HTTP 转成应用请求
      |
      v
application 应用层：编排一次 Agent 用例
      |
      +------> domain：读取和更新 Agent 的状态、计划、观察结果
      |
      +------> application.port：调用外部能力的接口
                            ^
                            |
                   infrastructure：实现这些接口
                   文件系统 / LLM / Prompt / skill / 工具

bootstrap：启动 Spring，并把上述组件装配起来
```

这里有一个容易混淆的点：`bootstrap` 虽然位于 `com.minicodex` 包下，但它不是业务调用层，而是启动装配层；`application.port` 也不是具体实现，而是应用层定义的“我需要什么能力”。例如：

1. `QwenPlanner` 需要读取 Prompt，于是依赖 `PromptRepository`。
2. `PromptLoader` 位于基础设施层，实现 `PromptRepository`，负责从 `.ai/prompts` 读取文件。
3. Spring 启动时发现 `PromptLoader` 是 Bean，并把它注入到 `QwenPlanner` 的 `PromptRepository` 字段中。
4. `QwenPlanner` 只知道“可以通过接口读取 Prompt”，不需要知道文件路径和读取细节。

因此，依赖方向要区分两种关系：

- **业务调用方向**：接口层调用应用层，应用层使用领域模型并调用端口。
- **代码依赖方向**：应用层依赖端口接口，基础设施层实现端口；应用层不导入基础设施具体类。

### 四层分别负责什么

#### 1. `interfaces`：接收请求和返回响应

接口层只关心外部协议，不负责规划、改文件或验证代码。

- `AgentController` 暴露 `/agent/run`。
- `AgentRequest`、`AgentTaskRequest` 定义请求 JSON 的结构。
- 单条请求和批量请求在这里完成基本分流，然后交给应用层服务。

接口层不应该直接调用 `QwenClient`、`WorkspaceService` 或具体文件工具；否则 HTTP 适配和业务实现会耦合在一起。

#### 2. `application`：编排一次完整用例

应用层是系统的“流程控制中心”，负责决定先做什么、后做什么，但不负责保存具体文件或拼接底层 HTTP 请求。

- `AgentBatchService`：按顺序处理批量需求。
- `AgentRuntime`、`AgentExecutor`、`AgentLoop`：驱动一次 Agent 运行。
- `PhaseManager`、各阶段处理器、`AgentPolicy`：控制分析、编码、验证、修复和完成阶段。
- `QwenPlanner`：组装上下文、调用 LLM 端口并生成 `CodePlan`。
- `ToolExecutor`：根据计划查找并执行工具，记录 `Observation`。
- `VerifyEngine`、`CodeValidator`：检查改动结果并生成验证错误。
- `CodeChangeExtractor`：从观察结果中提取最终变更清单。

应用层依赖 `domain` 中的状态对象，也依赖 `application.port` 中的接口；它不应该依赖基础设施的具体实现类。

#### 3. `domain`：保存业务状态和规则数据

领域层描述 Agent 处理过程中“是什么”，不负责 Spring 启动、文件读写或网络请求。

- `AgentContext`：一次任务的完整上下文。
- `AgentPhase`、`AgentStatus`：当前阶段和运行状态。
- `CodePlan`、`PlanStep`：模型生成的计划及其步骤。
- `Observation`：工具执行后的观察记录。
- `AgentResult`、`CodeChange`：最终返回结果和代码变更。
- `Skill`、`Memory`、`ProjectIndex`、`AgentTrace`：技能、记忆、项目索引和执行跟踪数据。
- `ToolInput`、`FileContent`、`FileOperationResult`：工具调用所需的输入输出数据。

领域对象可以被接口层和应用层使用，但不应反过来依赖 Controller、LLM 客户端或文件系统服务。

#### 4. `infrastructure`：提供技术实现

基础设施层负责“怎么做”，把外部系统和本地资源接入应用。

- `QwenClient`：实现 `LlmClient`，调用配置的模型服务。
- `WorkspaceService`：实现 `WorkspacePort`，解析路径并限制访问范围。
- `PromptLoader`：实现 `PromptRepository`，读取 `.ai/prompts` 下的模板。
- `SkillLoader`：实现 `SkillRepository`，读取 `.ai/skills` 下的 skill 文件。
- `ReadFileTool`、`PatchFileTool`、`WriteFileTool` 等：实现 `AgentTool`，执行受工作区保护的文件操作。
- `SimpleMemoryStore`：实现 `MemoryStore`，保存运行记忆。

基础设施可以依赖领域数据模型，也可以依赖应用层定义的端口；但具体实现不应被接口层或领域层直接调用。

### 一次 `/agent/run` 请求如何流转

下面按实际运行顺序展开：

1. **启动阶段**：`bootstrap.MiniCodexApplication` 启动 Spring，扫描 `com.minicodex`；`bootstrap.AgentConfig` 创建默认 `Agent` Bean。
2. **进入接口层**：`interfaces.web.AgentController` 接收请求，识别单条或批量模式。
3. **技能预检查**：应用层的 `SkillMatcher` 通过 `SkillRepository` 读取并匹配 skill；未命中时直接返回兜底结果，不进入自动改造。
4. **创建上下文**：`AgentContextFactory` 创建独立的 `AgentContext`，初始化任务、阶段、记忆、技能、观察和 trace。
5. **开始执行**：`Agent` 调用 `AgentRuntime`，`AgentRuntime` 再交给 `AgentExecutor` 和 `AgentLoop`。
6. **分析阶段**：`AgentLoop` 调用 `QwenPlanner`；规划器通过 `PromptRepository` 读取模板，通过 `WorkspacePort` 获取项目上下文，通过 `LlmClient` 请求模型，生成 `CodePlan`。
7. **工具阶段**：`ToolExecutor` 根据 `PlanStep` 从 `ToolRegistry` 找到 `AgentTool`；具体工具在基础设施层执行，并把结果写入 `Observation`。
8. **保护检查**：执行文件工具前，应用层检查阶段白名单、路径边界、文件是否已读取，以及创建/修改规则。
9. **阶段流转**：工具完成后，`PhaseManager` 根据上下文决定继续分析、进入编码、验证、修复或完成。
10. **验证阶段**：`VerifyEngine` 和 `CodeValidator` 检查 Java 文件和变更结果；失败时将错误写回上下文，推动进入修复阶段。
11. **结果汇总**：`CodeChangeExtractor` 从成功的文件操作观察中提取 `changes`，`AgentExecutor` 组装 `AgentResult`。
12. **返回响应**：`TraceService` 结束 trace，Controller 将结果序列化为 HTTP 响应。

整个过程可以简化为：

```text
HTTP
 -> Controller
 -> AgentBatchService / SkillMatcher
 -> AgentContextFactory
 -> AgentRuntime
 -> AgentExecutor
 -> AgentLoop
    -> Planner -> LlmClient / PromptRepository / WorkspacePort
    -> ToolExecutor -> AgentTool
    -> VerifyEngine
 -> CodeChangeExtractor
 -> HTTP 响应
```

### 为什么不让应用层直接调用基础设施

如果 `QwenPlanner` 直接依赖 `QwenClient`、`WorkspaceService`，以后更换模型供应商、替换文件系统或改成数据库 skill 存储，就需要修改应用流程代码。现在应用层只依赖 `LlmClient`、`WorkspacePort`、`SkillRepository` 等端口，替换实现时只需新增或修改基础设施适配器，Agent 的业务流程不变。

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

- [AgentController.java](src/main/java/com/minicodex/interfaces/web/AgentController.java)
- [AgentBatchService.java](src/main/java/com/minicodex/application/agent/AgentBatchService.java)

`AgentController` 是统一入口。它先判断是否为批量请求，再用 `SkillMatcher` 判断当前任务是否有可执行 skill。批量任务由 `AgentBatchService` 串行处理，任一任务失败即停止后续任务。

### 2. Agent 运行入口

- [Agent.java](src/main/java/com/minicodex/application/agent/Agent.java)
- [AgentContextFactory.java](src/main/java/com/minicodex/application/agent/AgentContextFactory.java)

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

- [AgentRuntime.java](src/main/java/com/minicodex/application/execution/AgentRuntime.java)
- [AgentExecutor.java](src/main/java/com/minicodex/application/execution/AgentExecutor.java)
- [AgentLoop.java](src/main/java/com/minicodex/application/execution/AgentLoop.java)
- [PhaseManager.java](src/main/java/com/minicodex/application/phase/PhaseManager.java)
- [QwenPlanner.java](src/main/java/com/minicodex/application/planning/QwenPlanner.java)

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

- [ToolExecutor.java](src/main/java/com/minicodex/application/execution/ToolExecutor.java)
- [WorkspaceService.java](src/main/java/com/minicodex/infrastructure/workspace/WorkspaceService.java)
- [WorkspacePort.java](src/main/java/com/minicodex/application/port/WorkspacePort.java)

工具执行阶段通过 `WorkspacePort` 访问受限工作区；具体实现由基础设施层提供。核心约束包括：

- 路径统一落到 `codex.workspace.root`
- `patch_file` 必须先读文件
- 创建文件不能覆盖已有文件
- 工具执行结果会同步进 `observations`

### 5. 验证与结果汇总

- [VerifyEngine.java](src/main/java/com/minicodex/application/verification/VerifyEngine.java)
- [CodeValidator.java](src/main/java/com/minicodex/application/verification/CodeValidator.java)
- [CodeChangeExtractor.java](src/main/java/com/minicodex/application/service/CodeChangeExtractor.java)

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
- 当前版本以 agent 内部验证为主；架构迁移已通过 `mvn -DskipTests compile` 和 HTTP 路由冒烟校验。
- 已移除未接入的 Git、测试、旧规划与旧保护链路，避免保留不可达或危险能力。

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
