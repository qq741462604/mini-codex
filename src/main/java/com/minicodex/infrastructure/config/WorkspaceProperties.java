package com.minicodex.infrastructure.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "codex.workspace")
/**
 * WorkspaceProperties：提供外部配置的读取与绑定能力。
 * 所属层：基础设施层。
 *
 * @author yy
 */
public class WorkspaceProperties {
    /**
     * 用户代码根目录
     */
    private String root;
}
