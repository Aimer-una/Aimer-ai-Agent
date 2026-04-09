package com.aimer.agent.app;

import com.aimer.agent.advisor.MyLoggerAdvisor;
import com.aimer.agent.chatmemory.FileBasedChatMemory;
import com.aimer.agent.rag.LoveAppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * Java 14 引入的预览特性，并在 Java 16 正式发布，它是为了简化“纯数据载体”类的编写而设计的
 * record 是一种特殊的「不可变数据类」，
 * 编译器会自动生成构造方法、字段、equals()、hashCode()、getter() 和 toString()，
 * 让你专注数据本身，而不是样板代码。
 * @param title
 * @param suggestions
 * @author 张润
 */
record LoveReport(String title, List<String> suggestions){

}

@Component
@Slf4j
public class LoveApp {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
    你是一位专业的恋爱心理顾问。
    用户会直接告诉你他们的状态（如“我正在恋爱”）和具体困扰。
    请根据用户提到的状态，从专业角度给出温暖、实用的建议。
    不要反问用户问题，直接提供解决方案。
""";


    public LoveApp(ChatModel dashscopeChatModel) {
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                // 指定默认的系统 Prompt
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 指定基于内存‌的对话记忆 Advisor
                        new MessageChatMemoryAdvisor(chatMemory)
                        // new MyLoggerAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                // CHAT_MEMORY_CONVERSATION_ID_KEY:指定对话 id
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        // CHAT_MEMORY_RETRIEVE_SIZE_KEY:指定对话记忆大小
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content= response.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    public LoveReport doChatWithReport(String message, String chatId){
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    public String doChatWithRag(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(sepc -> sepc.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
       //  log.info("content:{}",content);
        return content;
    }

    public String doChatWithRagCustom(String message, String chatId){
        // ✅ 动态判断状态
        String userStatus = extractRelationshipStatus(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(sepc -> sepc.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                // .advisors(new MyLoggerAdvisor())
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(pgVectorVectorStore,userStatus)
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    // 在 LoveApp 类里新增
    private String extractRelationshipStatus(String userMessage) {
        String msg = userMessage.toLowerCase();
        if (msg.contains("恋爱") || msg.contains("男女朋友") || msg.contains("对象")) {
            return "dating";   // 对应 dating/ 目录
        } else if (msg.contains("已婚") || msg.contains("结婚") || msg.contains("夫妻")) {
            return "married";  // 对应 married/ 目录
        } else {
            return "single";   // 默认或包含“单身”
        }
    }

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))

                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))

                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
