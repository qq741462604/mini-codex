package com.minicodex.agent.result;


import com.minicodex.agent.observation.Observation;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;



@Component
public class CodeChangeExtractor {



    public List<CodeChange> extract(
            List<Observation> observations
    ){


        List<CodeChange> result =
                new ArrayList<>();


        if(observations==null){

            return result;

        }



        for(Observation o:observations){


            if(!o.isSuccess()){

                continue;

            }



            String tool =
                    o.getTool();



            if(!isCodeChangeTool(tool)){

                continue;

            }



            String path =
                    extractPath(
                            o.getResult()
                    );


            if(path==null
                    ||
                    path.trim().isEmpty()){

                continue;

            }



            result.add(
                    CodeChange.builder()
                            .action(
                                    convertAction(tool)
                            )
                            .path(path)
                            .success(true)
                            .build()
            );


        }



        return result;

    }







    private boolean isCodeChangeTool(
            String tool
    ){

        return "create_file".equals(tool)
                ||
                "write_file".equals(tool)
                ||
                "edit_file".equals(tool);

    }









    private String convertAction(
            String tool
    ){

        switch(tool){

            case "create_file":
                return "create";


            case "edit_file":
                return "edit";


            case "write_file":
                return "write";


            default:
                return tool;

        }

    }









    private String extractPath(
            Object result
    ){


        if(result==null){

            return null;

        }




        /*
         * 第一种：
         *
         * create success:xxx
         *
         */
        String text =
                result.toString();


        if(text.contains(":")){


            int index =
                    text.indexOf(":");



            String path =
                    text.substring(
                                    index + 1
                            )
                            .trim();



            if(!path.isEmpty()){

                return normalize(path);

            }

        }






        /*
         * 第二种：
         *
         * 如果以后返回 Map
         *
         */
        if(result instanceof java.util.Map){


            java.util.Map<?,?> map =
                    (java.util.Map<?,?>) result;



            Object path =
                    map.get("path");


            if(path!=null){

                return normalize(
                        path.toString()
                );

            }

        }






        /*
         * 第三种：
         *
         * 返回对象，有getPath()
         *
         */
        try{


            java.lang.reflect.Method method =
                    result.getClass()
                            .getMethod(
                                    "getPath"
                            );


            Object path =
                    method.invoke(
                            result
                    );


            if(path!=null){

                return normalize(
                        path.toString()
                );

            }


        }catch(Exception ignored){



        }



        return null;

    }








    private String normalize(
            String path
    ){

        return path
                .replace("\\","/");

    }



}