package com.aimer.agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    // 它是内存存储，重启会丢数据（生产可用 Redis、PGVector等）
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        // 用传入的 dashscopeEmbeddingModel 把文本转成向量
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        // 加载文档并添加到向量库
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        /*
         * simpleVectorStore.add(documents) → 关键一步！
         * 对每个 Document.content 调用 embeddingModel.embed()；
         * 把 <向量, 文本, metadata> 三元组存入内存索引；
         * 后续检索时，就能根据“语义相似度”快速找到最相关段落！
         */
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
