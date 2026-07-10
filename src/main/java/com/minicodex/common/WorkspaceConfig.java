package com.minicodex.common;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "codex.workspace")
public class WorkspaceConfig {


    private String path;


}