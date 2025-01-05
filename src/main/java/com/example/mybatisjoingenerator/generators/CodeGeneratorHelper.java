package com.example.mybatisjoingenerator.generators;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CodeGeneratorHelper 提供文件保存的辅助方法
 */
public class CodeGeneratorHelper {

    /**
     * 保存 Java 文件到指定路径
     *
     * @param savePath      保存路径（目录）
     * @param fileName      文件名（含扩展名）
     * @param javaClassCode Java 类代码内容
     */
    public void saveJavaFile(String savePath, String fileName, String javaClassCode) {
        Path filePath = Path.of(savePath, fileName);
        try {
            // 确保目录存在
            Files.createDirectories(filePath.getParent());

            // 写入文件
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(javaClassCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 你可以使用日志系统记录错误
        }
    }
}
