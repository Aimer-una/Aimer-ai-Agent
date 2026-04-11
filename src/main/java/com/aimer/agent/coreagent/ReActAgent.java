package com.aimer.agent.coreagent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct){
                return "思考完成 - 无需行动";
            }
            return act();
        }catch (Exception e){
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }

    public abstract boolean think();

    public abstract String act();
}
