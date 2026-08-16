package com.minicodex.domain.plan;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
/**
 * PlanStep：承载代码改造计划及其执行步骤。
 * 所属层：领域层。
 *
 * @author yy
 */
public class PlanStep {


    /**
     * 步骤编号
     */
    private Integer order;



    /**
     * 使用的工具
     */
    private String tool;



    /**
     * 工具输入
     */
    private Object input;



    /**
     * 描述
     */
    private String description;


}