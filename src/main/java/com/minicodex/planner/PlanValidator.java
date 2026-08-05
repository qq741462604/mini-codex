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


        String path=input.getPath();


        if(path==null){

            return;
        }


        path=path.replace("\\","/");



        if(!path.endsWith(
                "DataPrepEventHandler.java"
        )){
            return;
        }



        /*
         * Target只能patch
         */
        if(!"patch_file".equals(step.getTool())){


            throw new RuntimeException(
                    "Target only supports patch_file"
            );

        }



        /*
         * patch必须有oldText
         */
        if(input.getOldText()==null
                || input.getOldText().length()<50){


            throw new RuntimeException(
                    "patch_file Target oldText invalid"
            );

        }


    }

    private void validateTarget1(
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



        if(!normalized.endsWith(
                "DataPrepEventHandler.java"
        )){

            return;

        }



        String tool =
                step.getTool();



        /*
         * Target允许:
         *
         * read_file
         * patch_file
         *
         */


        if("read_file".equals(tool)){

            return;

        }



        if("patch_file".equals(tool)){


            return;

        }




        /*
         * 其他全部禁止
         *
         */


        throw new RuntimeException(
                "Plan invalid: Target cannot use tool="
                        + tool
        );


    }


}