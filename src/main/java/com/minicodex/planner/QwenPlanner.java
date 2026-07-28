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
public class QwenPlanner
        implements Planner {


    private final PromptLoader promptLoader;

    private final PromptTemplateService templateService;

    private final SkillManager skillManager;

    private final ProjectContextService projectContextService;

    private final LlmClient llmClient;


    private final ObjectMapper objectMapper;


    @Override
    public CodePlan createPlan(
            AgentContext context
    ) {


        String template =
                promptLoader.load(
                        "planner"
                );
        Map<String, String> vars =
                new HashMap<>();


        vars.put(
                "PROJECT",
                projectContextService.buildSummary()
        );

        String matchedSkills =
                skillManager.buildContext(
                        context.getTask()
                );


        log.info(
                "========== MATCHED SKILLS ==========\n{}",
                matchedSkills
        );


        vars.put(
                "SKILLS",
                matchedSkills
        );



        vars.put(
                "TASK",
                context.getTask()
        );

        vars.put(
                "PHASE",
                context.getPhase()
                        .name()
        );

        vars.put(
                "PHASE_RULES",
                buildPhaseRules(
                        context
                )
        );
        try {
            vars.put(
                    "OBSERVATIONS",
                    objectMapper.writeValueAsString(
                            getPlannerObservations(context)
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        vars.put(
                "VERIFY_ERRORS",
                buildVerifyErrors(context)
        );

        vars.put(
                "PROJECT_INDEX",
                buildProjectIndex(context)
        );

        String prompt =
                templateService.render(
                        template,
                        vars
                );


        String response =
                llmClient.chat(
                        prompt
                );


        return parse(
                context.getTask(),
                response
        );

    }


    private CodePlan parse(
            String task,
            String json
    ) {


        try {


            JsonNode root =
                    objectMapper.readTree(
                            extractJson(json)
                    );


            List<PlanStep> steps =
                    new ArrayList<>();


            int index = 1;

            JsonNode stepArray;


            if (root.has("steps")) {

                stepArray = root.get("steps");

            } else if (root.has("plan")) {

                stepArray = root.get("plan");

            } else {

                throw new RuntimeException(
                        "plan field not found"
                );

            }


            for (JsonNode node : stepArray) {


                JsonNode inputNode;


                if (node.has("input")) {

                    inputNode =
                            node.get("input");

                } else if (node.has("args")) {

                    inputNode =
                            node.get("args");

                } else {

                    inputNode =
                            objectMapper.createObjectNode();

                }


                ToolInput input =
                        objectMapper.treeToValue(
                                inputNode,
                                ToolInput.class
                        );


                steps.add(
                        PlanStep.builder()
                                .order(index++)
                                .tool(
                                        node.get("tool")
                                                .asText()
                                )
                                .input(input)
                                .description(
                                        node.has("description")
                                                ?
                                                node.get("description").asText()
                                                :
                                                ""
                                )
                                .build()
                );


            }


            return CodePlan.builder()
                    .task(task)
                    .steps(steps)
                    .build();


        } catch (Exception e) {


            throw new RuntimeException(
                    "parse plan failed:"
                            + json,
                    e
            );


        }


    }


    private String buildPhaseRules(
            AgentContext context
    ){

        switch(context.getPhase()){


            case ANALYSIS:

                return
                        "当前阶段 ANALYSIS\n"
                                +
                                "允许工具:\n"
                                +
                                "- list_files\n"
                                +
                                "- search_code\n"
                                +
                                "- read_file\n"
                                +
                                "目标:分析项目，不修改代码";


            case CODING:

                return
                        "当前阶段 CODING\n"
                                +
                                "允许工具:\n"
                                +
                                "- create_file\n"
                                +
                                "- write_file\n"
                                +
                                "- edit_file\n"
                                +
                                "目标:执行代码修改";


            case VERIFY:

                return
                        "当前阶段 VERIFY\n"
                                +
                                "禁止修改代码\n"
                                +
                                "只能:\n"
                                +
                                "- read_file\n"
                                +
                                "- search_code\n";


            case REPAIR:

                return
                        "当前阶段 REPAIR\n"
                                +
                                "根据验证错误修复代码\n"
                                +
                                "允许:\n"
                                +
                                "- edit_file\n"
                                +
                                "- write_file";


            case FINISH:

                return
                        "任务已经完成，不生成任何tool";


            default:

                return "";

        }

    }


    private List<Observation> getPlannerObservations(
            AgentContext context
    ){

        if(context.getLastObservations()!=null
                &&
                !context.getLastObservations().isEmpty()){


            return context.getLastObservations();

        }


        return context.getObservations();

    }

    private String buildVerifyErrors(
            AgentContext context
    ){


        if(context.getVerifyErrors()==null){

            return "";

        }


        return String.join(
                "\n",
                context.getVerifyErrors()
        );

    }

    private String buildProjectIndex(
            AgentContext context
    ) {


        if (context.getProjectIndex() == null) {

            return "";

        }


        try {


            return objectMapper.writeValueAsString(
                    context.getProjectIndex()
            );


        } catch (Exception e) {


            return "";

        }

    }


    private String extractJson(
            String text
    ) {


        int start =
                text.indexOf("{");


        int end =
                text.lastIndexOf("}");


        if (start >= 0 && end > start) {

            return text.substring(
                    start,
                    end + 1
            );

        }


        return text;


    }


}