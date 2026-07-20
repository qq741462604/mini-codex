package com.minicodex.skill;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class SkillManager {


    private final SkillLoader loader;



    public String buildContext(){


        List<Skill> skills =
                loader.load();



        StringBuilder sb =
                new StringBuilder();



        for(Skill skill:skills){


            sb.append("\n## ")
                    .append(skill.getName())
                    .append("\n");


            sb.append(skill.getContent())
                    .append("\n");

        }


        return sb.toString();

    }


}