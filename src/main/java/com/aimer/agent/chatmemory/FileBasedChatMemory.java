package com.aimer.agent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 为什么不用 JSON？因为 Message 包含复杂对象（如 GenerationMetadata），JSON 反序列化容易失败，而 Kryo 更鲁棒。

public class FileBasedChatMemory implements ChatMemory {
    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        // 不用提前注册类，方便序列化任意 Message 子类
        kryo.setRegistrationRequired(false);
        // 即使类没有无参构造函数，也能反序列化（Spring AI 的 Message 类可能没有 public 无参构造
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }


    // 初始化存储目录
    public FileBasedChatMemory(String dir){
        /*
         * 比如传 "./chat_memory"，就会在项目根目录建一个 chat_memory/ 文件夹；
         * 每个对话存成一个文件：{conversationId}.kryo。
         */
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()){
            baseDir.mkdir();
        }
    }
    @Override
    public void add(String conversationId, List<Message> messages) {
        /*
         * 先读取已有对话（没有就新建空列表）；
         * 把新消息追加进去；
         * 整个列表重新写回文件（简单粗暴但有效）。
         * 注意：这里是全量覆盖写入，不是增量追加。对小对话没问题，大对话可能有性能损耗。
         */
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId,conversationMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        /*
         * 读取全部历史消息；
         * 用 skip(...) 取最后 lastN 条（避免 token 超限）；
         * 比如 lastN=5，就只返回最近 5 条消息给大模型。
         * 这是 prompt 工程的最佳实践：只传最近几轮对话，节省上下文长度。
         */
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size()- lastN))
                .toList();
    }

    @Override
    // 清除对话 = 删除对应 .kryo 文件；
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()){
            file.delete();
        }
    }

    private List<Message> getOrCreateConversation(String conversationId){
        /*
         * 如果文件存在 → 用 Kryo 反序列化出 List<Message>；
         * 如果不存在 → 返回空 ArrayList。
         */
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()){
            try(Input input = new Input(new FileInputStream(file))){
                messages = kryo.readObject(input,ArrayList.class);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return messages;
    }

    // 用 Kryo 把整个 List<Message> 写入文件。
    private void saveConversation(String conversationId,List<Message> messages){
        File file = getConversationFile(conversationId);
        try(Output output = new Output(new FileOutputStream(file))){
            kryo.writeObject(output,messages);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // 返回文件路径：BASE_DIR/{conversationId}.kryo
    private File getConversationFile(String conversationId){
        return new File(BASE_DIR,conversationId+ ".kryo");
    }
}
