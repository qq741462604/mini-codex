package com.minicodex.infrastructure.tool;

import com.minicodex.application.port.AgentTool;

/**
 * BaseTool：提供受工作区约束的具体文件操作工具。
 * 所属层：基础设施层。
 *
 * @author yy
 */
public abstract class BaseTool implements AgentTool {

    protected void logExecute(Object input) {
        // 预留扩展点，默认不打印工具输入，避免频繁文件操作刷日志。
    }
}
