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


            parseSections(
                    skill
            );


            return skill;



        }catch(Exception e){

            throw new RuntimeException(e);

        }

    }

    private void parseSections(
            Skill skill
    ){


        String content =
                skill.getContent();



        String mode=null;



        for(String line:
                content.split("\n")){


            line=line.trim();



            if(line.isEmpty()){

                continue;

            }



            if(line.startsWith("keywords")){

                mode="keywords";
                continue;

            }


            if(line.startsWith("rules")){

                mode="rules";
                continue;

            }


            if(line.startsWith("forbidden")){

                mode="forbidden";
                continue;

            }



            if(line.startsWith("#")){

                continue;

            }



            if("keywords".equals(mode)){


                skill.getKeywords()
                        .add(line);


            }


            if("rules".equals(mode)){


                skill.getRules()
                        .add(line);


            }



            if("forbidden".equals(mode)){


                skill.getForbidden()
                        .add(line);


            }


        }


    }

    private List<String> extractKeywords(
            String content
    ){


        List<String> result =
                new ArrayList<>();


        boolean start=false;


        for(String line:
                content.split("\n")){


            line=line.trim();


            if(line.startsWith("keywords")){

                start=true;

                continue;

            }


            if(start){


                if(line.startsWith("#")
                        ||
                        line.contains(":")){


                    break;

                }


                if(!line.isEmpty()){


                    result.add(
                            line.replace("-","")
                                    .trim()
                    );

                }

            }

        }


        return result;

    }

}