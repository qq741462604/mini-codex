package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;



@Component
@RequiredArgsConstructor
public class ReadFileTool extends BaseTool {


    private final WorkspaceService workspaceService;



    private static final int DEFAULT_LIMIT = 200;



    @Override
    public String name() {

        return "read_file";

    }



    @Override
    public String description() {

        return "读取项目文件内容";

    }




    @Override
    public void validate(
            Object input
    ){


        ToolInput toolInput =
                (ToolInput) input;



        if(toolInput.getPath()==null
                ||
                toolInput.getPath().trim().isEmpty()){


            throw new IllegalArgumentException(
                    "read_file path empty"
            );

        }


    }





    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {


        logExecute(input);



        ToolInput toolInput =
                (ToolInput) input;



        File file =
                resolveFile(
                        toolInput.getPath()
                );



        if(!file.exists()
                ||
                !file.isFile()){


            throw new RuntimeException(
                    "file not found:"
                            +
                            file.getAbsolutePath()
            );

        }



        int startLine =
                toolInput.getStartLine()==null
                        ?
                        1
                        :
                        toolInput.getStartLine();



        int endLine =
                toolInput.getEndLine()==null
                        ?
                        startLine + DEFAULT_LIMIT
                        :
                        toolInput.getEndLine();



        return read(
                file,
                startLine,
                endLine
        );


    }








    private File resolveFile(
            String path
    ){


//        File file =  new File(path);
        File file =  workspaceService.resolve(path);


        if(file.isAbsolute()){


            return file;

        }



        return workspaceService.resolve(
                path
        );

    }








    private FileContent read(
            File file,
            int startLine,
            int endLine
    ){


        BufferedReader reader=null;


        List<String> result =
                new ArrayList<>();



        try{


            reader =
                    new BufferedReader(
                            new FileReader(file)
                    );



            String line;


            int current=1;



            while(
                    (line=reader.readLine())
                            !=null
            ){



                if(current>=startLine
                        &&
                        current<=endLine){


                    result.add(
                            current
                                    +
                                    ": "
                                    +
                                    line
                    );


                }



                if(current>endLine){

                    break;

                }


                current++;


            }




            return FileContent.builder()
                    .path(
                            file.getCanonicalPath()
                    )
                    .startLine(
                            startLine
                    )
                    .endLine(
                            endLine
                    )
                    .lines(
                            result
                    )
                    .build();



        }catch(Exception e){


            throw new RuntimeException(
                    e
            );


        }finally{


            try{

                if(reader!=null){

                    reader.close();

                }

            }catch(Exception ignored){

            }

        }


    }


}