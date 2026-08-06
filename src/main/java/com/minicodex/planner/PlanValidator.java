package com.minicodex.planner;


import com.minicodex.tool.ToolInput;
import org.springframework.stereotype.Component;


@Component
public class PlanValidator {


    public void validate(CodePlan plan) {


        if(plan == null
                || plan.getSteps()==null){

            return;
        }


        for(PlanStep step: plan.getSteps()){

            validateTarget(step);

        }

    }



    private void validateTarget(
            PlanStep step
    ){


        if(!(step.getInput()
                instanceof ToolInput)){

            return;
        }



        ToolInput input =
                (ToolInput) step.getInput();



        String path =
                input.getPath();



        if(path==null){

            return;

        }



        String normalized =
                path.replace("\\","/");



        /*
         * Target文件规则
         */
        if(!normalized.endsWith(
                "DataPrepEventHandler.java"
        )){

            return;

        }



        String tool =
                step.getTool();



        /*
         * Target允许读取
         */
        if("read_file".equals(tool)){

            return;

        }



        /*
         * Target允许patch
         */
        if("patch_file".equals(tool)){


            if(input.getOldText()==null
                    || input.getOldText().length()<50){

                throw new RuntimeException(
                        "patch_file Target oldText invalid"
                );

            }


            return;

        }



        /*
         * Target禁止其他修改方式
         */
        throw new RuntimeException(
                "Target cannot use tool="
                        + tool
        );


    }


}