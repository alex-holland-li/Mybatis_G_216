package com.example.mybatisjoingenerator.models;

import java.util.List;

public class UserSelection {
    private String selectedDatabase;
    private String selectedSchema;
    private String rootObjectName;
    private String mainTable;
    private String mainTableAlias;
    private String joinTable;
    private String joinTableAlias;
    private String joinCondition;
    private List<TableField> selectedMainFields;
    private List<TableField> selectedJoinFields;
    private String relationType;
    private String joinObjectName;
    private String filePath;
    private boolean saveJavaToFile;
    private GenerationType generationType;
    // 新增字段
    private boolean createSwagger;
    private boolean createValidator;
    private boolean createLombok;
    // 构造函数
    public UserSelection(String selectedDatabase, String selectedSchema, String mainTable, String mainTableAlias,
                         String joinTable, String joinTableAlias, String joinCondition,
                         List<TableField> selectedMainFields, List<TableField> selectedJoinFields,
                         String relationType, String rootObjectName, String joinObjectName,
                         String filePath, boolean saveJavaToFile, GenerationType generationType,
                         boolean createSwagger, boolean createValidator, boolean createLombok) {
        this.selectedDatabase = selectedDatabase;
        this.selectedSchema = selectedSchema;
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
        this.generationType = generationType;
        this.createSwagger = createSwagger;
        this.createValidator = createValidator;
        this.createLombok = createLombok;
    }

    // Getter 和 Setter 方法
    public String getSelectedDatabase() {
        return selectedDatabase;
    }

    public void setSelectedDatabase(String selectedDatabase) {
        this.selectedDatabase = selectedDatabase;
    }

    public String getSelectedSchema() {
        return selectedSchema;
    }

    public void setSelectedSchema(String selectedSchema) {
        this.selectedSchema = selectedSchema;
    }

    public void setSaveJavaToFile(boolean saveJavaToFile) {
        this.saveJavaToFile = saveJavaToFile;
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

    public GenerationType getGenerationType() {
        return generationType;
    }

    public void setGenerationType(GenerationType generationType) {
        this.generationType = generationType;
    }

    // 新增 Getter 和 Setter 方法
    public boolean isCreateSwagger() {
        return createSwagger;
    }

    public void setCreateSwagger(boolean createSwagger) {
        this.createSwagger = createSwagger;
    }

    public boolean isCreateValidator() {
        return createValidator;
    }

    public void setCreateValidator(boolean createValidator) {
        this.createValidator = createValidator;
    }

    public boolean isCreateLombok() {
        return createLombok;
    }

    public void setCreateLombok(boolean createLombok) {
        this.createLombok = createLombok;
    }

    public enum GenerationType {
        GENERATE_NEW_CLASS,
        SELECT_EXISTING,
        GENERATE_MAIN_SELECT_JOIN
    }
}
