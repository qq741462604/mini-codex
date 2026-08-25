package com.minicodex.infrastructure.memory;

import com.minicodex.application.port.MemoryStore;
import com.minicodex.domain.memory.Memory;
import java.util.ArrayList;
import java.util.List;

/**
 * SimpleMemoryStore：提供记忆存储的具体实现。 所属层：基础设施层。
 *
 * @author yy
 */
public class SimpleMemoryStore implements MemoryStore {

  private final List<Memory> memories = new ArrayList<>();

  @Override
  public void save(Memory memory) {

    memories.add(memory);
  }

  @Override
  public List<Memory> query(String keyword) {

    List<Memory> result = new ArrayList<>();

    for (Memory memory : memories) {

      if (memory.getContent().contains(keyword)) {

        result.add(memory);
      }
    }

    return result;
  }

  @Override
  public void clear() {

    memories.clear();
  }
}
