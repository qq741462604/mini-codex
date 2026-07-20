package com.minicodex.memory;


import lombok.Builder;
import lombok.Data;


import java.util.Date;



@Data
@Builder
public class Memory {



    private String key;



    private String content;



    private Date createTime;


}