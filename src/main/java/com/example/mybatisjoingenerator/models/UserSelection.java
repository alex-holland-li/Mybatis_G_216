package com.example.mybatisjoingenerator.models;

import java.util.List;

public class UserSelection {
    private String database;
    private String schema;
    private String mainTable;
    private String mainTableAlias;
    private String joinTable;
    private String joinTableAlias;
    private String joinCondition;
    private List<TableField> selectedMainFields;
    private List<TableField> selectedJoinFields;
    private String relationType;

    // 新增的字段
    private String rootObjectName;   // 根对象名
    private String joinObjectName;   // 副表对象名
    private String filePath;         // 文件保存路径
    private boolean saveJavaToFile;  // 是否保存Java对象文件


    // 构造函数
    public UserSelection(String database, String schema, String mainTable, String mainTableAlias,
                         String joinTable, String joinTableAlias, String joinCondition,
                         List<TableField> selectedMainFields, List<TableField> selectedJoinFields,
                         String relationType, String rootObjectName, String joinObjectName,
                         String filePath, boolean saveJavaToFile) {
        this.database = database;
        this.schema = schema;
        this.mainTable = mainTable;
        this.mainTableAlias = mainTableAlias;
        this.joinTable = joinTable;
        this.joinTableAlias = joinTableAlias;
        this.joinCondition = joinCondition;
        this.selectedMainFields = selectedMainFields;
        this.selectedJoinFields = selectedJoinFields;
        this.relationType = relationType;
        this.rootObjectName = rootObjectName;
        this.joinObjectName = joinObjectName;
        this.filePath = filePath;
        this.saveJavaToFile = saveJavaToFile;
    }

    // Getters and Setters for all fields
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
    }

    public String getMainTableAlias() {
        return mainTableAlias;
    }

    public void setMainTableAlias(String mainTableAlias) {
        this.mainTableAlias = mainTableAlias;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(String joinTable) {
        this.joinTable = joinTable;
    }

    public String getJoinTableAlias() {
        return joinTableAlias;
    }

    public void setJoinTableAlias(String joinTableAlias) {
        this.joinTableAlias = joinTableAlias;
    }

    public String getJoinCondition() {
        return joinCondition;
    }

    public void setJoinCondition(String joinCondition) {
        this.joinCondition = joinCondition;
    }

    public List<TableField> getSelectedMainFields() {
        return selectedMainFields;
    }

    public void setSelectedMainFields(List<TableField> selectedMainFields) {
        this.selectedMainFields = selectedMainFields;
    }

    public List<TableField> getSelectedJoinFields() {
        return selectedJoinFields;
    }

    public void setSelectedJoinFields(List<TableField> selectedJoinFields) {
        this.selectedJoinFields = selectedJoinFields;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getRootObjectName() {
        return rootObjectName;
    }

    public void setRootObjectName(String rootObjectName) {
        this.rootObjectName = rootObjectName;
    }

    public String getJoinObjectName() {
        return joinObjectName;
    }

    public void setJoinObjectName(String joinObjectName) {
        this.joinObjectName = joinObjectName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isSaveJavaToFile() {
        return saveJavaToFile;
    }

    public void setSaveJavaToFile(boolean saveJavaToFile) {
        this.saveJavaToFile = saveJavaToFile;
    }
}
