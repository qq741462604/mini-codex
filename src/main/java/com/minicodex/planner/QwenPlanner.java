package com.minicodex.planner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.llm.LlmClient;
import com.minicodex.project.ProjectContextService;
import com.minicodex.prompt.PromptLoader;
import com.minicodex.prompt.PromptTemplateService;
import com.minicodex.skill.SkillManager;
import com.minicodex.tool.ToolInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    @Override
    public CodePlan createPlan(AgentContext context) {
        log.info("task={}, phase={}", context.getTask(), context.getPhase());

        String template = promptLoader.load("planner");
        Map<String, String> vars = new HashMap<>();

        vars.put("PROJECT", buildProjectSummary());

        String matchedSkills = skillManager.buildContext(context.getTask());

        log.info("skill context length={}", matchedSkills == null ? 0 : matchedSkills.length());
        log.info("========== MATCHED SKILLS ==========\n{}", matchedSkills);

        vars.put("SKILLS", matchedSkills);
        vars.put("TASK", context.getTask());
        vars.put(
                "TARGET_RULE",
                buildTargetRule()
        );
        vars.put("PHASE", context.getPhase().name());
        vars.put("PHASE_RULES", buildPhaseRules(context));

        String observations = buildObservations(context);
        log.info("observations length={}", observations.length());
        vars.put("OBSERVATIONS", observations);

        vars.put("LAST_ACTION", buildLastAction(context));
        vars.put("VERIFY_ERRORS", buildVerifyErrors(context));

        String projectIndex = buildProjectIndex(context);
        log.info("project index length={}", projectIndex.length());
        vars.put("PROJECT_INDEX", projectIndex);

        String prompt = templateService.render(template, vars);

        log.info("planner prompt={}", prompt);
        log.info("planner prompt length={}", prompt.length());
        log.info("planner template length={}", template.length());
        log.info("PROJECT length={}", vars.get("PROJECT") == null ? 0 : vars.get("PROJECT").length());
        log.info("TASK length={}", vars.get("TASK") == null ? 0 : vars.get("TASK").length());
        log.info("PHASE_RULES length={}", vars.get("PHASE_RULES") == null ? 0 : vars.get("PHASE_RULES").length());
        log.info("LAST_ACTION length={}", vars.get("LAST_ACTION") == null ? 0 : vars.get("LAST_ACTION").length());

        String response = llmClient.chat(prompt);
        log.info(
                "========== PLAN RESPONSE ==========\n{}",
                response
        );
        log.info(
                "========== PROMPT TAIL ==========\n{}",
                prompt.substring(
                        Math.max(0,prompt.length()-2000)
                )
        );
        CodePlan plan = parse(context.getTask(), response);
        plan = repairInvalidPatchPlan(context, plan);
        try {
            log.info(
                    "========== PARSED PLAN ==========\n{}",
                    objectMapper.writeValueAsString(plan)
            );
        } catch(Exception e){

        }
        try {
            planValidator.validate(plan);
        } catch (Exception e) {
            log.error("plan validation failed {}", e.getMessage());

            throw new RuntimeException(
                    "Planner generated invalid plan:"
                            + e.getMessage()
            );

//            plan = repairPlan(plan, e.getMessage());
//
//            planValidator.validate(plan);

//            context.getVerifyErrors()
//                    .add(
//                            "Plan invalid:"
//                                    + e.getMessage()
//                    );
//
//
//            return CodePlan.builder()
//                    .task(context.getTask())
//                    .steps(new ArrayList<>())
//                    .build();

        }

        return plan;
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

            if (!isTargetFile(input.getPath())) {

                continue;

            }

            if (isInvalidPatchOldText(input.getOldText())) {

                log.warn(
                        "invalid target patch oldText detected, fallback to read_file path={}",
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

    private boolean isTargetFile(
            String path
    ) {

        if (path == null) {

            return false;

        }

        return path.replace("\\", "/")
                .endsWith("DataPrepEventHandler.java");

    }

    private boolean isInvalidPatchOldText(
            String oldText
    ) {

        if (oldText == null
                || oldText.trim().length() < 50) {

            return true;

        }

        String normalized =
                oldText.trim();

        return normalized.contains("{{")
                || normalized.contains("}}")
                || normalized.contains("READ_FILE_CONTENT")
                || normalized.contains("...");

    }

    private String buildTargetRule() {
        return "Target File Rule:\n" +
                "\n" +
                "如果任务涉及:\n" +
                "DataPrepEventHandler.java\n" +
                "\n" +
                "该文件已经存在。\n" +
                "\n" +
                "禁止:\n" +
                "write_file\n" +
                "\n" +
                "禁止:\n" +
                "create_file\n" +
                "\n" +
                "必须:\n" +
                "\n" +
                "Step1:\n" +
                "read_file\n" +
                "\n" +
                "Step2:\n" +
                "patch_file\n" +
                "\n" +
                "patch_file必须基于read_file返回oldText。\n" +
                "\n" +
                "任何情况下不能重新生成整个Target。\n" +
                "\n";
    }
    
    private CodePlan repairPlan(
            CodePlan plan,
            String error
    ){

        if(error.contains("must use patch_file")){


            for(PlanStep step:plan.getSteps()){


                if("write_file".equals(step.getTool())
                        &&
                        step.getInput()!=null){


                    ToolInput input =
                            (ToolInput)step.getInput();


//                    if(isTargetFile(input.getPath())){
                    if(input.getPath().contains("DataPrepEventHandler")){


                        log.warn(
                                "auto repair write_file -> patch_file {}",
                                input.getPath()
                        );


                        step.setTool("patch_file");

                    }

                }

            }

        }


        return plan;

    }



    private CodePlan parse(String task, String json) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(json));
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
            normalizeTargetTool(steps);
            return CodePlan.builder()
                    .task(task)
                    .steps(steps)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("parse plan failed:" + json, e);
        }
    }

    private void normalizeTargetTool(List<PlanStep> steps){


        for(PlanStep step:steps){


            if(step.getInput() instanceof ToolInput){


                ToolInput input =
                        (ToolInput)step.getInput();


                String path=input.getPath();


                if(path==null){
                    continue;
                }


                if(path.endsWith(
                        "DataPrepEventHandler.java"
                )
                        &&
                        "write_file".equals(step.getTool())){


                    log.warn(
                            "normalize target write_file -> patch_file"
                    );


                    step.setTool("patch_file");

                }

            }

        }

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
                        "- edit_file\n" +
                        "- write_file";

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
                String result = String.valueOf(o.getResult());
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
}
