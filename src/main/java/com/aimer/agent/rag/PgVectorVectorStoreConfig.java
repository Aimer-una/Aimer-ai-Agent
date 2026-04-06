package com.aimer.agent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private LoveAppPdfDocumentLoader loveAppPdfDocumentLoader;



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


        // TODO 暂时这么写
        // 检查表是否为空
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);

        if (count == null || count == 0) {
            log.info("向量库为空，正在初始化...");
            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            List<Document> documentsPdf = loveAppPdfDocumentLoader.loadPdfs();
            vectorStore.add(documents);
            vectorStore.add(documentsPdf);

        } else {
            log.info("向量库已存在 {} 条记录，跳过初始化", count);
        }

        return vectorStore;

    }
}
