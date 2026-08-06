package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.agent.policy.AgentPolicy;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.PlanStep;
import com.minicodex.tool.AgentTool;
import com.minicodex.tool.FileContent;
import com.minicodex.tool.FileOperationResult;
import com.minicodex.tool.SearchMatch;
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

    public List<ToolCallResult> execute(
            CodePlan plan,
            AgentContext context
    ){

        int beforeSize =
                context.getObservations()
                        .size();


        List<ToolCallResult> results =
                new ArrayList<>();



        for(PlanStep step:plan.getSteps()){

            log.info(
                    "execute tool={} phase={}",
                    step.getTool(),
                    context.getPhase()
            );

            normalizeKnownTargetPath(
                    step,
                    context
            );

            /*
             *
             * 防止重复create
             *
             */
            if(shouldSkipDuplicateCreate(
                    step,
                    context
            )){

                com.minicodex.tool.ToolInput input =
                        (com.minicodex.tool.ToolInput) step.getInput();



                String path =
                        input.getPath();
                log.info(
                        "skip duplicate create file={}",
                        path
                );


                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                "file already created in current agent run"
                        )
                );


                continue;

            }

            if(!agentPolicy.allow(
                    context.getPhase(),
                    step.getTool()
            )){


                log.warn(
                        "tool blocked phase={} tool={}",
                        context.getPhase(),
                        step.getTool()
                );


                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                "tool not allowed in phase "
                                        +
                                        context.getPhase()
                        )
                );


                continue;

            }
            AgentTool tool =
                    toolRegistry.getTool(
                            step.getTool()
                    );



            if(tool==null){


                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                "tool not found"
                        )
                );


                continue;

            }





            try{

                String protectionError = validateSkillProtection(step, context);

                if (protectionError != null) {

                    log.error("skill protection blocked tool={} error={}",
                            step.getTool(),
                            protectionError);

                    ToolCallResult failed =
                            ToolCallResult.failed(
                                    step.getTool(),
                                    protectionError
                            );

                    results.add(failed);

                    context.getObservations().add(
                            Observation.builder()
                                    .tool(step.getTool())
                                    .success(false)
                                    .input(step.getInput())
                                    .error(protectionError)
                                    .build()
                    );

                    continue;
                }


                long start = System.currentTimeMillis();



                tool.validate(
                        step.getInput()
                );



                Object result =
                        tool.execute(
                                step.getInput(),
                                context
                        );



                long cost =
                        System.currentTimeMillis()
                                -
                                start;



                if(context.getTrace()!=null){


                    context.getTrace()
                            .add(
                                    TraceStep.builder()
                                            .type("TOOL")
                                            .name(step.getTool())
                                            .input(step.getInput())
                                            .output(result)
                                            .cost(cost)
                                            .build()
                            );


                }



                ToolCallResult callResult =
                        buildToolResult(
                                step.getTool(),
                                result
                        );



                results.add(
                        callResult
                );

                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(callResult.isSuccess())
                                        .result(result)
                                        .input(step.getInput())
                                        .error(
                                                callResult.isSuccess()
                                                        ?
                                                        null
                                                        :
                                                        "tool execute failed"
                                        )
                                        .build()
                        );





            }catch(Exception e){


                log.warn(
                        "tool execute failed tool={} error={}",
                        step.getTool(),
                        e.getMessage()
                );



                ToolCallResult failed =
                        ToolCallResult.failed(
                                step.getTool(),
                                e.getMessage()
                        );


                results.add(
                        failed
                );


                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(false)
                                        .input(step.getInput())
                                        .error(e.getMessage())
                                        .build()
                        );


            }


        }

        List<Observation> currentObservations =
                context.getObservations()
                        .subList(
                                beforeSize,
                                context.getObservations().size()
                        );


        context.setLastObservations(
                new ArrayList<>(
                        currentObservations
                )
        );

        return results;

    }

    private void normalizeKnownTargetPath(
            PlanStep step,
            AgentContext context
    ){


        if(!(step.getInput() instanceof ToolInput)){

            return;

        }


        ToolInput input =
                (ToolInput)step.getInput();


        String path =
                input.getPath();


        if(!isTargetPath(path)){

            return;

        }


        if(pathExists(path)){

            return;

        }


        String knownPath =
                findKnownTargetPath(context);


        if(knownPath==null){

            knownPath =
                    findSourceRootTargetPath(path);

        }


        if(knownPath==null){

            return;

        }


        if(!samePath(path,knownPath)){

            log.info(
                    "normalize target path from={} to={}",
                    path,
                    knownPath
            );

            input.setPath(knownPath);

        }

    }


    private boolean isTargetPath(
            String path
    ){


        if(path==null){

            return false;

        }


        return path.replace("\\","/")
                .endsWith("DataPrepEventHandler.java");

    }


    private boolean pathExists(
            String path
    ){


        try{

            File file =
                    workspaceService.resolve(path);


            return file.exists()
                    &&
                    file.isFile();

        }catch(Exception e){

            return false;

        }

    }


    private String findKnownTargetPath(
            AgentContext context
    ){


        if(context.getObservations()==null){

            return null;

        }


        for(int i=context.getObservations().size()-1;i>=0;i--){

            Observation observation =
                    context.getObservations().get(i);


            if(!observation.isSuccess()){

                continue;

            }


            String path =
                    extractTargetPath(observation.getResult());


            if(path!=null
                    &&
                    pathExists(path)){

                return toRelativePath(path);

            }

        }


        return null;

    }


    private String extractTargetPath(
            Object result
    ){


        if(result instanceof FileContent){

            String path =
                    ((FileContent)result).getPath();


            return isTargetPath(path)
                    ? path
                    : null;

        }


        if(result instanceof List){

            for(Object item:(List<?>)result){

                if(!(item instanceof SearchMatch)){

                    continue;

                }


                SearchMatch match =
                        (SearchMatch)item;


                String path =
                        match.getPath()!=null
                                ? match.getPath()
                                : match.getRelativePath();


                if(isTargetPath(path)){

                    return path;

                }

            }

        }


        return null;

    }


    private String findSourceRootTargetPath(
            String path
    ){


        String normalized =
                normalizeWorkspaceRelativePath(path);


        if(normalized.startsWith("src/main/java/")){

            return null;

        }


        String sourcePath =
                "src/main/java/"
                        +
                        normalized;


        return pathExists(sourcePath)
                ? sourcePath
                : null;

    }


    private String normalizeWorkspaceRelativePath(
            String path
    ){


        String normalized =
                path.replace("\\","/");


        try{

            File file =
                    workspaceService.resolve(path);


            return workspaceService.relativePath(file)
                    .replace("\\","/");

        }catch(Exception e){

            return normalized;

        }

    }


    private String toRelativePath(
            String path
    ){


        try{

            return workspaceService.relativePath(
                    workspaceService.resolve(path)
            );

        }catch(Exception e){

            return path;

        }

    }


    private String validateSkillProtection(
            PlanStep step,
            AgentContext context
    ) {


        if (!(step.getInput() instanceof ToolInput)) {
            return null;
        }


        ToolInput input =
                (ToolInput) step.getInput();


        String path = input.getPath();


        if (path == null) {
            return null;
        }


        String normalized =
                path.replace("\\", "/");


        /*
         * Target保护
         */
        if ("write_file".equals(step.getTool())
                &&
                normalized.endsWith(
                        "DataPrepEventHandler.java"
                )) {


            return "Skill Protection: "
                    + "DataPrepEventHandler is existing Target, "
                            + "must use patch_file instead of write_file";
        }


        if("write_file".equals(step.getTool())
                &&
                normalized.endsWith("RestTemplateConfig.java")
                &&
                hasWrittenAnotherRestTemplateConfig(
                        normalized,
                        context
                )){


            return "Skill Protection: duplicate RestTemplateConfig is not allowed";

        }



        /*
         * patch_file必须read_file
         */
        if ("patch_file".equals(step.getTool())) {


            boolean readSuccess =
                    hasReadFile(
                            path,
                            context
                    );


            if (!readSuccess) {

                return "Skill Protection: "
                        + "patch_file requires read_file first";

            }
        }


        return null;
    }


    private boolean hasReadFile(
            String path,
            AgentContext context
    ){


        if(context.getObservations()==null){

            return false;

        }


        File target =
                workspaceService.resolve(path);


        return context.getObservations()
                .stream()
                .anyMatch(o -> {

                    if(!"read_file".equals(o.getTool())
                            ||
                            !o.isSuccess()
                            ||
                            !(o.getResult() instanceof FileContent)){

                        return false;

                    }


                    FileContent content =
                            (FileContent)o.getResult();


                    File readFile =
                            workspaceService.resolve(
                                    content.getPath()
                            );


                    return sameFile(
                            target,
                            readFile
                    );

                });

    }


    private boolean sameFile(
            File left,
            File right
    ){


        try{

            return left.getCanonicalFile()
                    .equals(
                            right.getCanonicalFile()
                    );

        }catch(Exception e){

            return false;

        }

    }


    private boolean hasWrittenAnotherRestTemplateConfig(
            String path,
            AgentContext context
    ){


        return context.getObservations()
                .stream()
                .anyMatch(o -> {


                    if(!o.isSuccess()
                            ||
                            !"write_file".equals(o.getTool())
                            ||
                            !(o.getInput() instanceof ToolInput)){

                        return false;

                    }


                    String existingPath =
                            ((ToolInput)o.getInput())
                                    .getPath();


                    if(existingPath==null){

                        return false;

                    }


                    String normalizedExisting =
                            existingPath.replace("\\","/");


                    return normalizedExisting.endsWith("RestTemplateConfig.java")
                            &&
                            !normalizedExisting.equals(path);

                });

    }

    private boolean samePath(
            String a,
            String b
    ){

        if(a==null || b==null){
            return false;
        }


        return a.replace("\\","/")
                .equals(
                        b.replace("\\","/")
                );

    }

    private boolean shouldSkipDuplicateCreate(
            PlanStep step,
            AgentContext context
    ){


        if (!"create_file".equals(step.getTool())
                &&
                !"write_file".equals(step.getTool())) {

            return false;
        }


        if(!(step.getInput() instanceof com.minicodex.tool.ToolInput)){


            return false;

        }



        com.minicodex.tool.ToolInput input =
                (com.minicodex.tool.ToolInput) step.getInput();



        String path =
                input.getPath();



        if(path==null){


            return false;

        }



        if(context.getObservations()==null){


            return false;

        }



        return context.getObservations()
                .stream()
                .anyMatch(
                        o ->
                        {
                            if(!o.isSuccess()
                                    ||
                                    o.getResult()==null){

                                return false;
                            }


                            return samePath(
                                    o.getResult()
                                            .toString(),
                                    path
                            );
                        }
                );

    }












    private ToolCallResult buildToolResult(
            String tool,
            Object result
    ){

        if(result instanceof FileOperationResult){


            FileOperationResult fileResult =
                    (FileOperationResult) result;


            if(!fileResult.isSuccess()){


                return ToolCallResult.failed(
                        tool,
                        fileResult.getMessage()
                );

            }


            return ToolCallResult.success(
                    tool,
                    result
            );

        }




        if(result instanceof Map){


            Map map =
                    (Map) result;


            Object success =
                    map.get("success");


            if(success instanceof Boolean
                    &&
                    !((Boolean) success)){


                return ToolCallResult.failed(
                        tool,
                        String.valueOf(
                                map.get("message")
                        )
                );

            }

        }



        return ToolCallResult.success(
                tool,
                result
        );

    }



}
