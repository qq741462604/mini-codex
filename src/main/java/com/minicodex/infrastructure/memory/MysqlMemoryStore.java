package com.minicodex.infrastructure.memory;

import com.minicodex.application.port.MemoryStore;
import com.minicodex.domain.memory.Memory;
import com.minicodex.domain.memory.MemoryType;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MysqlMemoryStore：通过 MyBatis 持久化 Agent 记忆。 所属层：基础设施层。
 *
 * @author summer
 * @date 2026-08-25 00:00:00
 */
@Component
@RequiredArgsConstructor
public class MysqlMemoryStore implements MemoryStore {

  private static final int QUERY_LIMIT = 20;

  private final AgentMemoryMapper agentMemoryMapper;

  @Override
  public void save(Memory memory) {
    agentMemoryMapper.insert(toRecord(memory));
  }

  @Override
  public List<Memory> query(String keyword) {
    return agentMemoryMapper.selectByKeyword(keyword, QUERY_LIMIT).stream()
        .map(this::toMemory)
        .collect(Collectors.toList());
  }

  @Override
  public void clear() {
    agentMemoryMapper.deleteAll();
  }

  private MemoryRecord toRecord(Memory memory) {
    MemoryRecord memoryRecord = new MemoryRecord();
    memoryRecord.setKey(memory.getKey());
    memoryRecord.setContent(memory.getContent());
    memoryRecord.setType(memory.getType().name());
    memoryRecord.setCreateTime(memory.getCreateTime());
    return memoryRecord;
  }

  private Memory toMemory(MemoryRecord memoryRecord) {
    return Memory.builder()
        .key(memoryRecord.getKey())
        .content(memoryRecord.getContent())
        .type(MemoryType.valueOf(memoryRecord.getType()))
        .createTime(new Date(memoryRecord.getCreateTime().getTime()))
        .build();
  }
}
