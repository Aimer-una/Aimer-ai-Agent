package com.aimer.agent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// 排除自动配置
/**
 * 你引入了 spring-ai-pgvector-spring-boot-starter；
 * 它自带了一个 PgVectorStoreAutoConfiguration 类；
 * 这个类也会尝试创建一个 PgVectorStore Bean；
 * 如果你不排除，就会出现 两个相同的 Bean → 报错！
 */
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class AimerAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimerAiAgentApplication.class, args);
    }

}
