package com.minicodex.domain.tool;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
/**
 * FileOperationResult：承载工具调用的输入与结果领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class FileOperationResult {


    private String action;


    private String path;


    private boolean success;
    private String message;

}