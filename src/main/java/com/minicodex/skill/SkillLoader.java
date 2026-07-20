package com.minicodex.skill;


import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.*;
import java.util.ArrayList;
import java.util.List;



@Component
@RequiredArgsConstructor
public class SkillLoader {


    private final WorkspaceService workspaceService;



    public List<Skill> load(){


        List<Skill> result =
                new ArrayList<>();


        File skillDir =
                workspaceService.resolve(
                        ".ai/skills"
                );


        if(!skillDir.exists()){

            return result;

        }


        scan(
                skillDir,
                result
        );


        return result;

    }





    private void scan(
            File file,
            List<Skill> result
    ){


        if(file.isDirectory()){


            File[] files =
                    file.listFiles();


            if(files!=null){

                for(File child:files){

                    scan(
                            child,
                            result
                    );

                }

            }


            return;

        }



        if(!file.getName()
                .endsWith(".md")){


            return;

        }




        result.add(
                parse(file)
        );


    }





    private Skill parse(
            File file
    ){


        try(
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(file)
                        )
        ){


            StringBuilder builder =
                    new StringBuilder();



            String line;


            while(
                    (line=reader.readLine())
                            !=null
            ){

                builder.append(line)
                        .append("\n");

            }



            Skill skill =
                    new Skill();


            skill.setName(
                    file.getParentFile()
                            .getName()
            );


            skill.setContent(
                    builder.toString()
            );


            return skill;



        }catch(Exception e){


            throw new RuntimeException(e);

        }


    }


}