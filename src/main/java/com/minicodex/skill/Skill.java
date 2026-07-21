package com.minicodex.skill;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class Skill {


    /**
     * skill名称
     */
    private String name;


    /**
     * 原始内容
     */
    private String content;



    /**
     * 匹配关键词
     */
    private List<String> keywords =
            new ArrayList<>();



    /**
     * 强制规则
     */
    private List<String> rules =
            new ArrayList<>();



    /**
     * 禁止行为
     */
    private List<String> forbidden =
            new ArrayList<>();



}