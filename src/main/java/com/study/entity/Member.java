package com.study.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Member {
    private String id;
    private String userId;
    private Integer level;
    private Date expireTime;
}