package com.minicodex.infrastructure.memory;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AgentMemoryMapper：定义 Agent 记忆的持久化操作。 所属层：基础设施层。
 *
 * @author summer
 * @date 2026-08-25 00:00:00
 */
@Mapper
public interface AgentMemoryMapper {

  void insert(MemoryRecord memoryRecord);

  List<MemoryRecord> selectByKeyword(
      @Param("keyword") String keyword, @Param("limit") int limit);

  void deleteAll();
}
