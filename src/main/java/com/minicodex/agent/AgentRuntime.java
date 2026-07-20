//package com.minicodex.agent;
//
//import com.minicodex.ai.AiClient;
//import com.minicodex.ai.AiMessage;
//import com.minicodex.ai.AiResponse;
//import com.minicodex.ai.ToolCall;
//import com.minicodex.runtime.ToolRegistry;
//import com.minicodex.tool.ToolResult;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class AgentRuntime {
//
//    /**
//     * 最大执行次数
//     */
//    private static final int MAX_STEP = 5;
//
//    private final AiClient aiClient;
//
//    private final ToolRegistry toolRegistry;
//
//    public AgentRuntime(
//            AiClient aiClient,
//            ToolRegistry toolRegistry
//    ) {
//        this.aiClient = aiClient;
//        this.toolRegistry = toolRegistry;
//    }
//
//    public String run(
//            AgentContext context
//    ) {
//
//        for (int i = 0; i < MAX_STEP; i++) {
//
//            context.setStep(i);
//
//            log.info("Agent Runtime Step : {}", i);
//
//            AiResponse response =
//                    aiClient.chat(
//                            context.getMessages()
//                    );
//
//            if (response.getToolCall() == null) {
//
//                log.info("Agent Runtime Finish");
//
//                return response.getContent();
//            }
//
//            executeTool(
//                    context,
//                    response.getToolCall()
//            );
//        }
//
//        return "任务执行次数超过限制";
//    }
//
//    private void executeTool(
//            AgentContext context,
//            ToolCall call
//    ) {
//
//        Tool tool =
//                toolRegistry.get(
//                        call.getToolName()
//                );
//
//        AiMessage toolMessage =
//                new AiMessage();
//
//        toolMessage.setRole("tool");
//
//        if (tool == null) {
//
//            toolMessage.setContent(
//                    "工具不存在:" + call.getToolName()
//            );
//
//            context.getMessages().add(toolMessage);
//
//            return;
//        }
//
//        ToolResult result =
//                tool.execute(
//                        call.getArguments()
//                );
//
//        if (result.isSuccess()) {
//
//            toolMessage.setContent(
//                    result.getOutput()
//            );
//
//        } else {
//
//            toolMessage.setContent(
//                    "执行失败:" + result.getOutput()
//            );
//
//        }
//
//        context.getMessages().add(toolMessage);
//    }
//
//}