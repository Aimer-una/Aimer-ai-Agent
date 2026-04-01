package com.aimer.agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel){
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate,dashscopeEmbeddingModel)
                .dimensions(1536) // 向量维度（通义千问 embedding 是 1536）
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE) // 距离计算方式：余弦相似度
                .indexType(PgVectorStore.PgIndexType.HNSW) // 索引类型：HNSW（高效近邻搜索）
                .initializeSchema(true) // 创建表结构（如果不存在）
                .schemaName("public") // 数据库 schema 名称
                .vectorTableName("vector_store") // 向量表名
                .maxDocumentBatchSize(10000) // 批量插入最大数量
                .build();
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        vectorStore.add(documents);
        return vectorStore;

    }
}
