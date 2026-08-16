package com.minicodex.application.service;

import com.minicodex.domain.tool.ToolInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.minicodex.domain.trace.AgentTrace;
import com.minicodex.domain.trace.TraceStep;

@Slf4j
@Service
/**
 * TraceService：负责面向用例的代码变更、结果或跟踪服务。
 * 所属层：应用层。
 *
 * @author yy
 */
public class TraceService {

    public AgentTrace start(String task) {
        AgentTrace trace = new AgentTrace();
        trace.setTask(task);
        trace.setStartTime(System.currentTimeMillis());
        return trace;
    }

    public void finish(AgentTrace trace) {
        trace.setEndTime(System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            print(trace);
        }
    }

    public void print(AgentTrace trace) {
        log.debug("TRACE STEP SIZE={}", trace.getSteps().size());
        log.debug("\n========== MINI CODEX TRACE ==========");
        log.debug("TASK:\n{}", trace.getTask());
        for (TraceStep step : trace.getSteps()) {
            log.debug(
                    "\nTYPE:{}\nNAME:{}\nINPUT:{}\nOUTPUT:{}\nCOST:{}ms",
                    step.getType(),
                    step.getName(),
                    summarize(step.getInput()),
                    summarize(step.getOutput()),
                    step.getCost()
            );
        }
        log.debug("TOTAL:{}ms", trace.getEndTime() - trace.getStartTime());
        log.debug("======================================");
    }

    private String summarize(Object value) {
        if (value == null) {
            return null;
        }
        String text = summarizeValue(value);
        if (text.length() <= 500) {
            return text;
        }
        return text.substring(0, 500) + "...(truncated, length=" + text.length() + ")";
    }

    private String summarizeValue(Object value) {
        if (!(value instanceof ToolInput)) {
            return String.valueOf(value);
        }
        ToolInput input = (ToolInput) value;
        return "ToolInput("
                + "path=" + input.getPath()
                + ", keyword=" + input.getKeyword()
                + ", limit=" + input.getLimit()
                + ", startLine=" + input.getStartLine()
                + ", endLine=" + input.getEndLine()
                + ", contentLength=" + length(input.getContent())
                + ", oldTextLength=" + length(input.getOldText())
                + ", newTextLength=" + length(input.getNewText())
                + ")";
    }

    private int length(String text) {
        return text == null ? 0 : text.length();
    }
}
