

# Aimer AI Agent

基于 Spring AI + DashScope 构建的 AI Agent 应用，提供对话、工具调用、RAG 向量检索等能力。

## 项目简介

Aimer AI Agent 是一个智能对话应用框架，基于 Spring Boot 和 Spring AI 生态系统构建。它集成了阿里云 DashScope 的 AI 能力，支持：

- **智能对话**：基于大语言模型的自然语言交互
- **RAG 向量检索**：支持 Markdown、PDF 文档的向量化存储与检索
- **工具调用**：内置多种工具（邮件发送、PDF 生成、文件操作、网页抓取等）
- **云端 RAG**：支持阿里云 RAG 云的文档检索服务

## 技术栈

- Java 17+
- Spring Boot 3.x
- Spring AI (DashScope)
- PostgreSQL + PGVector
- Kryo (序列化)
- Vue 3 + Vite (前端)

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+ (带 pgvector 扩展)

### 配置说明

在 `src/main/resources/application.yml` 中配置必要的参数：

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key
  datasource:
    url: jdbc:postgresql://localhost:5432/aimer
    username: postgres
    password: your-password
  mail:
    username: your-email@example.com
```

### 构建运行

```bash
# 后端构建
./mvnw clean install
./mvnw spring-boot:run

# 前端构建
cd yu-ai-agent-frontend
npm install
npm run dev
```

## 核心功能

### LoveApp 对话应用

`LoveApp` 是核心对话服务类，提供多种对话模式：

| 方法 | 描述 |
|------|------|
| `doChat` | 基础对话 |
| `doChatWithRag` | 带 RAG 检索的对话 |
| `doChatWithReport` | 返回结构化报告 |
| `doChatWithTools` | 带工具调用的对话 |
| `doChatWithMcp` | MCP 协议对话 |
| `doChatByStream` | 流式对话 |

### Agent 框架

项目提供完整的 Agent 框架，支持：

- **BaseAgent**：基础 Agent 抽象类
- **ReActAgent**：ReAct 模式 Agent
- **ToolCallAgent**：工具调用 Agent
- **AimerManus**：核心智能体实现

### RAG 向量检索

项目支持三种 RAG 模式：

1. **本地 RAG**：使用文档加载器加载本地 Markdown/PDF 文件
2. **PGVector 向量库**：使用 PostgreSQL + pgvector 存储向量
3. **云端 RAG**：使用阿里云 RAG 云服务

### 工具系统

| 工具 | 功能 |
|------|------|
| EmailTool | 发送带 PDF 附件的邮件 |
| PDFGenerationTool | 生成 PDF 文件 |
| FileOperationTool | 文件读写操作 |
| WebScrapingTool | 网页内容抓取 |
| WebSearchTool | 百度搜索 |
| TerminalOperationTool | 终端命令执行 |
| ResourceDownloadTool | 资源下载 |
| DebugTool | 调试工具 |

### Advisor 增强

- **MyLoggerAdvisor**：请求响应日志记录
- **ReReadingAdvisor**：增强理解的重读策略

### 聊天记忆

- **FileBasedChatMemory**：基于 Kryo 序列化的文件存储聊天记忆

## 目录结构

```
src/main/java/com/aimer/agent/
├── AimerAiAgentApplication.java    # 应用入口
├── advisor/                      # Advisor 增强
│   ├── MyLoggerAdvisor.java
│   └── ReReadingAdvisor.java
├── ai/                          # AI 调用
├── app/                         # 核心应用
│   └── LoveApp.java             # 对话服务
├── chatmemory/                 # 聊天记忆
├── config/                     # 配置
├── controller/                 # 控制器
│   ├── AiController.java
│   └── HealthController.java
├── coreagent/                  # Agent 核心
│   ├── AimerManus.java
│   ├── BaseAgent.java
│   ├── ReActAgent.java
│   └── ToolCallAgent.java
├── rag/                        # RAG 组件
│   ├── LoveAppDocumentLoader.java
│   ├── LoveAppPdfDocumentLoader.java
│   ├── LoveAppRagCloudAdvisorConfig.java
│   └── LoveAppVectorStoreConfig.java
└── tools/                      # 工具集
    ├── EmailTool.java
    ├── PDFGenerationTool.java
    ├── FileOperationTool.java
    ├── WebScrapingTool.java
    ├── WebSearchTool.java
    └── TerminalOperationTool.java

yu-ai-agent-frontend/          # Vue 3 前端
├── src/
│   ├── views/                 # 页面视图
│   ├── components/            # 组件
│   └── api/                   # API 接口

aimer-image-mcp/              # 图片搜索 MCP 模块
```

## API 接口

### 对话接口

```
GET /ai/love_app/chat/sync     - 同步对话
GET /ai/love_app/chat/sse      - SSE 流式对话
GET /ai/love_app/chat/sse/emitter - SSE Emitter 对话
GET /ai/manus/chat             - Manus 智能体对话
```

### 健康检查

```
GET /health                   - 健康状态检查
```

## 测试

```bash
./mvnw test
```

## 许可证

MIT License