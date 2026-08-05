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

        log.info("planner prompt length={}", prompt.length());
        log.info("planner template length={}", template.length());
        log.info("PROJECT length={}", vars.get("PROJECT") == null ? 0 : vars.get("PROJECT").length());
        log.info("TASK length={}", vars.get("TASK") == null ? 0 : vars.get("TASK").length());
        log.info("PHASE_RULES length={}", vars.get("PHASE_RULES") == null ? 0 : vars.get("PHASE_RULES").length());
        log.info("LAST_ACTION length={}", vars.get("LAST_ACTION") == null ? 0 : vars.get("LAST_ACTION").length());

        String response = llmClient.chat(prompt);

        CodePlan plan = parse(context.getTask(), response);

        try {
            planValidator.validate(plan);
        } catch (Exception e) {
            log.error("plan validation failed {}", e.getMessage());
            plan = repairPlan(plan, e.getMessage());

            planValidator.validate(plan);
        }

        return plan;
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


                    if(isTargetFile(input.getPath())){
//                    if(input.getPath().contains("DataPrepEventHandler")){


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

    private boolean isTargetFile(String path){

        return path!=null
                &&
                path.endsWith(
                        "DataPrepEventHandler.java"
                );
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

            return CodePlan.builder()
                    .task(task)
                    .steps(steps)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("parse plan failed:" + json, e);
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
                return "当前阶段 CODING\n\n" +
                        "工具规则:\n\n" +
                        "新增文件:\n" +
                        "允许:\n" +
                        "- write_file\n\n\n" +
                        "修改已有文件:\n" +
                        "必须:\n" +
                        "- patch_file\n\n\n" +
                        "禁止:\n" +
                        "- write_file修改已有文件\n\n\n" +
                        "如果Skill存在:\n" +
                        "优先执行Skill中的文件修改规则。\n";

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
                if (result.length() > 500) {
                    result = result.substring(0, 500);
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