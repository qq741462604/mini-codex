package com.minicodex.guard;


import lombok.Builder;
import lombok.Data;



import java.util.Date;



@Data
@Builder
public class ChangeSnapshot {



    /**
     * 修改任务
     */
    private String task;



    /**
     * git hash
     */
    private String commitId;



    /**
     * 时间
     */
    private Date time;


}