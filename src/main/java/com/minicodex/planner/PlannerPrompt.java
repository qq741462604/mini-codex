package com.minicodex.planner;


public class PlannerPrompt {



    public static String build(String task, String project) {
        return String.format(
                "你是一个代码工程Agent。\n\n" +
                        "当前项目结构:\n\n" +
                        "%s\n\n" +
                        "用户任务:\n\n" +
                        "%s\n\n" +
                        "要求:\n\n" +
                        "1. 必须基于真实项目结构生成计划\n" +
                        "2. 不允许生成不存在的文件\n" +
                        "3. 如果不知道文件位置，先使用list_files/search_code\n\n" +
                        "返回JSON:\n\n" +
                        "{\n" +
                        " \"plan\":[\n" +
                        "  {\n" +
                        "   \"tool\":\"\",\n" +
                        "   \"args\":{}\n" +
                        "  }\n" +
                        " ]\n" +
                        "}",
                project,
                task
        );
    }

}