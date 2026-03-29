package com.aimer.agent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
/*
 * 你上传的文档（PDF、Word、Markdown 等）已经在阿里云端被：
 * 切分 → Embedding → 向量化 → 建索引；
 * 所以你本地不需要再加载文档、不需要 EmbeddingModel、不需要 VectorStore！
 */
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    @Bean
    public Advisor loveAppRagCloudAdvisor(){
        // 这是阿里云 Spring AI Starter 提供的客户端；
        // 封装了对 DashScope RAG API 的 HTTP 调用（比如 /v1/rag/retrieve）。
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
            // 指定云端知识索引名称
            final String KNOWLEDGE_INDEX = "恋爱大师";

            // DashScopeDocumentRetriever 是 Spring AI 的 DocumentRetriever 接口的实现；
            // 它的作用：当用户提问时，自动调用阿里云 API，传入问题，在 "恋爱大师" 索引中检索最相关的 N 个片段。
            DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                    DashScopeDocumentRetrieverOptions.builder()
                            .withIndexName(KNOWLEDGE_INDEX)
                            .build());

        /*
         * RetrievalAugmentationAdvisor 是 Spring AI 提供的 “检索增强顾问”；
         * 它会在大模型生成答案前：
         * 自动调用 documentRetriever.retrieve(query)；
         * 把检索到的相关文档片段拼接到 prompt 中；
         * 再把增强后的 prompt 发给大模型。
         */
        return RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)
                    .build();
    }
}
