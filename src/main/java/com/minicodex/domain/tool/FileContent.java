package com.minicodex.domain.tool;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * FileContent：承载工具调用的输入与结果领域数据。 所属层：领域层。
 *
 * @author yy
 */
public class FileContent {

  /** 文件路径 */
  private String path;

  /** 开始行 */
  private Integer startLine;

  /** 结束行 */
  private Integer endLine;

  /** 文件内容 */
  private List<String> lines;
}
