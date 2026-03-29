package com.aimer.agent.app;

import com.aimer.agent.advisor.MyLoggerAdvisor;
import com.aimer.agent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
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
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
                你是一位专业的恋爱咨询师。
                用户提供了自己的基本信息
                请为用户选择合适的对象，并说明理由：
    
              
                【要求】
                - 重点考虑年龄、星座、兴趣匹配度
                - 给出具体原因，不要泛泛而谈
                - 语气温暖，像朋友一样聊天
                - 如果有多位候选人符合条件，请任选其中一位，并明确说出她的名字；
                - 不要因为不确定就不提名字；
                - 名字必须来自文档中的“# 恋爱对象候选人 - XXX”部分
                """;

/*            "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
                    "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
                    "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
                    "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。" +
                    "回答问题简洁明了";*/

    public LoveApp(ChatModel dashscopeChatModel) {
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                // 指定默认的系统 Prompt
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 指定基于内存‌的对话记忆 Advisor
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
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
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
       //  log.info("content:{}",content);
        return content;
    }


}
