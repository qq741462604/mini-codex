package com.minicodex.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.llm.LlmClient;
import com.minicodex.project.ProjectContextService;
import com.minicodex.prompt.PromptLoader;
import com.minicodex.prompt.PromptTemplateService;
import com.minicodex.skill.SkillManager;
import com.minicodex.tool.FileContent;
import com.minicodex.tool.ToolInput;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QwenPlanner implements Planner {

    private final PlanValidator planValidator;
    private final PromptLoader promptLoader;
    private final PromptTemplateService templateService;
    private final SkillManager skillManager;
    private final ProjectContextService projectContextService;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final WorkspaceService workspaceService;

    @Override
    public CodePlan createPlan(AgentContext context) {
        long start = System.currentTimeMillis();
        log.info("planner start phase={} task={}", context.getPhase(), summarize(context.getTask(), 120));

        String template = promptLoader.load("planner");
        Map<String, String> vars = new HashMap<>();
        vars.put("PROJECT", buildProjectSummary());

        String matchedSkills = skillManager.buildContext(context.getTask());
        vars.put("SKILLS", matchedSkills);
        vars.put("TASK", context.getTask());
        vars.put("TARGET_RULE", "");
        vars.put("PHASE", context.getPhase().name());
        vars.put("PHASE_RULES", buildPhaseRules(context));

        String observations = buildObservations(context);
        vars.put("OBSERVATIONS", observations);
        vars.put("LAST_ACTION", buildLastAction(context));
        vars.put("VERIFY_ERRORS", buildVerifyErrors(context));

        String projectIndex = buildProjectIndex(context);
        vars.put("PROJECT_INDEX", projectIndex);

        String prompt = templateService.render(template, vars);
        log.info(
                "planner prompt length={} skillsLength={} observationsLength={}",
                prompt.length(),
                matchedSkills == null ? 0 : matchedSkills.length(),
                observations.length()
        );
        String response = llmClient.chat(prompt);
        log.info("planner response length={}", response == null ? 0 : response.length());
        CodePlan plan = parse(context.getTask(), response);
        plan = repairExistingFileWritePlan(context, plan);
        plan = repairInvalidPatchPlan(context, plan);
        try {
            planValidator.validate(plan);
        } catch (Exception e) {
            log.error("plan validation failed {}", e.getMessage());

            throw new RuntimeException(
                    "Planner generated invalid plan:"
                            + e.getMessage()
            );
        }

        log.info("planner finish cost={}ms plan={}", System.currentTimeMillis() - start, summarizePlan(plan));
        return plan;
    }

    private CodePlan repairExistingFileWritePlan(AgentContext context, CodePlan plan) {
        if (plan == null || plan.getSteps() == null || plan.getSteps().isEmpty()) {
            return plan;
        }
        for (PlanStep step : plan.getSteps()) {
            if (!isCreateTool(step.getTool()) || !(step.getInput() instanceof ToolInput)) {
                continue;
            }
            ToolInput input = (ToolInput) step.getInput();
            if (!isExistingFile(input.getPath())) {
                continue;
            }
            String oldText = findReadFileContent(context, input.getPath());
            String newText = writeContent(input);
            if (oldText != null && newText != null) {
                log.warn(
                        "write existing file detected, convert to patch_file path={}",
                        input.getPath()
                );
                step.setTool("patch_file");
                input.setOldText(oldText);
                input.setNewText(newText);
                input.setContent(null);
                continue;
            }
            log.warn(
                    "write existing file detected, fallback to read_file path={}",
                    input.getPath()
            );
            return CodePlan.builder()
                    .task(context.getTask())
                    .steps(singleReadStep(input.getPath()))
                    .build();
        }
        return plan;
    }

    private String writeContent(ToolInput input) {
        if (input.getNewText() != null) {
            return input.getNewText();
        }
        return input.getContent();
    }

    private String findReadFileContent(AgentContext context, String path) {
        if (context.getObservations() == null) {
            return null;
        }
        File target = workspaceService.resolve(path);
        for (int i = context.getObservations().size() - 1; i >= 0; i--) {
            Observation observation = context.getObservations().get(i);
            if (!"read_file".equals(observation.getTool())
                    || !observation.isSuccess()
                    || !(observation.getResult() instanceof FileContent)) {
                continue;
            }
            FileContent content = (FileContent) observation.getResult();
            File readFile = workspaceService.resolve(content.getPath());
            if (!sameFile(target, readFile)) {
                continue;
            }
            if (!isCompleteRead(content)) {
                return null;
            }
            return stripReadLineNumbers(content.getLines());
        }
        return null;
    }

    private boolean isCompleteRead(FileContent content) {
        if (content.getLines() == null || content.getLines().isEmpty()) {
            return true;
        }
        int start = content.getStartLine() == null ? 1 : content.getStartLine();
        int end = content.getEndLine() == null ? start : content.getEndLine();
        return content.getLines().size() < end - start + 1;
    }

    private String stripReadLineNumbers(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        List<String> content = new ArrayList<>();
        for (String line : lines) {
            content.add(line.replaceFirst("^\\d+: ?", ""));
        }
        return String.join("\n", content);
    }

    private boolean sameFile(File left, File right) {
        try {
            return left.getCanonicalFile().equals(right.getCanonicalFile());
        } catch (Exception e) {
            return false;
        }
    }

    private CodePlan repairInvalidPatchPlan(
            AgentContext context,
            CodePlan plan
    ) {

        if (plan == null
                || plan.getSteps() == null
                || plan.getSteps().isEmpty()) {

            return plan;

        }

        for (PlanStep step : plan.getSteps()) {

            if (!"patch_file".equals(step.getTool())
                    || !(step.getInput() instanceof ToolInput)) {

                continue;

            }

            ToolInput input =
                    (ToolInput) step.getInput();

            if (shouldRepairPatchOldText(input)) {

                log.warn(
                        "invalid patch oldText detected, fallback to read_file path={}",
                        input.getPath()
                );

                return CodePlan.builder()
                        .task(context.getTask())
                        .steps(singleReadStep(input.getPath()))
                        .build();

            }

        }

        return plan;

    }

    private boolean shouldRepairPatchOldText(ToolInput input) {
        if (input == null || input.getPath() == null || isEmptyExistingFile(input.getPath())) {
            return false;
        }
        return isInvalidPatchOldText(input.getOldText())
                || !oldTextExists(input.getPath(), input.getOldText());
    }

    private boolean oldTextExists(String path, String oldText) {
        if (path == null || oldText == null) {
            return false;
        }
        try {
            File file = workspaceService.resolve(path);
            if (!file.exists() || !file.isFile()) {
                return true;
            }
            String content = new String(
                    Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8
            );
            return content.contains(oldText)
                    || normalizeLineSeparator(content).contains(normalizeLineSeparator(oldText));
        } catch (Exception e) {
            return true;
        }
    }

    private String normalizeLineSeparator(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    private boolean isCreateTool(String tool) {
        return "write_file".equals(tool) || "create_file".equals(tool);
    }

    private boolean isExistingFile(String path) {
        if (path == null) {
            return false;
        }
        try {
            File file = workspaceService.resolve(path);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isEmptyExistingFile(String path) {
        if (path == null) {
            return false;
        }
        try {
            File file = workspaceService.resolve(path);
            return file.exists() && file.isFile() && file.length() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<PlanStep> singleReadStep(
            String path
    ) {

        List<PlanStep> steps =
                new ArrayList<>();

        steps.add(
                PlanStep.builder()
                        .order(1)
                        .tool("read_file")
                        .input(
                                ToolInput.builder()
                                        .path(path)
                                        .startLine(1)
                                        .endLine(1000)
                                        .build()
                        )
                        .description("Read target file before patch_file")
                        .build()
        );

        return steps;

    }

    private boolean isInvalidPatchOldText(
            String oldText
    ) {

        if (oldText == null
                || oldText.trim().isEmpty()) {

            return true;

        }

        String normalized =
                oldText.trim();

        return normalized.contains("{{")
                || normalized.contains("}}")
                || normalized.contains("READ_FILE_CONTENT")
                || normalized.contains("...");

    }

    private CodePlan parse(String task, String json) {
        try {
            if(json==null
                    ||
                    json.trim().isEmpty()){

                throw new RuntimeException(
                        "model response is empty"
                );

            }


            JsonNode root = objectMapper.readTree(extractJson(json));
            validateModelResponse(root);
            List<PlanStep> steps = new ArrayList<>();
            int index = 1;
            JsonNode stepArray;

            if (root.has("steps")) {
                stepArray = root.get("steps");
            } else if (root.has("plan")) {
                stepArray = root.get("plan");
            } else {
                throw new RuntimeException("plan field not found");
            }

            for (JsonNode node : stepArray) {
                JsonNode inputNode;
                if (node.has("input")) {
                    inputNode = node.get("input");
                } else if (node.has("args")) {
                    inputNode = node.get("args");
                } else {
                    inputNode = objectMapper.createObjectNode();
                }

                ToolInput input = objectMapper.treeToValue(inputNode, ToolInput.class);

                steps.add(
                        PlanStep.builder()
                                .order(index++)
                                .tool(node.get("tool").asText())
                                .input(input)
                                .description(node.has("description") ? node.get("description").asText() : "")
                                .build()
                );
            }
            return CodePlan.builder()
                    .task(task)
                    .steps(steps)
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("parse plan failed:" + json, e);
        }
    }

    private void validateModelResponse(
            JsonNode root
    ){


        if(!root.has("error")){

            return;

        }


        JsonNode error =
                root.get("error");


        String message =
                error.has("message")
                        ? error.get("message").asText()
                        : error.toString();


        throw new RuntimeException(
                "model call failed:"
                        +
                        message
        );

    }

    private String buildLastAction(AgentContext context) {
        if (context.getObservations() == null || context.getObservations().isEmpty()) {
            return "当前没有执行任何工具";
        }

        Observation last = context.getObservations().get(context.getObservations().size() - 1);

        StringBuilder sb = new StringBuilder();
        sb.append("最近一次执行:\n");
        sb.append("tool=").append(last.getTool()).append("\n");
        sb.append("success=").append(last.isSuccess()).append("\n");

        if (!last.isSuccess()) {
            sb.append("\n工具执行失败:\n");
            sb.append(last.getError());
            sb.append("\n必须修正Plan，不允许重复错误操作。\n");
            return sb.toString();
        }

        return sb.toString();
    }

    private String buildPhaseRules(AgentContext context) {
        switch (context.getPhase()) {
            case ANALYSIS:
                return "当前阶段 ANALYSIS\n" +
                        "允许工具:\n" +
                        "- list_files\n" +
                        "- search_code\n" +
                        "- read_file\n" +
                        "目标:分析项目，不修改代码";

            case CODING:
                return
                        "当前阶段 CODING\n\n" +
                                "工具规则:\n\n" +

                                "新增文件:\n" +
                                "允许:\n" +
                                "- write_file\n\n" +

                                "修改已有文件:\n" +
                                "必须:\n" +
                                "- patch_file\n\n" +

                                "patch_file前置要求:\n" +
                                "必须先执行read_file获取oldText。\n" +
                                "禁止编造oldText。\n\n" +

                                "禁止:\n" +
                                "- write_file修改已有文件\n" +
                                "- patch_file没有read_file结果\n\n" +

                                "执行顺序:\n" +
                                "1. 如果修改已有文件，必须先read_file读取完整文件。\n" +
                                "2. 创建新增文件使用write_file。\n" +
                                "3. 修改已有文件必须在read_file之后使用patch_file。\n" +
                                "4. write_file和patch_file没有固定先后，但是patch_file之前必须存在成功read_file结果。\n\n" +

                                "重要:\n" +
                                "如果任务同时包含新增文件和修改已有文件:\n" +
                                "必须生成:\n" +
                                "read_file(Target文件)\n" +
                                "write_file(新增文件)\n" +
                                "patch_file(Target文件)\n\n" +

                                "禁止:\n" +
                                "根据项目上下文猜测oldText。\n" +
                                "禁止直接patch_file已有文件。\n";

            case VERIFY:
                return "当前阶段 VERIFY\n" +
                        "禁止修改代码\n" +
                        "只能:\n" +
                        "- read_file\n" +
                        "- search_code\n";

            case REPAIR:
                return "当前阶段 REPAIR\n" +
                        "根据验证错误修复代码\n" +
                        "允许:\n" +
                        "- read_file\n" +
                        "- write_file\n" +
                        "- patch_file\n\n" +
                        "规则:\n" +
                        "1. 修复已有文件必须先read_file再patch_file。\n" +
                        "2. 只有创建新文件才能write_file。\n" +
                        "3. 禁止write_file修改已有文件。";

            case FINISH:
                return "任务已经完成，不生成任何tool";

            default:
                return "";
        }
    }

    private String buildObservations(AgentContext context) {
        List<Observation> list = getPlannerObservations(context);

        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int start = Math.max(0, list.size() - 3);

        for (int i = start; i < list.size(); i++) {
            Observation o = list.get(i);

            sb.append("tool=").append(o.getTool()).append("\n");
            sb.append("success=").append(o.isSuccess()).append("\n");

            if (o.getInput() != null) {
                String input = String.valueOf(o.getInput());
                if (input.length() > 300) {
                    input = input.substring(0, 300);
                }
                sb.append("input=").append(input).append("\n");
            }

            if (o.getResult() != null) {
                String result = formatObservationResult(o.getResult());
                int maxResultLength =
                        "read_file".equals(o.getTool())
                                ? 8000
                                : 500;
                if (result.length() > maxResultLength) {
                    result = result.substring(0, maxResultLength);
                }
                sb.append("result=").append(result).append("\n");
            }

            if (o.getError() != null) {
                sb.append("error=").append(o.getError()).append("\n");
            }

            sb.append("---\n");
        }

        return sb.toString();
    }

    private String formatObservationResult(Object result) {
        if (result instanceof FileContent) {
            FileContent content = (FileContent) result;
            return "path="
                    + content.getPath()
                    + "\ncontent:\n"
                    + stripReadLineNumbers(content.getLines());
        }
        return String.valueOf(result);
    }

    private String buildProjectSummary() {
        String summary = projectContextService.buildSummary();
        if (summary.length() > 1500) {
            return summary.substring(0, 1500);
        }
        return summary;
    }

    private List<Observation> getPlannerObservations(AgentContext context) {
        List<Observation> list;

        if (context.getLastObservations() != null && !context.getLastObservations().isEmpty()) {
            list = context.getLastObservations();
        } else {
            list = context.getObservations();
        }

        if (list == null) {
            return new ArrayList<>();
        }

        // 只保留最近3次。
        if (list.size() > 3) {
            return list.subList(list.size() - 3, list.size());
        }

        return list;
    }

    private String buildVerifyErrors(AgentContext context) {
        if (context.getVerifyErrors() == null) {
            return "";
        }
        return String.join("\n", context.getVerifyErrors());
    }

    private String buildProjectIndex(AgentContext context) {
        if (context.getProjectIndex() == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(context.getProjectIndex());
        } catch (Exception e) {
            return "";
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String summarizePlan(CodePlan plan) {
        if (plan == null || plan.getSteps() == null || plan.getSteps().isEmpty()) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        for (PlanStep step : plan.getSteps()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(step.getTool());
            if (step.getInput() instanceof ToolInput) {
                ToolInput input = (ToolInput) step.getInput();
                sb.append("(").append(input.getPath()).append(")");
            }
        }
        return sb.toString();
    }

    private String summarize(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
