package com.example.mybatisjoingenerator.models;

public class MyBatisGenerationResult {
    private String selectQuery;
    private String resultMap;
    private String mapperInterface;
    private String mainJavaCode;
    private String joinJavaCode;

    // Getter 和 Setter 方法

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public String getResultMap() {
        return resultMap;
    }

    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    public String getMapperInterface() {
        return mapperInterface;
    }

    public void setMapperInterface(String mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public String getMainJavaCode() {
        return mainJavaCode;
    }

    public void setMainJavaCode(String mainJavaCode) {
        this.mainJavaCode = mainJavaCode;
    }

    public String getJoinJavaCode() {
        return joinJavaCode;
    }

    public void setJoinJavaCode(String joinJavaCode) {
        this.joinJavaCode = joinJavaCode;
    }
}
