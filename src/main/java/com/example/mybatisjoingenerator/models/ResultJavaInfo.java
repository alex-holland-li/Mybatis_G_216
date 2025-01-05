package com.example.mybatisjoingenerator.models;

/**
 * @author 李运
 */
public class ResultJavaInfo {
    private String className;
    private String packageName;
    private String savePath;

    public ResultJavaInfo() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
