package com.aimer.agent.app;

import com.aimer.agent.tools.PDFGenerationTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "张润.pdf";
        String content = "张润 https://www.baidu.com";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}