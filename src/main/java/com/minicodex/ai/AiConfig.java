package com.minicodex.ai;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Data
@Component
@ConfigurationProperties(prefix = "codex.ai")
public class AiConfig {


    /**
     * API地址
     */
    private String url;


    /**
     * API Key
     */
    private String apiKey;


    /**
     * 模型
     */
    private String model;



}