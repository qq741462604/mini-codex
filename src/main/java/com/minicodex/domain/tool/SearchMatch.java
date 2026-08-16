package com.minicodex.domain.tool;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * SearchMatch：承载工具调用的输入与结果领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class SearchMatch {


    private String path;


    private Integer lineNumber;


    private String content;


    private List<String> context;
    private String relativePath;

}