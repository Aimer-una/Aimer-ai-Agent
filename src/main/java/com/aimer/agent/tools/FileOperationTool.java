package com.aimer.agent.tools;

import cn.hutool.core.io.FileUtil;
import com.aimer.agent.tools.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {
    private final String File_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "read contant from a file")
    public String readFile(@ToolParam(description = "name of the file") String filename){
        String filePath = File_DIR + "/" + filename;
        try {
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "write content to a file")
    public String writeFile(
            @ToolParam(description = "name of the file to write") String filename,
            @ToolParam(description = "content to write to the file") String content
    ){
        String filePath = File_DIR + "/" + filename;
        try {
            FileUtil.mkdir(File_DIR);
            FileUtil.writeUtf8String(content,filePath);
            return "File written successfully to: " + filePath;
        }catch (Exception e){
            return "Error writing to file: " + e.getMessage();
        }
    }

}
