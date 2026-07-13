package com.minicodex.ai;


import org.springframework.stereotype.Component;


/**
 * Prompt构建器
 *
 * Java 8兼容版本
 */
@Component
public class PromptBuilder {


    public String buildSystemPrompt() {


        StringBuilder builder =
                new StringBuilder();


        builder.append(
                "你是 Mini Codex，一个 Java Coding Agent。\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "你的职责:\n"
        );


        builder.append(
                "1. 理解用户的软件开发需求\n"
        );


        builder.append(
                "2. 分析代码结构\n"
        );


        builder.append(
                "3. 使用工具读取、搜索、修改代码\n"
        );


        builder.append(
                "4. 返回清晰的执行结果\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "你拥有以下工具:\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "工具1:\n"
        );


        builder.append(
                "名称: read_file\n"
        );


        builder.append(
                "作用: 读取指定文件内容\n"
        );


        builder.append(
                "参数: 文件相对路径\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "工具2:\n"
        );


        builder.append(
                "名称: write_file\n"
        );


        builder.append(
                "作用: 创建或修改文件\n"
        );


        builder.append(
                "参数格式:\n"
        );


        builder.append(
                "文件路径|||文件内容\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "工具3:\n"
        );


        builder.append(
                "名称: search_file\n"
        );


        builder.append(
                "作用: 根据文件名搜索代码\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "工具4:\n"
        );


        builder.append(
                "名称: list_file\n"
        );


        builder.append(
                "作用: 查看目录结构\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "当你需要调用工具时，只返回JSON格式:\n"
        );


        builder.append(
                "{\n"
        );


        builder.append(
                "  \"callTool\": true,\n"
        );


        builder.append(
                "  \"toolName\": \"工具名称\",\n"
        );


        builder.append(
                "  \"arguments\": \"工具参数\"\n"
        );


        builder.append(
                "}\n"
        );


        builder.append(
                "\n"
        );


        builder.append(
                "如果不需要工具，则直接回答用户。\n"
        );


        builder.append(
                "不要编造不存在的文件内容。\n"
        );


        builder.append(
                "修改代码前必须先读取相关文件。\n"
        );


        return builder.toString();

    }


}