package com.aimer.agent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void testChat(){
        String chatId = UUID.randomUUID().toString();

        String message = "千问宝宝你好，我是林忆宁";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

//        message = "我想让另一半（张润）更爱我";
//        answer = loveApp.doChat(message, chatId);
//        Assertions.assertNotNull(answer);
//
//        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
//        answer = loveApp.doChat(message, chatId);
//        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport(){
        String chatId = UUID.randomUUID().toString();

        String message = "千问宝宝你好，我是林忆宁,我想让另一半（张润）更爱我，但我不知道该怎么做";
        LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag(){
        String chatId = UUID.randomUUID().toString();
        String template = "你好,{systemName}。我是{name},我的星座是{starSigns}，年龄{age}请你给我推荐一个对象";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String,Object> map = new HashMap<>();
        map.put("systemName","千问宝宝");
        map.put("name","张润");
        map.put("starSigns","水瓶");
        map.put("age",24);
        String message = promptTemplate.render(map);
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
