package com.aimer.agent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class LoveAppRagCustomAdvisorFactory {
    /**
     * 根据用户当前的恋爱状态（比如“单身”、“恋爱中”），只从向量数据库里检索和这个状态相关的建议文档，然后交给大模型生成回答。
     * 这样就不会出现：给一个已婚人士推荐“如何搭讪异性”的尴尬情况啦
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore,String status){
        // 构建过滤条件（核心！）
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status",status) // “请只查找那些元信息（metadata）中 status 字段等于当前传入值的文档。”
                .build();
        // 创建文档检索器（DocumentRetriever）
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore) // 去哪个数据库找？→ 你传进来的那个
                .filterExpression(expression) // 只找符合 status 条件的文档
                .similarityThreshold(0.5) // 相似度低于 0.5 的不要（避免不相关的内容）
                .topK(3) // 最多返回 3 条最相关的文档
                .build();
        /*
         * RetrievalAugmentationAdvisor 是 Spring AI 提供的一个标准组件
         * 它的作用是：在用户提问时，自动调用 documentRetriever 去查文档，然后把查到的内容加进 Prompt，再交给大模型回答
         */
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}
