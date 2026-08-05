package com.minicodex.planner;


import com.minicodex.tool.ToolInput;
import org.springframework.stereotype.Component;


@Component
public class PlanValidator {


    public void validate(CodePlan plan) {


        if (plan == null
                || plan.getSteps() == null) {

            return;
        }


        for (PlanStep step : plan.getSteps()) {


            validateTarget(step);

        }

    }



    private void validateTarget(
            PlanStep step
    ) {


        if (!(step.getInput()
                instanceof ToolInput)) {

            return;

        }


        ToolInput input =
                (ToolInput) step.getInput();


        String path =
                input.getPath();


        if (path == null) {

            return;

        }


        String normalized =
                path.replace("\\", "/");



        /*
         * 已存在Target禁止write_file
         */
        if ("write_file".equals(step.getTool())
                &&
                normalized.endsWith(
                        "DataPrepEventHandler.java"
                )) {


            throw new RuntimeException(
                    "Plan invalid: "
                            + "DataPrepEventHandler is existing Target, "
                            + "must use patch_file"
            );

        }



        /*
         * 禁止Target创建
         */
        if ("create_file".equals(step.getTool())
                &&
                normalized.endsWith(
                        "DataPrepEventHandler.java"
                )) {


            throw new RuntimeException(
                    "Plan invalid: "
                            + "cannot create existing Target"
            );

        }

    }

}