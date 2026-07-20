package com.minicodex.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Data
@Component
@ConfigurationProperties(prefix = "codex.workspace")
public class WorkspaceProperties {
    @PostConstruct
    public void init(){

        System.out.println(
                "workspace root="
                        + root
        );

    }

    /**
     * 用户代码根目录
     */
    private String root;


}