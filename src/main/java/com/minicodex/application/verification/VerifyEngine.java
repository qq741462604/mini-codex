package com.minicodex.application.verification;

import com.minicodex.domain.agent.AgentContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * VerifyEngine：负责代码变更后的验证与修复反馈。 所属层：应用层。
 *
 * @author yy
 */
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
