package com.example.mybatisjoingenerator.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 辅助类，用于保存生成的文件
 */
public class CodeGeneratorHelper {

    /**
     * 保存文件
     *
     * @param savePath    保存路径的目录
     * @param fileName    文件名
     * @param fileContent 文件内容
     */
    public void saveFile(String savePath, String fileName, String fileContent) {
        try {
            File directory = new File(savePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fileContent);
            }
            System.out.println("成功保存文件: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("保存文件时发生错误: " + e.getMessage());
        }
    }
}
