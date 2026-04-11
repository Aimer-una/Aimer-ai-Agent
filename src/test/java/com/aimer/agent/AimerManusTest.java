package com.aimer.agent;

import com.aimer.agent.coreagent.AimerManus;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AimerManusTest {

    @Resource
    private AimerManus aimerManus;

    @Test
    void run() {
        String userPrompt = """  
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点，  
                制定一份详细的约会计划，  
                并以 PDF 格式输出
                并发送到2103095120@qq.com这个邮箱""";
        String answer = aimerManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
