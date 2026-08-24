package com.minicodex.application.verification;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * VerifyResult：负责代码变更后的验证与修复反馈。 所属层：应用层。
 *
 * @author yy
 */
public class VerifyResult {

  private boolean success;

  @Builder.Default private List<String> errors = new ArrayList<>();

  /** 失败文件 */
  @Builder.Default private List<String> failedFiles = new ArrayList<>();

  public static VerifyResult success() {

    return VerifyResult.builder().success(true).build();
  }

  public static VerifyResult failed(List<String> errors) {

    return VerifyResult.builder().success(false).errors(errors).build();
  }
}
