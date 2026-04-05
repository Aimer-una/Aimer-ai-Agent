// src/main/java/com/aimer/agent/tools/EmailTool.java

package com.aimer.agent.tools;

import com.aimer.agent.tools.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.File;

@Component
@Slf4j
public class EmailTool {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailTool(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    // 在方法开头加路径白名单校验
    private static final String ALLOWED_PDF_DIR = FileConstant.FILE_SAVE_DIR + "/pdf";

    @Tool(description = "Send an email with a PDF file as attachment. The PDF file must already exist on the server.")
    public String sendEmailWithAttachment(
            @ToolParam(description = "Recipient's email address") String to,
            @ToolParam(description = "Subject of the email") String subject,
            @ToolParam(description = "Body text of the email (plain text)") String body,
            @ToolParam(description = "pdf file path") String pdfFilePath){


        log.info("📧 Trying to send email, got pdf path: {}",pdfFilePath);
        if (pdfFilePath == null || pdfFilePath.isEmpty()) {
            return "Error: No PDF file has been generated yet. Please generate a PDF first.";
        }

        // 在 sendEmailWithAttachment 方法开头
        if (!pdfFilePath.startsWith(ALLOWED_PDF_DIR)) {
            return "Error: Only PDFs in " + ALLOWED_PDF_DIR + " are allowed.";
        }
        try {
            // 1. 校验邮箱
            if (!to.contains("@") || !to.contains(".")) {
                return "Error: Invalid email address.";
            }

            // 2. 检查 PDF 文件是否存在
            File pdfFile = new File(pdfFilePath);
            if (!pdfFile.exists() || !pdfFile.getName().toLowerCase().endsWith(".pdf")) {
                return "Error: PDF file not found or invalid: " + pdfFilePath;
            }

            // 3. 创建 MimeMessage（支持附件）
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true = support multipart

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body); // 纯文本正文

            // 4. 添加 PDF 附件
            FileSystemResource pdfResource = new FileSystemResource(pdfFile);
            helper.addAttachment(pdfFile.getName(), pdfResource); // 自动用文件名作为附件名

            // 5. 发送
            mailSender.send(mimeMessage);
            return "Email with PDF attachment sent successfully to: " + to;

        } catch (MessagingException e) {
            return "Failed to create email message: " + e.getMessage();
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}