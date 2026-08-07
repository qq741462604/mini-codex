package com.minicodex.skill;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillMatcher {

    private final SkillLoader loader;
    private List<Skill> cache;

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
            String text = task.toLowerCase();
            for (Skill skill : cache) {
                if (skill.isAlwaysApply()) {
                    continue;
                }
                for (String keyword : skill.getKeywords()) {
                    if (text.contains(keyword.toLowerCase())) {
                        result.add(skill);
                        break;
                    }
                }
            }
        }

        List<Skill> skills = new ArrayList<>(result);
        log.info("effective skills={}", buildSkillNames(skills));
        return skills;
    }

    private String buildSkillNames(List<Skill> skills) {
        List<String> names = new ArrayList<>();
        for (Skill skill : skills) {
            names.add(skill.getName());
        }
        return names.toString();
    }
}
