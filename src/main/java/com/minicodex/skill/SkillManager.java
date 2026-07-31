package com.minicodex.skill;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillManager {


    private final SkillMatcher matcher;



    public String buildContext(
            String task
    ){


        List<Skill> skills =
                matcher.match(task);

        log.info(
                "matched skills size={}",
                skills.size()
        );

        StringBuilder sb =
                new StringBuilder();



        for(Skill skill:skills){
            log.info(
                    "skill name={} keywords={}",
                    skill.getName(),
                    skill.getKeywords()
            );

            sb.append("\n## Skill:")
                    .append(skill.getName())
                    .append("\n");

            if(skill.getTarget()!=null){


                sb.append("\nTarget:\n");


                sb.append(
                                "class="
                        )
                        .append(
                                skill.getTarget()
                                        .getClassName()
                        )
                        .append("\n");


                sb.append(
                                "method="
                        )
                        .append(
                                skill.getTarget()
                                        .getMethodName()
                        )
                        .append("\n");

            }

            sb.append("Rules:\n");


            for(String rule:
                    skill.getRules()){


                sb.append("- ")
                        .append(rule)
                        .append("\n");

            }


            sb.append("\n请严格执行以下Implementation:\n");


            for(String item:
                    skill.getImplementation()){


                sb.append(item)
                        .append("\n");

            }
            sb.append("\nForbidden:\n");


            for(String f:
                    skill.getForbidden()){


                sb.append("- ")
                        .append(f)
                        .append("\n");

            }


        }



        return sb.toString();

    }


}