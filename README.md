# mini-codex

`mini-codex` 是一个面向企业 Java 项目的代码改造 Agent。项目目标是把已知、可复用的业务改造需求沉淀为 skill，再根据用户输入匹配 skill，自动完成目标项目中的代码定位、读取、修改和结果返回。

当前版本已经完成从“单条需求执行”到“批量需求串行执行”的初版能力，并增加了未知需求兜底，避免未匹配 skill 的需求直接进入模型开发流程。

## 核心能力

- **Skill 驱动开发**：通过 `.ai/skills/**/skill.md` 定义关键词、目标类、改造规则、实现步骤和禁止行为。
- **未知需求兜底**：没有命中业务 skill 时，接口直接返回提示语，不进入 Agent 执行链路。
- **单条需求执行**：兼容原有 `/agent/run` 的 `task` 输入。
- **批量需求执行**：支持 `tasks` 数组串行执行多个改造点。
- **失败即停止**：批量执行中任一任务失败，立即停止后续任务，并返回失败序号、任务标识和失败原因。
- **目标项目隔离**：通过 `codex.workspace.root` 指定目标项目，工具基于工作区路径解析文件。
- **安全文件修改**：已有文件必须先读取再使用 `patch_file` 修改，避免直接覆盖。
- **Java 语法校验**：验证阶段引入 JavaParser，按 JDK 11 语言级别解析变更后的 Java 文件。
- **最小测试约束**：公共 skill 要求新生成目标项目代码后补充最小单元测试或可独立执行的测试类。
- **代码变更汇总**：接口返回 `changes`，汇总本次执行涉及的创建或修改文件。

## 技术栈

- Java 8
- Spring Boot 2.7.2
- Maven
- Jackson
- Lombok
- OkHttp
- JavaParser
- DashScope 兼容 OpenAI Chat Completions 接口

说明：`mini-codex` 自身当前按 Java 8 构建；生成和校验目标项目代码时，公共 skill 约定目标项目开发环境为 JDK 11。

## 项目结构

```text
mini-codex
├── .ai
│   ├── prompts
│   │   └── planner.md
│   └── skills
│       ├── common-file-editing
│       ├── date-transform
│       ├── external-api
│       ├── ip-parser
│       └── rtd-results
├── docs
├── logs
├── src
│   └── main
│       ├── java/com/minicodex
│       └── resources
└── pom.xml
```

## 核心流程

1. 调用方提交需求到 `/agent/run`。
2. Controller 判断请求是单条还是批量。
3. SkillMatcher 根据关键词匹配业务 skill。
4. 未命中业务 skill 时直接返回兜底提示。
5. 命中 skill 后进入 Agent 执行链路。
6. Planner 结合项目上下文、skill、阶段规则生成工具计划。
7. ToolExecutor 执行 `search_code`、`read_file`、`patch_file`、`write_file` 等工具。
8. AgentLoop 在分析、编码、验证、修复、完成阶段之间流转。
9. CodeChangeExtractor 汇总代码变更并返回接口。

## 配置说明

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

关键配置：

- `minicodex.home`：mini-codex 自身目录，用于加载 prompt 和核心 skill。
- `codex.workspace.root`：目标项目目录，所有代码读取和修改都应落在该目录下。
- `llm.api.key`：模型调用密钥，建议通过环境变量 `LLM_API_KEY` 注入。

## API 使用

### 单条需求

请求：

```http
POST /agent/run
Content-Type: application/json
```

```json
{
  "task": "将模型评分返回"
}
```

响应示例：

```json
{
  "success": true,
  "message": "agent execute success",
  "data": [],
  "changes": [
    {
      "action": "edit",
      "path": "src/main/java/com/study/plugin/extension/ScoreAnalysisEventHandler.java",
      "success": true
    }
  ]
}
```

### 批量需求

请求：

```json
{
  "tasks": [
    {
      "id": "score",
      "task": "将模型评分返回"
    },
    {
      "id": "rtq",
      "task": "将rtqVars添加到rtdResults返回"
    }
  ]
}
```

成功响应：

```json
{
  "success": true,
  "message": "batch execute success",
  "data": {
    "stopped": false,
    "failedIndex": null,
    "failedTaskId": null,
    "failureReason": null,
    "results": []
  },
  "changes": []
}
```

失败响应：

```json
{
  "success": false,
  "message": "batch execute failed, stopped at task 2: task is empty",
  "data": {
    "stopped": true,
    "failedIndex": 2,
    "failedTaskId": "rtq",
    "failureReason": "task is empty",
    "results": []
  },
  "changes": []
}
```

批量执行策略：

- 串行执行，前一条完成后再执行下一条。
- 任一任务失败立即停止。
- 不继续调用模型，避免无意义开销。
- 接口和日志同时记录失败原因。

## Skill 说明

Skill 位于 `.ai/skills/<skill-name>/skill.md`。

常见结构：

```text
name:
skill-name

keywords:
- 关键词

Target:
class:
目标类

method:
目标方法

Rules:
1. 约束规则

Implementation:
具体实现步骤

Forbidden:
- 禁止行为
```

当前内置 skill：

- `common-file-editing`：通用文件修改安全规则。
- `date-transform`：日期字段转换和拼接加工。
- `external-api`：外部接口调用链生成。
- `ip-parser`：IP 信息解析增强。
- `rtd-results`：`ScoreAnalysisEventHandler` 中围绕 `rtdResults` 的返回数据加工。

公共 skill 额外约束：

- 目标项目 Java 代码按 JDK 11 兼容性生成。
- 新增目标项目代码后，应生成最小单元测试或测试类验证核心逻辑。
- 外部接口调用类优先通过测试类直接验证 Client 或核心方法，减少完整业务流程测试成本。

## 已完成的关键改造

- 增加未知需求兜底，未匹配业务 skill 时不触发开发。
- 增加 `rtd-results` skill，支持 `procVars`、`rtqVars`、模型评分等返回数据加工。
- 修复 `patch_file` 变更未进入 `changes` 的问题。
- 新增批量执行能力，支持多改造点串行处理。
- 批量执行失败即停止，并返回失败原因。
- 强化 skill 对小片段 patch 的约束，降低整文件替换失败风险。
- 引入 JavaParser，对变更后的 Java 文件进行 JDK 11 语法级校验。
- 在公共 skill 中补充目标代码 JDK 11 和最小测试生成约束。

## 当前边界

- 需求必须先沉淀为 skill，未知需求不会自动开发。
- 批量任务串行执行，不支持并行。
- skill 匹配基于关键词，复杂语义匹配仍依赖后续增强。
- 当前验证以 Agent 内部工具检查为主，是否编译由调用方或后续流程决定。

## 后续规划

- 增加 skill 热加载或刷新能力，减少服务重启成本。
- 增加批量任务的执行摘要和耗时统计。
- 优化 prompt 中整文件 patch 与小片段 patch 的规则一致性。
- 增加更细粒度的失败分类，例如 skill 未命中、模型计划失败、工具执行失败、验证失败。
- 增加前端或管理接口，用于维护 skill、查看执行记录和失败原因。
