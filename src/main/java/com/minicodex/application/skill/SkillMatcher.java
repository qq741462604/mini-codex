package com.minicodex.application.skill;

import com.minicodex.application.port.SkillRepository;
import com.minicodex.domain.skill.Skill;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * SkillMatcher：负责匹配并组装可执行技能上下文。 所属层：应用层。
 *
 * @author yy
 */
public class SkillMatcher {

  private final SkillRepository loader;
  private List<Skill> cache;

  public boolean hasBusinessMatch(String task) {
    if (task == null || task.trim().isEmpty()) {
      return false;
    }
    if (cache == null) {
      cache = loader.load();
    }

    for (Skill skill : cache) {
      if (skill.isAlwaysApply()) {
        continue;
      }
      if (matches(task, skill)) {
        return true;
      }
    }
    return false;
  }

  public List<Skill> match(String task) {
    if (cache == null) {
      cache = loader.load();
    }

    Set<Skill> result = new LinkedHashSet<>();
    for (Skill skill : cache) {
      if (skill.isAlwaysApply()) {
        result.add(skill);
      }
    }
    if (task != null) {
      for (Skill skill : cache) {
        if (skill.isAlwaysApply()) {
          continue;
        }
        if (matches(task, skill)) {
          result.add(skill);
        }
      }
    }

    List<Skill> skills = new ArrayList<>(result);
    log.info("effective skills={}", buildSkillNames(skills));
    return skills;
  }

  private boolean matches(String task, Skill skill) {
    if (task == null || skill == null || skill.getKeywords() == null) {
      return false;
    }
    String text = task.toLowerCase();
    for (String keyword : skill.getKeywords()) {
      if (keyword != null && text.contains(keyword.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private String buildSkillNames(List<Skill> skills) {
    List<String> names = new ArrayList<>();
    for (Skill skill : skills) {
      names.add(skill.getName());
    }
    return names.toString();
  }
}
