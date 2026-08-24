package com.minicodex.application.port;

import com.minicodex.domain.memory.Memory;
import java.util.List;

/**
 * MemoryStore：定义应用层调用外部能力的抽象边界。 所属层：应用层。
 *
 * @author yy
 */
public interface MemoryStore {

  void save(Memory memory);

  List<Memory> query(String keyword);

  void clear();
}
