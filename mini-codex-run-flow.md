# mini-codex 项目运行流转图

```text
/agent/run
   |
   v
AgentController
   |
   +------------------------+
   |                        |
   v                        v
Batch Request           Single Request
   |                        |
   v                        v
AgentBatchService      SkillMatcher
   |                        |
   v                        v
Agent.run()            Agent.run()
   |                        |
   v                        v
TraceService.start()    TraceService.start()
   |                        |
   v                        v
AgentContextFactory     AgentContextFactory
   |                        |
   v                        v
AgentRuntime
   |
   v
AgentExecutor
   |
   v
AgentLoop
   |
   +-------------------------------+
   |               |               |
   v               v               v
Planner        ToolExecutor     VerifyEngine
   |               |               |
   |               |               v
   |               |          Validation Result
   |               |
   |               +--------------------------+
   |                                          |
   v                                          v
CodePlan                                 observations / trace
   |
   v
Tool Calling
   |
   v
File Tool  <---->  Search Tool
   |
   v
CodeChangeExtractor
   |
   v
AgentResult
   |
   v
TraceService.finish()
   |
   v
Response
```

## 说明

- `AgentController` 是统一入口。
- `SkillMatcher` 决定需求是否进入自动执行链路。
- `AgentContextFactory` 负责创建本次任务专用上下文。
- `AgentLoop` 是核心循环，驱动规划、工具调用和验证。
- `ToolExecutor` 负责文件类工具和检索类工具的实际执行。
- `CodeChangeExtractor` 汇总本次改动结果。

