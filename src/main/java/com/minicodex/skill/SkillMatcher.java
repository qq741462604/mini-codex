package com.minicodex.skill;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;



@Component
@RequiredArgsConstructor
public class SkillMatcher {


    private final SkillLoader loader;


    private List<Skill> cache;



    public List<Skill> match(
            String task
    ){


        if(cache==null){

            cache =
                    loader.load();

        }


        List<Skill> result =
                new ArrayList<>();


        if(task==null){

            return result;

        }


        String text =
                task.toLowerCase();



        for(Skill skill:cache){


            for(String keyword:
                    skill.getKeywords()){


                if(text.contains(
                        keyword.toLowerCase()
                )){


                    result.add(skill);

                    break;

                }

            }


        }


        return result;

    }


}