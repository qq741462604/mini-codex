package com.minicodex.application.port;

import com.minicodex.domain.skill.Skill;
import java.util.List;

/**
 * 定义应用层读取 skill 配置的边界。 所属层：应用层。
 *
 * @author yy
 */
public interface SkillRepository {

  List<Skill> load();
}
