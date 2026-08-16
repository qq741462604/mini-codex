package com.minicodex.infrastructure.tool;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.infrastructure.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import com.minicodex.application.agent.Agent;
import com.minicodex.domain.tool.FileOperationResult;
import com.minicodex.domain.tool.ToolInput;


@Slf4j
@Component
@RequiredArgsConstructor
/**
 * WriteFileTool：提供受工作区约束的具体文件操作工具。
 * 所属层：基础设施层。
 *
 * @author yy
 */
public class WriteFileTool
        extends BaseTool {


    private final WorkspaceService workspaceService;



    @Override
    public String name(){

        return "write_file";

    }



    @Override
    public String description(){

        return "create new file";

    }



    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {


        ToolInput toolInput =
                (ToolInput) input;



        File file =
                workspaceService.resolve(
                        toolInput.getPath()
                );


        boolean existedBefore =
                file.exists();


        if(existedBefore){

            throw new RuntimeException(
                    "write_file cannot modify existing file, use patch_file"
            );

        }



        if(file.getParentFile()!=null
                &&
                !file.getParentFile().exists()){


            file.getParentFile()
                    .mkdirs();

        }



        String content;



        /*
         *
         * 修改已有文件
         *
         */
        if(toolInput.getOldText()!=null
                &&
                !toolInput.getOldText().isEmpty()){


            content =
                    new String(
                            Files.readAllBytes(
                                    file.toPath()
                            ),
                            StandardCharsets.UTF_8
                    );
            if(toolInput.getNewText()==null){
                throw new RuntimeException(
                        "newText is null"
                );
            }

            String source =
                    normalize(content);


            String old =
                    normalize(toolInput.getOldText());


            if(!source.contains(old)){

                log.error(
                        "oldText not found path={} oldTextLength={}",
                        toolInput.getPath(),
                        toolInput.getOldText().length()
                );

                throw new RuntimeException(
                        "oldText not found"
                );

            }

            content =
                    replaceIgnoreFormat(
                            content,
                            toolInput.getOldText(),
                            toolInput.getNewText()
                    );





        }else{

            if(toolInput.getNewText()!=null){

                content =
                        toolInput.getNewText();

            }else{

                content =
                        toolInput.getContent();

            }

            if(content==null){

                throw new RuntimeException(
                        "write content is null"
                );

            }

        }



        Files.write(
                file.toPath(),
                content.getBytes(
                        StandardCharsets.UTF_8
                )
        );



        return FileOperationResult.builder()

                .action(
                        existedBefore
                                ?
                                "update"
                                :
                                "create"
                )

                .path(
                        workspaceService.relativePath(file)
                )

                .success(true)

                .message(
                        "write success"
                )

                .build();


    }

    private String replaceIgnoreFormat(
            String source,
            String oldText,
            String newText
    ){


        String normalizedSource =
                normalize(source);


        String normalizedOld =
                normalize(oldText);



        int index =
                normalizedSource.indexOf(
                        normalizedOld
                );


        if(index < 0){

            throw new RuntimeException(
                    "oldText not found"
            );

        }


        // 找原始文本位置
        int start =
                findOriginalIndex(
                        source,
                        oldText
                );


        if(start < 0){

            throw new RuntimeException(
                    "original oldText not found"
            );

        }


        return source.substring(0,start)
                +
                newText
                +
                source.substring(
                        start + oldText.length()
                );

    }



    private int findOriginalIndex(
            String source,
            String oldText
    ){


        StringBuilder current =
                new StringBuilder();


        for(int i=0;i<source.length();i++){


            char c =
                    source.charAt(i);


            if(c=='\r'){

                continue;

            }


            if(c=='\t'){

                current.append(" ");

            }else{

                current.append(c);

            }


            if(normalize(
                    current.toString()
            ).endsWith(
                    normalize(oldText)
            )){

                return i-oldText.length()+1;

            }

        }


        return -1;

    }

    private String normalize(String text){

        return text
                .replace("\r\n","\n")
                .replaceAll("[\\t ]+"," ")
                .trim();

    }
}
