package com.minicodex.domain.plan;


import lombok.Builder;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;



@Data
@Builder
/**
 * CodePlan：承载代码改造计划及其执行步骤。
 * 所属层：领域层。
 *
 * @author yy
 */
public class CodePlan {



    /**
     * 用户任务
     */
    private String task;



    /**
     * 执行步骤
     */
    @Builder.Default
    private List<PlanStep> steps =
            new ArrayList<>();



}