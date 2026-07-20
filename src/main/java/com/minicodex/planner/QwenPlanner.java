package com.minicodex.planner;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicodex.agent.AgentContext;
import com.minicodex.llm.LlmClient;
import com.minicodex.project.ProjectContextService;
import com.minicodex.prompt.PromptLoader;
import com.minicodex.prompt.PromptTemplateService;
import com.minicodex.skill.SkillManager;
import com.minicodex.tool.ToolInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    ){


        String template =
                promptLoader.load(
                        "planner"
                );
        Map<String,String> vars =
                new HashMap<>();


        vars.put(
                "PROJECT",
                projectContextService.buildSummary()
        );


        vars.put(
                "SKILLS",
                skillManager.buildContext()
        );


        vars.put(
                "TASK",
                context.getTask()
        );

        try {
            vars.put(
                    "OBSERVATIONS",
                    objectMapper.writeValueAsString(
                            context.getObservations()
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

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
    ){


        try{


            JsonNode root =
                    objectMapper.readTree(
                            extractJson(json)
                    );



            List<PlanStep> steps =
                    new ArrayList<>();



            int index = 1;

            JsonNode stepArray;


            if(root.has("steps")){

                stepArray = root.get("steps");

            }else if(root.has("plan")){

                stepArray = root.get("plan");

            }else{

                throw new RuntimeException(
                        "plan field not found"
                );

            }



            for(JsonNode node:stepArray){

//            for(JsonNode node:
//                    root.get("steps")){


//                ToolInput input =
//                        objectMapper.treeToValue(
//                                node.get("input"),
//                                ToolInput.class
//                        );
                JsonNode inputNode;


                if(node.has("input")){

                    inputNode =
                            node.get("input");

                }else if(node.has("args")){

                    inputNode =
                            node.get("args");

                }else{

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



        }catch(Exception e){


            throw new RuntimeException(
                    "parse plan failed:"
                            + json,
                    e
            );


        }


    }


    private String buildProjectIndex(
            AgentContext context
    ){


        if(context.getProjectIndex()==null){

            return "";

        }



        try{


            return objectMapper.writeValueAsString(
                    context.getProjectIndex()
            );


        }catch(Exception e){


            return "";

        }

    }


    private String extractJson(
            String text
    ){


        int start =
                text.indexOf("{");


        int end =
                text.lastIndexOf("}");



        if(start>=0 && end>start){

            return text.substring(
                    start,
                    end+1
            );

        }


        return text;


    }



}