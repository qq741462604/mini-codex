package com.minicodex.verify;

import com.minicodex.agent.AgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyEngine {

    private final CodeValidator validator;
    private final VerifyFileLoader loader;

    public VerifyResult verify(AgentContext context) {
        loader.loadChangedFiles(context);
        return buildResult(validator.validate(context));
    }

    private VerifyResult buildResult(List<String> errors) {
        if (errors.isEmpty()) {
            return VerifyResult.success();
        }
        log.warn("verify failed errors={}", errors);
        return VerifyResult.failed(errors);
    }
}
