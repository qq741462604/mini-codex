package com.minicodex.skill;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class SkillManager {


    private final SkillMatcher matcher;



    public String buildContext(
            String task
    ){


        List<Skill> skills =
                matcher.match(task);



        StringBuilder sb =
                new StringBuilder();



        for(Skill skill:skills){


            sb.append("\n## Skill:")
                    .append(skill.getName())
                    .append("\n");



            sb.append("Rules:\n");


            for(String rule:
                    skill.getRules()){


                sb.append("- ")
                        .append(rule)
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