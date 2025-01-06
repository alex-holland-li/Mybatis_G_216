package com.example.mybatisjoingenerator.models;

import com.example.mybatisjoingenerator.ui.JavaSelectOrCreatePanel.GeneratedClassInfo;
import com.example.mybatisjoingenerator.ui.JavaSelectOrCreatePanel.SelectedClassInfo;

import java.util.List;

/**
 * @author 李运
 */
public class UserSelection {

    private String joinObjectName;
    //选择的数据库链接名
    private String selectedDatabase;
    //选择的数据库Schema名
    private String selectedSchema;
    //主表
    private String mainTable;
    //主表别名
    private String mainTableAlias;
    //链接表
    private String joinTable;
    //链接表别名
    private String joinTableAlias;
    //链接条件
    private String joinCondition;
    //主表字段
    private List<TableField> selectedMainFields;
    //链接表字段
    private List<TableField> selectedJoinFields;


    // 生成类型: 生成新类、选择现有类、生成主查询+链接查询
    private GenerationType generationType;

    // 关系类型:普通， 一对一、一对多、
    private String relationType;

    // 生成的主类信息
    private GeneratedClassInfo generatedMainClassInfo;
    // 选择的主类信息
    private SelectedClassInfo selectedMainClassInfo;
    // 生成的链接类信息
    private GeneratedClassInfo generatedJoinClassInfo;
    // 选择的链接类信息
    private SelectedClassInfo selectedJoinClassInfo;
    // 生成的普通类信息
    private GeneratedClassInfo generatedClassInfo;
    // 选择的普通类信息
    private SelectedClassInfo selectedClassInfo;

    // 是否创建Swagger注解
    private boolean createSwagger;
    // 是否创建Validator注解
    private boolean createValidator;
    // 是否创建Lombok注解
    private boolean createLombok;

    // 构造函数
    public UserSelection(String selectedDatabase, String selectedSchema, String mainTable, String mainTableAlias,
                         String joinTable, String joinTableAlias, String joinCondition,
                         List<TableField> selectedMainFields, List<TableField> selectedJoinFields,
                         String relationType, GeneratedClassInfo generatedClassInfo, GeneratedClassInfo generatedMainClassInfo,
                         GeneratedClassInfo generatedJoinClassInfo, SelectedClassInfo selectedClassInfo, SelectedClassInfo selectedMainClassInfo, SelectedClassInfo selectedJoinClassInfo, GenerationType generationType,
                         boolean createSwagger, boolean createValidator, boolean createLombok, String joinObjectName) {
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
        this.generatedClassInfo = generatedClassInfo;
        this.generatedMainClassInfo = generatedMainClassInfo;
        this.generatedJoinClassInfo = generatedJoinClassInfo;
        this.selectedClassInfo = selectedClassInfo;
        this.selectedMainClassInfo = selectedMainClassInfo;
        this.selectedJoinClassInfo = selectedJoinClassInfo;
        this.generationType = generationType;
        this.createSwagger = createSwagger;
        this.createValidator = createValidator;
        this.createLombok = createLombok;
        this.joinObjectName = joinObjectName;
    }

    // Getter 和 Setter 方法

    public String getJoinObjectName() {
        return joinObjectName;
    }

    public void setJoinObjectName(String joinObjectName) {
        this.joinObjectName = joinObjectName;
    }

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

    public GenerationType getGenerationType() {
        return generationType;
    }

    public void setGenerationType(GenerationType generationType) {
        this.generationType = generationType;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public GeneratedClassInfo getGeneratedMainClassInfo() {
        return generatedMainClassInfo;
    }

    public void setGeneratedMainClassInfo(GeneratedClassInfo generatedMainClassInfo) {
        this.generatedMainClassInfo = generatedMainClassInfo;
    }

    public SelectedClassInfo getSelectedMainClassInfo() {
        return selectedMainClassInfo;
    }

    public void setSelectedMainClassInfo(SelectedClassInfo selectedMainClassInfo) {
        this.selectedMainClassInfo = selectedMainClassInfo;
    }

    public GeneratedClassInfo getGeneratedJoinClassInfo() {
        return generatedJoinClassInfo;
    }

    public void setGeneratedJoinClassInfo(GeneratedClassInfo generatedJoinClassInfo) {
        this.generatedJoinClassInfo = generatedJoinClassInfo;
    }

    public SelectedClassInfo getSelectedJoinClassInfo() {
        return selectedJoinClassInfo;
    }

    public void setSelectedJoinClassInfo(SelectedClassInfo selectedJoinClassInfo) {
        this.selectedJoinClassInfo = selectedJoinClassInfo;
    }

    public GeneratedClassInfo getGeneratedClassInfo() {
        return generatedClassInfo;
    }

    public void setGeneratedClassInfo(GeneratedClassInfo generatedClassInfo) {
        this.generatedClassInfo = generatedClassInfo;
    }

    public SelectedClassInfo getSelectedClassInfo() {
        return selectedClassInfo;
    }

    public void setSelectedClassInfo(SelectedClassInfo selectedClassInfo) {
        this.selectedClassInfo = selectedClassInfo;
    }

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
