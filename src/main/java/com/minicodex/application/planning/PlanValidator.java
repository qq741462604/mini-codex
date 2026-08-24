package com.minicodex.application.planning;

import com.minicodex.application.port.WorkspacePort;
import com.minicodex.domain.plan.CodePlan;
import com.minicodex.domain.plan.PlanStep;
import com.minicodex.domain.tool.ToolInput;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * PlanValidator：负责生成和校验代码改造计划。 所属层：应用层。
 *
 * @author yy
 */
public class PlanValidator {

  private final WorkspacePort workspaceService;

  public void validate(CodePlan plan) {
    if (plan == null || plan.getSteps() == null) {
      return;
    }
    for (PlanStep step : plan.getSteps()) {
      validateFileStep(step);
    }
  }

  private void validateFileStep(PlanStep step) {
    if (!(step.getInput() instanceof ToolInput)) {
      return;
    }
    ToolInput input = (ToolInput) step.getInput();
    String path = input.getPath();
    String tool = step.getTool();
    if (isFileMutationTool(tool) && (path == null || path.trim().isEmpty())) {
      throw new RuntimeException(tool + " path is empty");
    }
    if (isCreateTool(tool)) {
      validateCreateTool(path, tool);
      return;
    }
    if ("patch_file".equals(tool)) {
      validatePatchTool(input);
    }
  }

  private void validateCreateTool(String path, String tool) {
    if (isExistingFile(path)) {
      throw new RuntimeException(
          tool + " cannot modify existing file, use patch_file path=" + path);
    }
  }

  private void validatePatchTool(ToolInput input) {
    String path = input.getPath();
    if (!isExistingFile(path)) {
      throw new RuntimeException("patch_file target file not found path=" + path);
    }
    if (input.getNewText() == null) {
      throw new RuntimeException("patch_file newText is null path=" + path);
    }
    if (isEmptyFile(path)) {
      return;
    }
    if (input.getOldText() == null || input.getOldText().trim().isEmpty()) {
      throw new RuntimeException("patch_file oldText is empty path=" + path);
    }
  }

  private boolean isCreateTool(String tool) {
    return "write_file".equals(tool) || "create_file".equals(tool);
  }

  private boolean isFileMutationTool(String tool) {
    return isCreateTool(tool) || "patch_file".equals(tool);
  }

  private boolean isExistingFile(String path) {
    try {
      File file = workspaceService.resolve(path);
      return file.exists() && file.isFile();
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isEmptyFile(String path) {
    try {
      File file = workspaceService.resolve(path);
      return file.exists() && file.isFile() && file.length() == 0;
    } catch (Exception e) {
      return false;
    }
  }
}
