package com.minicodex.infrastructure.tool;

import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.tool.FileOperationResult;
import com.minicodex.domain.tool.ToolInput;
import com.minicodex.infrastructure.workspace.WorkspaceService;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * CreateFileTool：提供受工作区约束的具体文件操作工具。 所属层：基础设施层。
 *
 * @author yy
 */
public class CreateFileTool extends BaseTool {

  private final WorkspaceService workspaceService;

  @Override
  public String name() {

    return "create_file";
  }

  @Override
  public String description() {

    return "create new source file";
  }

  @Override
  public Object execute(Object input, AgentContext context) throws Exception {

    ToolInput toolInput = (ToolInput) input;

    File file = workspaceService.resolve(toolInput.getPath());

    File parent = file.getParentFile();

    if (parent != null && !parent.exists()) {

      parent.mkdirs();
    }

    boolean exists = file.exists();

    /*
     *
     * Agent场景:
     *
     * 已存在也允许覆盖
     *
     */
    Files.write(file.toPath(), toolInput.getContent().getBytes(StandardCharsets.UTF_8));

    return FileOperationResult.builder()
        .action(exists ? "update" : "create")
        .path(workspaceService.relativePath(file))
        .success(true)
        .message(exists ? "file exists overwrite success" : "create success")
        .build();
  }
}
