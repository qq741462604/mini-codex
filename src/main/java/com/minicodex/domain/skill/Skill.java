package com.minicodex.domain.skill;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
/**
 * Skill：承载技能及其目标的领域数据。 所属层：领域层。
 *
 * @author yy
 */
public class Skill {

  /** skill名称 */
  private String name;

  /** 原始内容 */
  private String content;

  /** 匹配关键词 */
  private List<String> keywords = new ArrayList<>();

  /** 强制规则 */
  private List<String> rules = new ArrayList<>();

  /** 禁止行为 */
  private List<String> forbidden = new ArrayList<>();

  private List<String> implementation = new ArrayList<>();

  /** 是否每次请求都生效 */
  private boolean alwaysApply;

  /** Skill指定修改目标 */
  private SkillTarget target;

  private String template;
}
