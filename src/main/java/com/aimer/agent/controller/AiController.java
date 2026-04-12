package com.aimer.agent.controller;

import com.aimer.agent.app.LoveApp;
import com.aimer.agent.coreagent.AimerManus;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    // 同步调用接口
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId){
        return loveApp.doChat(message,chatId);
    }

    // SSE流式接口(返回Flux 响应式‌对象，并且添加 SSE 对应的 MediaType：)
/*    produces = MediaType.TEXT_EVENT_STREAM_VALUE + Flux<String> = 让浏览器通过 SSE 协议实时接收后端推送的消息流
     @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE) 作用：告诉浏览器“我要用 SSE 协议发数据”
     MediaType.TEXT_EVENT_STREAM_VALUE = "text/event-stream"*/
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message,String chatId){
        // 👇 关键：设置响应头 charset=utf-8
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getResponse();
        if (response != null) {
            response.setContentType("text/event-stream; charset=utf-8");
        }
/*        返回一个“消息流”，后端可以分多次发送数据
        Flux<T> 是 Project Reactor 的核心类型 → 表示 0~N 个异步数据项的流*/
        return loveApp.doChatByStream(message, chatId);
    }
    // SSE流式接口(返回 Flux 对象，并且‌设置泛型为 ServerSentEvent。使用这种方式可以‌省略 MediaType)
/*    @GetMapping(value = "/love_app/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }*/

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId){
        SseEmitter emitter = new SseEmitter(180000L);

        // 👇 关键：设置响应头 charset=utf-8
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getResponse();
        if (response != null) {
            response.setContentType("text/event-stream; charset=utf-8");
        }

        loveApp.doChatByStream(message,chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }


    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AimerManus aimerManus = new AimerManus(allTools, dashscopeChatModel);
        return aimerManus.runStream(message);
    }
}
