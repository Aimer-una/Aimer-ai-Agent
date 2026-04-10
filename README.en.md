# Aimer AI Agent

An AI Agent application built on Spring AI + DashScope, providing capabilities such as conversational interaction, tool invocation, and RAG vector retrieval.

## Project Overview

Aimer AI Agent is an intelligent conversational application framework built on Spring Boot and the Spring AI ecosystem. It integrates Alibaba Cloud DashScope's AI capabilities and supports:

- **Intelligent Conversation**: Natural language interaction powered by large language models
- **RAG Vector Retrieval**: Vectorized storage and retrieval of Markdown and PDF documents
- **Tool Invocation**: Built-in tools (email sending, PDF generation, file operations, web scraping, etc.)
- **Cloud RAG**: Support for Alibaba Cloud RAG cloud document retrieval services

## Technology Stack

- Java 17+
- Spring Boot 3.x
- Spring AI (DashScope)
- PostgreSQL + PGVector
- Kryo (Serialization)

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+ (with pgvector extension)

### Configuration

Configure required parameters in `application.yml`:

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

### Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Core Features

### LoveApp Conversational Service

`LoveApp` is the core conversational service class, offering multiple conversation modes:

| Method | Description |
|--------|-------------|
| `doChat` | Basic conversation |
| `doChatWithRag` | Conversation with RAG retrieval |
| `doChatWithReport` | Returns structured reports |
| `doChatWithTools` | Conversation with tool invocation |
| `doChatWithMcp` | MCP protocol-based conversation |

### RAG Vector Retrieval

The project supports three RAG modes:

1. **Local RAG**: Load local Markdown/PDF files using document loaders
2. **PGVector Vector Store**: Store vectors using PostgreSQL + pgvector
3. **Cloud RAG**: Utilize Alibaba Cloud RAG cloud service

### Tool System

| Tool | Function |
|------|----------|
| EmailTool | Send emails with PDF attachments |
| PDFGenerationTool | Generate PDF files |
| FileOperationTool | File read/write operations |
| WebScrapingTool | Web content scraping |
| WebSearchTool | Baidu search |
| TerminalOperationTool | Execute terminal commands |
| ResourceDownloadTool | Download resources |

### Advisor Enhancements

- **MyLoggerAdvisor**: Logs request and response details
- **ReReadingAdvisor**: Enhances understanding through re-reading strategies

## Directory Structure

```
src/main/java/com/aimer/agent/
├── AimerAiAgentApplication.java    # Application entry point
├── advisor/                       # Advisor enhancements
├── ai/                           # AI invocation
├── app/                          # Core application
├── chatmemory/                  # Chat memory
├── controller/                   # Controllers
├── rag/                          # RAG components
└── tools/                        # Toolset
```

## Health Check

After service startup, access:

```
GET http://localhost:8080/health
```

## Testing

```bash
./mvnw test
```

## License

MIT License