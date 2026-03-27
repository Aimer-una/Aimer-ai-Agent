package com.aimer.agent.ai;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// TODO 这个类会在启动的时候自动触发run方法所以把Component注释掉了，使其不被启动类扫描到
// @Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;
    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("千问宝宝晚上好呀(●'◡'●)")).getResult().getOutput();
        System.out.println(output.getText());
    }
}
