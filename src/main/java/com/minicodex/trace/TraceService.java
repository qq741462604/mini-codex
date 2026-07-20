package com.minicodex.trace;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TraceService {




    public AgentTrace start(
            String task
    ){


        AgentTrace trace =
                new AgentTrace();


        trace.setTask(task);


        trace.setStartTime(
                System.currentTimeMillis()
        );


        return trace;

    }





    public void finish(
            AgentTrace trace
    ){


        trace.setEndTime(
                System.currentTimeMillis()
        );


        print(trace);


    }





    public void print(
            AgentTrace trace
    ){
        log.info(
                "TRACE STEP SIZE={}",
                trace.getSteps().size()
        );

        log.info(
                "\n========== MINI CODEX TRACE =========="
        );


        log.info(
                "TASK:\n{}",
                trace.getTask()
        );



        for(TraceStep step:
                trace.getSteps()){


            log.info(
                    "\nTYPE:{}\nNAME:{}\nINPUT:{}\nOUTPUT:{}\nCOST:{}ms",
                    step.getType(),
                    step.getName(),
                    step.getInput(),
                    step.getOutput(),
                    step.getCost()
            );


        }



        log.info(
                "TOTAL:{}ms",
                trace.getEndTime()
                        -
                        trace.getStartTime()
        );


        log.info(
                "======================================"
        );


    }


}