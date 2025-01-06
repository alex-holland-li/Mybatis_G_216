package com.example.mybatisjoingenerator.models;

/**
 * @author 李运
 */
public class TableField {
    private final String columnName;
    private String javaFieldName;
    private final String javaType;

    private final String comment;

    public TableField(String columnName, String javaFieldName, String javaType, String comment) {
        this.columnName = columnName;
        this.javaFieldName = javaFieldName;
        this.javaType = javaType;
        this.comment = comment;
    }

    // Getters and Setters


    public String getColumnName() {
        return columnName;
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getComment() {
        return comment;
    }


    public void setJavaFieldName(String aValue) {
        this.javaFieldName = aValue;
    }

    @Override
    public String toString() {
        return "TableField{" +
                "columnName='" + columnName + '\'' +
                ", javaFieldName='" + javaFieldName + '\'' +
                ", javaType='" + javaType + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
