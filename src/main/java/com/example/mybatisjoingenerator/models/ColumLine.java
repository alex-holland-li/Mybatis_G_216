package com.example.mybatisjoingenerator.models;

import com.example.mybatisjoingenerator.generators.CodeGenerator;
import com.example.mybatisjoingenerator.ui.JoinQueryPanel;

/**
 * @author liyun
 * @program Mybatis_G_216
 * @create 2025/1/6
 **/
public class ColumLine {
    private String columName;
    private String columType;
    private String columComment;

    public ColumLine(String columName, String columType, String columComment) {
        this.columName = columName;
        this.columType = columType;
        this.columComment = columComment;
    }

    public static String formatComment(String input) {
        if (input == null || input.isEmpty()) {
            return "";  // 如果字符串为空或 null，返回空字符串
        }

        if (input.length() > 100) {
            return "  " + input.substring(0, 100) + "...";  // 如果字符串长度超过 100，截取前 100 个字符并添加省略号

        }

        return "  " + input;  // 否则返回原始字符串
    }

    public String getColumName() {
        return columName;
    }

    public void setColumName(String columName) {
        this.columName = columName;
    }

    public String getColumType() {
        return columType;
    }

    public void setColumType(String columType) {
        this.columType = columType;
    }

    public String getColumComment() {
        return columComment;
    }

    public void setColumComment(String columComment) {
        this.columComment = columComment;
    }

    @Override
    public String toString() {
        return columName + formatComment(columComment);
    }

    public TableField toTableField() {
        return new TableField(columName, CodeGenerator.toJavaFileName(columName), JoinQueryPanel.mapDataTypeToJavaType(columType), columComment);
    }
}
