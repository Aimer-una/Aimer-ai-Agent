package com.aimer.agent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Resource
    private VectorStore pgVectorVectorStore;

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
    void test(){
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2"))
        );
        pgVectorVectorStore.add(documents);
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        Assertions.assertNotNull(results);
    }

    @Test
    void doChatWithReportCustom(){

    }

    @Test
    void doChatWithReport(){
        String chatId = UUID.randomUUID().toString();

        String message = "千问宝宝你好，我正在恋爱，我想维护好这段关系，但我不知道该怎么做";
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

    @Test
    void doChatWithRagCustomTest(){
        String chatId = UUID.randomUUID().toString();

        String message = "你好，我目前单身，我想交一个女朋友，但我不知道该怎么做";
        String s = loveApp.doChatWithRagCustom(message, chatId);
        Assertions.assertNotNull(s);
    }

    @Test
    void doChatWithTools() {

        //testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

         testMessage("请帮我生成一个很简短不超过50字恋爱报告的pdf生成后请发到2103095120@qq.com这个邮箱里");

        // testMessage("执行 Python3 脚本来生成数据分析报告");

        //testMessage("保存我的恋爱档案为文件");

        // testMessage("请调用测试工具");
    }

    private void testMessage(String message) {
        // 打印 AssistantMessage 类来自哪个 jar
        System.out.println("AssistantMessage class: " +
                AssistantMessage.class.getProtectionDomain().getCodeSource().getLocation());
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();

        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点 回复3个地点即可";
        String answer =  loveApp.doChatWithMcp(message, chatId);
    }

    @Test
    void testNpx() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("npx", "-v").start();
        String version = new String(p.getInputStream().readAllBytes()).trim();
        System.out.println("✅ npx version: " + version);
        assert version.equals("11.3.0"); // 你之前查到的版本
    }
}
