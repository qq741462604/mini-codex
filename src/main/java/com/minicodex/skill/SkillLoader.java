package com.minicodex.skill;


import com.minicodex.config.AgentHomeService;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.*;
import java.util.ArrayList;
import java.util.List;



@Component
@RequiredArgsConstructor
@Slf4j
public class SkillLoader {


    private final AgentHomeService agentHomeService;



    public List<Skill> load(){


        List<Skill> result =
                new ArrayList<>();


        File skillDir =
                agentHomeService.resolve(
                        ".ai/skills"
                );

        log.info(
                "load skill path={}",
                skillDir.getAbsolutePath()
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

        log.info(
                "scan file={}",
                file.getAbsolutePath()
        );

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


        log.info(
                "load skill file={}",
                file.getAbsolutePath()
        );


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
            log.info(
                    "SKILL CONTENT={} keywords={} target={} implementation={}",
                    skill.getName(),
                    skill.getKeywords(),
                    skill.getTarget(),
                    skill.getImplementation().size()
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

        log.info(
                "========== SKILL CONTENT ==========\n{}",
                content
        );

        String mode=null;



        for(String line:
                content.split("\n")){


            line=line.trim();



            if(line.isEmpty()){

                continue;

            }



            if(line.toLowerCase()
                    .startsWith("keywords")){

                mode="keywords";
                continue;
            }


            if(line.toLowerCase().startsWith("rules")){

                mode="rules";
                continue;

            }


            if(line.toLowerCase().startsWith("forbidden")){

                mode="forbidden";
                continue;

            }

            if(line.toLowerCase()
                    .startsWith("implementation")){
                mode="implementation";
                continue;
            }


            if(line.toLowerCase()
                    .startsWith("target")){

                mode="target";

                continue;

            }

            if(line.startsWith("#")){

                continue;

            }



            if("keywords".equals(mode)){


                skill.getKeywords()
                        .add(cleanLine(line));


            }


            if("rules".equals(mode)){


                skill.getRules()
                        .add(line);


            }



            if("forbidden".equals(mode)){


                skill.getForbidden()
                        .add(line);


            }


            if("implementation".equals(mode)){

                skill.getImplementation()
                        .add(line);

            }
            if("target".equals(mode)){


                if(line.startsWith("class")){


                    String value =
                            line.substring(
                                            line.indexOf(":")+1
                                    )
                                    .trim();


                    if(skill.getTarget()==null){

                        skill.setTarget(
                                new SkillTarget()
                        );

                    }


                    skill.getTarget()
                            .setClassName(value);


                }



                if(line.startsWith("method")){


                    String value =
                            line.substring(
                                            line.indexOf(":")+1
                                    )
                                    .trim();


                    if(skill.getTarget()==null){

                        skill.setTarget(
                                new SkillTarget()
                        );

                    }


                    skill.getTarget()
                            .setMethodName(value);


                }

            }
        }


    }


    private String cleanLine(
            String line
    ){

        return line.replace("-","")
                .trim();

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