package com.minicodex.context;


import org.springframework.stereotype.Component;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



@Component
public class ContextManager {



    /**
     * 最大token预算
     */
    private static final int MAX_TOKEN =
            12000;




    public List<ContextItem> compress(
            List<ContextItem> items
    ){


        items.sort(
                Comparator.comparingInt(
                                ContextItem::getPriority
                        )
                        .reversed()
        );



        List<ContextItem> result =
                new ArrayList<>();


        int total = 0;



        for(ContextItem item:items){


            if(total+
                    item.getTokens()
                    >
                    MAX_TOKEN){

                break;

            }


            result.add(item);


            total +=
                    item.getTokens();


        }


        return result;


    }


}