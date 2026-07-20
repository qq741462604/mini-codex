package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.workspace.WorkspaceIgnoreMatcher;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class SearchCodeTool extends BaseTool {


    private final WorkspaceService workspaceService;

    private final WorkspaceIgnoreMatcher matcher;

    @Override
    public String name() {

        return "search_code";

    }



    @Override
    public String description() {

        return "搜索项目代码内容，返回匹配文件、行号以及上下文";

    }




    @Override
    public void validate(
            Object input
    ){


        ToolInput toolInput =
                (ToolInput) input;



        if(toolInput.getKeyword()==null
                ||
                toolInput.getKeyword().trim().isEmpty()){


            throw new IllegalArgumentException(
                    "search_code keyword is empty"
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



        String keyword =
                toolInput.getKeyword();



        String path =
                toolInput.getPath();



        if(path==null
                ||
                path.trim().isEmpty()){


            path=".";

        }





        File root =
                workspaceService.resolve(
                        path
                );



        if(!root.exists()){


            throw new RuntimeException(
                    "path not exists:"
                            +
                            root.getAbsolutePath()
            );

        }







        List<SearchMatch> result =
                new ArrayList<>();



        scan(
                root,
                keyword,
                result
        );



        return result;

    }







    private void scan(
            File file,
            String keyword,
            List<SearchMatch> result
    ){



        if(file==null){

            return;

        }



        if(matcher.match(file)){

            return;

        }




        if(file.isDirectory()){


            File[] files =
                    file.listFiles();



            if(files!=null){


                for(File child:files){


                    scan(
                            child,
                            keyword,
                            result
                    );


                }

            }



            return;

        }





        searchFile(
                file,
                keyword,
                result
        );


    }









    private void searchFile(
            File file,
            String keyword,
            List<SearchMatch> result
    ){



        BufferedReader reader=null;



        try{


            List<String> lines =
                    new ArrayList<>();


            reader =
                    new BufferedReader(
                            new FileReader(file)
                    );



            String line;



            while(
                    (line=reader.readLine())
                            !=null
            ){


                lines.add(line);


            }



            for(int i=0;i<lines.size();i++){


                String current =
                        lines.get(i);

                Pattern pattern =
                        Pattern.compile(
                                keyword
                        );
                if(pattern.matcher(current).find()
                        && !isImportLine(current)
                        && !isPackageLine(current)
                ){
//                if(current.contains(keyword)
//                        && !isImportLine(current)
//                        && !isPackageLine(current)
//                ){



                    SearchMatch match =
                            SearchMatch.builder()
                                    .path(
                                            workspaceService.relativePath(file)
                                    )
                                    .lineNumber(
                                            i+1
                                    )
                                    .content(
                                            current.trim()
                                    )
                                    .context(
                                            buildContext(
                                                    lines,
                                                    i
                                            )
                                    )
                                    .build();



                    result.add(match);



                    // 一个文件只返回一次
                    break;

                }


            }



        }catch(Exception e){


            // 忽略无法读取文件


        }finally{


            try{

                if(reader!=null){

                    reader.close();

                }

            }catch(Exception ignored){

            }


        }



    }




    private boolean isImportLine(
            String line
    ){

        String text =
                line.trim();


        return text.startsWith("import ");

    }



    private boolean isPackageLine(
            String line
    ){

        String text =
                line.trim();


        return text.startsWith("package ");

    }




    private List<String> buildContext(
            List<String> lines,
            int index
    ){

        List<String> context =
                new ArrayList<>();


        int start =
                Math.max(
                        0,
                        index-5
                );


        int end =
                Math.min(
                        lines.size(),
                        index+6
                );


        for(int i=start;i<end;i++){


            String line =
                    lines.get(i);



            if(line.trim().startsWith("import ")
                    ||
                    line.trim().startsWith("package ")){

                continue;

            }


            context.add(line);


        }


        return context;

    }











}