package com.minicodex.tool;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileChangeResult {

    private String action;

    private String path;

    private boolean success;

}
