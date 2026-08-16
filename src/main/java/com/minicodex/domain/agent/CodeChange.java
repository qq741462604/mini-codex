package com.minicodex.domain.agent;


import lombok.Builder;
import lombok.Data;
import com.minicodex.application.agent.Agent;


@Data
@Builder
/**
 * CodeChange：承载 Agent 运行状态、观察结果与领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class CodeChange {


    /**
     * 操作类型
     *
     * create
     * edit
     * delete
     */
    private String action;



    /**
     * 文件路径
     */
    private String path;



    /**
     * 是否成功
     */
    private boolean success;


}