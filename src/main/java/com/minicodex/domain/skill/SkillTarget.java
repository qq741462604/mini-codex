package com.minicodex.domain.skill;


import lombok.Data;


@Data
/**
 * SkillTarget：承载技能及其目标的领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class SkillTarget {


    /**
     * 目标类
     */
    private String className;


    /**
     * 目标方法
     */
    private String methodName;


}