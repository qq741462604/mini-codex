package com.minicodex.verify;


import com.minicodex.agent.AgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyEngine {


    private final CodeValidator validator;



    public VerifyResult verify(
            AgentContext context
    ){


        log.info(
                "===== VERIFY START ====="
        );


        return buildResult(
                validator.validate(
                        context
                )
        );


    }



    private VerifyResult buildResult(
            java.util.List<String> errors
    ){


        if(errors.isEmpty()){


            log.info(
                    "verify success"
            );


            return VerifyResult.success();

        }



        log.warn(
                "verify failed errors={}",
                errors
        );


        return VerifyResult.failed(
                errors
        );

    }


}