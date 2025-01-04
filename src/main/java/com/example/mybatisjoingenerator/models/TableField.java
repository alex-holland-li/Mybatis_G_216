package com.example.mybatisjoingenerator.models;

public class TableField {
    private String tableName;
    private String tableAlias;
    private String columnName;
    private String javaFieldName;
    private String javaType;
    private boolean isPrimaryKey;

    public TableField(String tableName, String tableAlias, String columnName, String javaFieldName, String javaType, boolean isPrimaryKey) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.columnName = columnName;
        this.javaFieldName = javaFieldName;
        this.javaType = javaType;
        this.isPrimaryKey = isPrimaryKey;
    }

    // Getters and Setters

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public void setJavaFieldName(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String getJavaType() {
        return javaType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
}
