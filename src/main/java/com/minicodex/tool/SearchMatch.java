package com.minicodex.tool;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchMatch {


    private String path;


    private Integer lineNumber;


    private String content;


    private List<String> context;
    private String relativePath;

}