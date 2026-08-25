package com.minicodex.application.memory;

import com.minicodex.application.port.MemoryStore;
import com.minicodex.domain.memory.Memory;
import com.minicodex.domain.memory.MemoryType;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * AgentMemoryService：负责保存 Agent 运行记忆。 所属层：应用层。
 *
 * @author yy
 */
public class AgentMemoryService {

  private final MemoryStore memoryStore;

  public void saveTask(String task, String content) {

    memoryStore.save(
        Memory.builder()
            .key("task_" + System.currentTimeMillis())
            .content("TASK:\n" + task + "\nRESULT:\n" + content)
            .type(MemoryType.TASK)
            .createTime(new Date())
            .build());
  }

  public void saveVerifyError(String task, List<String> errors) {

    memoryStore.save(
        Memory.builder()
            .key(task + "_verify")
            .type(MemoryType.VERIFY_ERROR)
            .content(String.join("\n", errors))
            .createTime(new Date())
            .build());
  }
}
