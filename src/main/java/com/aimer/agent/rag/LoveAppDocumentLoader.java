package com.aimer.agent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
/**
 * LoveAppDocumentLoader 是一个 Spring 组件，它自动扫描 classpath:document/*.md目录下的所有Markdown文件
 * 解析成结构化的 Document 列表，供后续向量数据库做 Embedding 和检索使用。
 */
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver){
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns(){
        /*
         *  Document中的内容:
         *  content：文本内容
         *  metadata：元数据（如文件名、来源等）
         */
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 加载所有 Markdown 文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true) // 遇到 --- 分割线就切分成新文档
                        .withIncludeCodeBlock(false) // 忽略代码块（比如 ``` ... ```）
                        .withIncludeBlockquote(false) // 忽略引用块（比如 > ...）
                        .withAdditionalMetadata("filename", fileName) // 把文件名存进 metadata
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        }catch (IOException e){
            log.error("Markdown 文档加载失败",e);
        }
        return allDocuments;
    }
}
