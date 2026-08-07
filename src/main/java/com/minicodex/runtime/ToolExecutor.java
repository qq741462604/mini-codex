package com.minicodex.runtime;

import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.agent.policy.AgentPolicy;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.PlanStep;
import com.minicodex.tool.AgentTool;
import com.minicodex.tool.FileContent;
import com.minicodex.tool.FileOperationResult;
import com.minicodex.tool.ToolInput;
import com.minicodex.trace.TraceStep;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final AgentPolicy agentPolicy;
    private final WorkspaceService workspaceService;

    public List<ToolCallResult> execute(CodePlan plan, AgentContext context) {
        int beforeSize = context.getObservations().size();
        List<ToolCallResult> results = new ArrayList<>();
        for (PlanStep step : plan.getSteps()) {
            if (shouldSkipDuplicateCreate(step, context)) {
                addFailure(results, context, step, "file already created in current agent run");
                continue;
            }
            if (!agentPolicy.allow(context.getPhase(), step.getTool())) {
                log.warn("tool blocked phase={} tool={}", context.getPhase(), step.getTool());
                addFailure(results, context, step, "tool not allowed in phase " + context.getPhase());
                continue;
            }
            AgentTool tool = toolRegistry.getTool(step.getTool());
            if (tool == null) {
                addFailure(results, context, step, "tool not found");
                continue;
            }
            executeTool(step, context, results, tool);
        }
        List<Observation> currentObservations = context.getObservations().subList(
                beforeSize,
                context.getObservations().size()
        );
        context.setLastObservations(new ArrayList<>(currentObservations));
        return results;
    }

    private void addFailure(
            List<ToolCallResult> results,
            AgentContext context,
            PlanStep step,
            String error
    ) {
        results.add(ToolCallResult.failed(step.getTool(), error));
        context.getObservations().add(Observation.builder()
                .tool(step.getTool())
                .success(false)
                .input(step.getInput())
                .error(error)
                .build());
    }

    private void executeTool(
            PlanStep step,
            AgentContext context,
            List<ToolCallResult> results,
            AgentTool tool
    ) {
        try {
            String protectionError = validateFileProtection(step, context);
            if (protectionError != null) {
                log.error("file protection blocked tool={} error={}", step.getTool(), protectionError);
                addFailure(results, context, step, protectionError);
                return;
            }
            long start = System.currentTimeMillis();
            tool.validate(step.getInput());
            Object result = tool.execute(step.getInput(), context);
            long cost = System.currentTimeMillis() - start;
            if (context.getTrace() != null) {
                context.getTrace().add(TraceStep.builder()
                        .type("TOOL")
                        .name(step.getTool())
                        .input(step.getInput())
                        .output(result)
                        .cost(cost)
                        .build());
            }
            ToolCallResult callResult = buildToolResult(step.getTool(), result);
            results.add(callResult);
            context.getObservations().add(Observation.builder()
                    .tool(step.getTool())
                    .success(callResult.isSuccess())
                    .result(result)
                    .input(step.getInput())
                    .error(callResult.isSuccess() ? null : "tool execute failed")
                    .build());
        } catch (Exception e) {
            log.warn("tool execute failed tool={} error={}", step.getTool(), e.getMessage());
            ToolCallResult failed = ToolCallResult.failed(step.getTool(), e.getMessage());
            results.add(failed);
            context.getObservations().add(Observation.builder()
                    .tool(step.getTool())
                    .success(false)
                    .input(step.getInput())
                    .error(e.getMessage())
                    .build());
        }
    }

    private String validateFileProtection(PlanStep step, AgentContext context) {
        if (!(step.getInput() instanceof ToolInput)) {
            return null;
        }
        ToolInput input = (ToolInput) step.getInput();
        String path = input.getPath();
        if (path == null) {
            return null;
        }
        if (isCreateTool(step.getTool()) && pathExists(path)) {
            return "File Protection: existing file must use patch_file path=" + path;
        }
        if ("patch_file".equals(step.getTool()) && !pathExists(path)) {
            return "File Protection: patch_file target file not found path=" + path;
        }
        if ("patch_file".equals(step.getTool()) && !hasReadFile(path, context)) {
            return "File Protection: patch_file requires read_file first path=" + path;
        }
        return null;
    }

    private boolean hasReadFile(String path, AgentContext context) {
        if (context.getObservations() == null) {
            return false;
        }
        File target = workspaceService.resolve(path);
        return context.getObservations().stream().anyMatch(o -> {
            if (!"read_file".equals(o.getTool())
                    || !o.isSuccess()
                    || !(o.getResult() instanceof FileContent)) {
                return false;
            }
            FileContent content = (FileContent) o.getResult();
            File readFile = workspaceService.resolve(content.getPath());
            return sameFile(target, readFile);
        });
    }

    private boolean shouldSkipDuplicateCreate(PlanStep step, AgentContext context) {
        if (!isCreateTool(step.getTool()) || !(step.getInput() instanceof ToolInput)) {
            return false;
        }
        ToolInput input = (ToolInput) step.getInput();
        String path = input.getPath();
        if (path == null || context.getObservations() == null) {
            return false;
        }
        return context.getObservations().stream().anyMatch(o -> {
            if (!o.isSuccess() || !isCreateTool(o.getTool())) {
                return false;
            }
            String existingPath = extractFileOperationPath(o);
            return existingPath != null && samePath(existingPath, path);
        });
    }

    private String extractFileOperationPath(Observation observation) {
        if (observation.getResult() instanceof FileOperationResult) {
            return ((FileOperationResult) observation.getResult()).getPath();
        }
        if (observation.getInput() instanceof ToolInput) {
            return ((ToolInput) observation.getInput()).getPath();
        }
        return null;
    }

    private ToolCallResult buildToolResult(String tool, Object result) {
        if (result instanceof FileOperationResult) {
            FileOperationResult fileResult = (FileOperationResult) result;
            if (!fileResult.isSuccess()) {
                return ToolCallResult.failed(tool, fileResult.getMessage());
            }
            return ToolCallResult.success(tool, result);
        }
        if (result instanceof Map) {
            Map map = (Map) result;
            Object success = map.get("success");
            if (success instanceof Boolean && !((Boolean) success)) {
                return ToolCallResult.failed(tool, String.valueOf(map.get("message")));
            }
        }
        return ToolCallResult.success(tool, result);
    }

    private boolean isCreateTool(String tool) {
        return "create_file".equals(tool) || "write_file".equals(tool);
    }

    private boolean pathExists(String path) {
        try {
            File file = workspaceService.resolve(path);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean sameFile(File left, File right) {
        try {
            return left.getCanonicalFile().equals(right.getCanonicalFile());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean samePath(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.replace("\\", "/").equals(b.replace("\\", "/"));
    }
}
