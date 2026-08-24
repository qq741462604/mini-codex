package com.minicodex.application.port;

import java.io.File;

/**
 * 定义应用层访问目标工作区的路径边界。 所属层：应用层。
 *
 * @author yy
 */
public interface WorkspacePort {

  File getRoot();

  File resolve(String path);

  String relativePath(File file);
}
