package com.aimer.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;


public class DebugTool {

    @Tool(description = "一个用于测试的简单工具，返回固定字符串")
    public String debugTool() {
        return "工具执行成功！";
    }
}