package com.minicodex.service;


import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.InputStreamReader;



@Service
public class GitService {



    public String commit(
            String message
    ){


        try{


            execute(
                    "git add ."
            );


            execute(
                    "git commit -m \""
                            + message
                            +"\""
            );


            return execute(
                    "git rev-parse HEAD"
            );


        }catch(Exception e){

            throw new RuntimeException(e);

        }


    }





    public boolean reset(
            String commitId
    ){


        try{


            execute(
                    "git reset --hard "
                            + commitId
            );


            return true;


        }catch(Exception e){


            return false;

        }


    }





    private String execute(
            String command
    ) throws Exception{


        Process process =
                Runtime.getRuntime()
                        .exec(command);



        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                process.getInputStream()
                        )
                );


        StringBuilder sb =
                new StringBuilder();



        String line;


        while(
                (line=reader.readLine())
                        !=null
        ){

            sb.append(line);

        }


        process.waitFor();


        return sb.toString();


    }


}