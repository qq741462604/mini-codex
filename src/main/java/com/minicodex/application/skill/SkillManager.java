package com.minicodex.application.skill;

import com.minicodex.domain.skill.Skill;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * SkillManager：负责匹配并组装可执行技能上下文。 所属层：应用层。
 *
 * @author yy
 */
public class SkillManager {

  private final SkillMatcher matcher;

  public String buildContext(String task) {
    List<Skill> skills = matcher.match(task);
    StringBuilder sb = new StringBuilder();
    for (Skill skill : skills) {
      sb.append("\n## Skill:").append(skill.getName()).append("\n");

      if (skill.getTarget() != null) {
        sb.append("\nTarget:\n");
        sb.append("class=").append(skill.getTarget().getClassName()).append("\n");
        sb.append("method=").append(skill.getTarget().getMethodName()).append("\n");
      }

      sb.append("Rules:\n");
      for (String rule : skill.getRules()) {
        sb.append("- ").append(rule).append("\n");
      }

      sb.append("\nImplementation:\n");
      String implementation = String.join("\n", skill.getImplementation());
      sb.append(limit(implementation, 1500));

      sb.append("\n\nForbidden:\n");
      for (String forbidden : skill.getForbidden()) {
        sb.append("- ").append(forbidden).append("\n");
      }
    }
    return sb.toString();
  }

  private String limit(String text, int max) {
    if (text == null) {
      return "";
    }
    if (text.length() > max) {
      return text.substring(0, max) + "\n...[truncated]";
    }
    return text;
  }
}
