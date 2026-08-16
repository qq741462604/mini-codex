package com.minicodex.application.planning;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.Map;


@Slf4j
@Service
/**
 * PromptTemplateService：根据变量渲染 Prompt 模板。
 * 所属层：应用层。
 *
 * @author yy
 */
public class PromptTemplateService {



    public String render(
            String template,
            Map<String,String> values
    ){


        String result =
                template;



        for(Map.Entry<String,String> entry:
                values.entrySet()){


            String key =
                    "{{"
                            + entry.getKey()
                            + "}}";


            result =
                    result.replace(
                            key,
                            entry.getValue()==null
                                    ?
                                    ""
                                    :
                                    entry.getValue()
                    );

        }

        log.debug(
                "render prompt length={}",
                result.length()
        );
        return result;


    }



}
