package com.aimer.agent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
/**
 * LoveAppPdfDocumentLoader 是一个 Spring 组件，它自动扫描 classpath:document/*.pdf 目录下的所有PDF文件
 * 解析成结构化的 Document 列表，供后续向量数据库做 Embedding 和检索使用。
 */
public class LoveAppPdfDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppPdfDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadPdfs() {
        /*
         * Document中的内容:
         * content：文本内容
         * metadata：元数据（如文件名、来源、页码等）
         */
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 加载所有 PDF 文件（递归扫描子目录）
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/**/*.pdf");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                String classPathLocation = resource.getURL().getPath();

                // 找到 "/document/" 的位置
                int docIndex = classPathLocation.indexOf("/document/");
                if (docIndex == -1) {
                    log.warn("无法识别文档路径格式: {}", classPathLocation);
                    continue;
                }
                // 提取 "/document/{status}/..." 中的 {status}
                String remaining = classPathLocation.substring(docIndex + "/document/".length());
                String status;
                if (remaining.contains("/")) {
                    status = remaining.split("/")[0]; // 如 "dating"
                } else {
                    status = "general"; // 根目录文件
                }

                // PDF 解析配置
                PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                        .withPagesPerDocument(0) // 读取整个PDF为1个Document
                        .build();

                log.info("加载PDF文档: {} -> status: {}", fileName, status);

                // 创建PDF读取器
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource, config);

                // 获取所有页面文档
                List<Document> rawDocs  = reader.get();

                // ✅ 关键步骤：批量注入元数据（避免后续循环！）
                List<Document> enrichedDocs = rawDocs.stream()
                        .map(doc -> new Document(
                                doc.getText(),
                                Map.of(
                                        "filename", fileName,
                                        "status", status,
                                        "source_type", "pdf"
                                )
                        ))
                        .collect(Collectors.toList());

                // ✅ 直接分块（元数据自动继承到每个chunk！）
                TokenTextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocs = textSplitter.apply(enrichedDocs);
                allDocuments.addAll(splitDocs);
            }
        } catch (IOException e) {
            log.error("PDF 文档加载失败", e);
        }
        return allDocuments;
    }
}