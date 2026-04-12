package com.aimer.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 指定哪些接口要开跨域
                .allowCredentials(true) // 允许前端携带 Cookie/认证信息
                .allowedOriginPatterns("*") // 允许所有域名访问
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*") // 允许前端发送任意请求头（比如 Authorization）
                .exposedHeaders("*"); // 允许前端读取响应头中的任意字段
    }
}
