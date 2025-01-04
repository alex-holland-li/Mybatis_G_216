package com.example.mybatisjoingenerator.generators;

import com.example.mybatisjoingenerator.models.TableField;
import com.example.mybatisjoingenerator.models.UserSelection;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGenerator {

    /**
     * 生成 MyBatis XML 查询动态 SQL
     */
    public String generateSelectQuery(UserSelection selection) {
        String mainAlias = selection.getMainTableAlias();
        String joinAlias = selection.getJoinTableAlias();
        String relationType = selection.getRelationType();

        // 构建 SELECT 字段部分
        String selectFields = buildSelectFields(selection.getSelectedMainFields(), mainAlias)
                + ",\n"
                + buildSelectFields(selection.getSelectedJoinFields(), joinAlias);

        // 构建 FROM 子句
        // 如果表别名和表名一样，就不用指定表别名
        String fromClause = selection.getMainTable().equals(mainAlias) ?
                String.format("FROM %s ", selection.getMainTable()) : String.format("FROM %s AS %s", selection.getMainTable(), mainAlias);

        // 构建 JOIN 子句
        String joinClause = buildJoinClause(selection, joinAlias);

        // 构建 WHERE 子句（可根据需要扩展）
        String whereClause = "";

        // 完整的 SELECT 语句
        String selectQuery = String.format("<select id=\"select%sJoin\" parameterType=\"map\" resultMap=\"%sResultMap\">\n",
                capitalize(mainAlias), mainAlias)
                + "    SELECT\n"
                + selectFields + "\n"
                + fromClause + "\n"
                + joinClause + "\n"
                + whereClause + "\n"
                + "</select>";

        return selectQuery;
    }

    /**
     * 构建 SELECT 字段部分
     */
    private String buildSelectFields(List<TableField> fields, String tableAlias) {
        return fields.stream()
                .map(field -> String.format("        %s.%s AS %s_%s",
                        tableAlias,
                        field.getColumnName(),
                        tableAlias,
                        field.getColumnName()))
                .collect(Collectors.joining(",\n"));
    }

    /**
     * 构建 JOIN 子句
     */
    private String buildJoinClause(UserSelection selection, String joinAlias) {
        String joinType = "LEFT JOIN"; // 可根据 relationType 选择 JOIN 类型
        if (selection.getJoinTable().equals(joinAlias)) {
            // 如果表别名和表名一样，就不用指定表别名
            return String.format("    %s %s ON %s",
                    joinType,
                    selection.getJoinTable(),
                    selection.getJoinCondition());
        } else {
            // 示例：仅支持单一关联关系
            return String.format("    %s %s AS %s ON %s",
                    joinType,
                    selection.getJoinTable(),
                    joinAlias,
                    selection.getJoinCondition());
        }


    }

    /**
     * 生成 MyBatis XML resultMap
     */
    public String generateResultMap(UserSelection selection) {
        String mainAlias = selection.getMainTableAlias();
        String joinAlias = selection.getJoinTableAlias();
        String relationType = selection.getRelationType();  // 获取关联关系类型

        // 构建 resultMap 开头
        StringBuilder resultMap = new StringBuilder();
        resultMap.append(String.format("<resultMap id=\"%sResultMap\" type=\"%s\">\n", mainAlias, capitalize(selection.getMainTable())));

        // 添加主表字段映射
        for (TableField field : selection.getSelectedMainFields()) {
            resultMap.append(String.format("    <result column=\"%s_%s\" property=\"%s\" />\n",
                    mainAlias, field.getColumnName(), field.getJavaFieldName()));
        }

        // 根据关联关系类型进行不同的处理
        if ("一对一".equalsIgnoreCase(relationType)) {
            // 一对一：副表字段封装成嵌套对象，使用 association
            resultMap.append(String.format("    <association property=\"%s\" javaType=\"%s\">\n",
                    joinAlias, capitalize(selection.getJoinTable())));  // 为副表创建嵌套对象

            // 映射副表字段到主表中的属性
            for (TableField field : selection.getSelectedJoinFields()) {
                resultMap.append(String.format("        <result column=\"%s_%s\" property=\"%s\" />\n",
                        joinAlias, field.getColumnName(), field.getJavaFieldName()));
            }
            resultMap.append("    </association>\n");
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            // 一对多：使用 collection 来处理副表字段
            resultMap.append(String.format("    <collection property=\"%sList\" ofType=\"%s\">\n",
                    joinAlias, capitalize(selection.getJoinTable())));
            for (TableField field : selection.getSelectedJoinFields()) {
                resultMap.append(String.format("        <result column=\"%s_%s\" property=\"%s\" />\n",
                        joinAlias, field.getColumnName(), field.getJavaFieldName()));
            }
            resultMap.append("    </collection>\n");
        } else {
            // 普通：副表字段直接映射
            for (TableField field : selection.getSelectedJoinFields()) {
                resultMap.append(String.format("    <result column=\"%s_%s\" property=\"%s\" />\n",
                        joinAlias, field.getColumnName(), field.getJavaFieldName()));
            }
        }

        resultMap.append("</resultMap>");
        return resultMap.toString();
    }


    /**
     * 生成 Mapper 接口代码
     */
    public String generateMapperInterface(UserSelection selection) {
        String mapperName = capitalize(selection.getMainTable()) + "Mapper";

        String mapperInterface = String.format("public interface %s {\n\n", mapperName)
                + String.format("    List<%s> select%sJoin(Map<String, Object> params);\n\n",
                capitalize(selection.getMainTable()), capitalize(selection.getMainTable()))
                + "}";

        return mapperInterface;
    }

    /**
     * 生成 Java 对象源码
     */
    public String generateJavaClasses(UserSelection selection) {
        String className = capitalize(selection.getMainTable());
        StringBuilder classCode = new StringBuilder();
        classCode.append(String.format("public class %s {\n\n", className));

        // 添加主表字段
        for (TableField field : selection.getSelectedMainFields()) {
            classCode.append(String.format("    private %s %s;\n",
                    field.getJavaType(), field.getJavaFieldName()));
        }

        // 根据关联关系类型生成 Java 类结构
        String relationType = selection.getRelationType();  // 获取关联关系类型
        if ("一对一".equalsIgnoreCase(relationType)) {
            // 一对一：副表字段封装成一个独立的内部类
            String joinClassName = capitalize(selection.getJoinTable());
            classCode.append(String.format("    private %s %s;\n", joinClassName, selection.getJoinTableAlias()));
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            // 一对多：副表字段封装成 List
            String joinClassName = capitalize(selection.getJoinTable());
            classCode.append(String.format("    private List<%s> %sList;\n", joinClassName, selection.getJoinTableAlias()));
        } else {
            // 普通：副表字段直接作为成员变量
            for (TableField field : selection.getSelectedJoinFields()) {
                classCode.append(String.format("    private %s %s;\n",
                        field.getJavaType(), field.getJavaFieldName()));
            }
        }

        // 添加 Getter 和 Setter 方法
        classCode.append("\n    // Getters and Setters\n");
        for (TableField field : selection.getSelectedMainFields()) {
            String camelField = capitalize(field.getJavaFieldName());
            classCode.append(String.format("    public %s get%s() {\n        return %s;\n    }\n\n",
                    field.getJavaType(), camelField, field.getJavaFieldName()));
            classCode.append(String.format("    public void set%s(%s %s) {\n        this.%s = %s;\n    }\n\n",
                    camelField, field.getJavaType(), field.getJavaFieldName(),
                    field.getJavaFieldName(), field.getJavaFieldName()));
        }

        for (TableField field : selection.getSelectedJoinFields()) {
            String camelField = capitalize(field.getJavaFieldName());
            classCode.append(String.format("    public %s get%s() {\n        return %s;\n    }\n\n",
                    field.getJavaType(), camelField, field.getJavaFieldName()));
            classCode.append(String.format("    public void set%s(%s %s) {\n        this.%s = %s;\n    }\n\n",
                    camelField, field.getJavaType(), field.getJavaFieldName(),
                    field.getJavaFieldName(), field.getJavaFieldName()));
        }

        // 添加副表类到最后
        if ("一对一".equalsIgnoreCase(relationType)) {
            generateInnerClass(classCode, capitalize(selection.getJoinTable()), selection, false);  // 一对一，生成非List的副表类
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            generateInnerClass(classCode, capitalize(selection.getJoinTable()), selection, true);  // 一对多，生成List的副表类
        }

        classCode.append("}");

        return classCode.toString();
    }

    // 通用的副表内部类生成方法
    private void generateInnerClass(StringBuilder classCode, String joinClassName, UserSelection selection, boolean isList) {
        classCode.append("\n    // 副表类：").append(joinClassName).append("\n");
        classCode.append(String.format("    public static class %s {\n", joinClassName));

        // 添加副表字段
        for (TableField field : selection.getSelectedJoinFields()) {
            classCode.append(String.format("        private %s %s;\n", field.getJavaType(), field.getJavaFieldName()));
        }

        // 添加副表的 Getter 和 Setter 方法
        classCode.append("\n        // Getters and Setters\n");
        for (TableField field : selection.getSelectedJoinFields()) {
            String camelField = capitalize(field.getJavaFieldName());
            classCode.append(String.format("        public %s get%s() {\n            return %s;\n        }\n\n",
                    field.getJavaType(), camelField, field.getJavaFieldName()));
            classCode.append(String.format("        public void set%s(%s %s) {\n            this.%s = %s;\n        }\n\n",
                    camelField, field.getJavaType(), field.getJavaFieldName(),
                    field.getJavaFieldName(), field.getJavaFieldName()));
        }

        // 结束副表类定义
        classCode.append("    }\n");
    }


    /**
     * 将字符串首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public void saveJavaClasses(String packagePath, String fileName, String javaClassesCode) {
        try {
            // 创建文件夹路径（确保存在）
            java.io.File directory = new java.io.File(packagePath);
            if (!directory.exists()) {
                directory.mkdirs();  // 如果目录不存在，则创建它
            }

            // 创建文件并保存代码
            java.io.File javaFile = new java.io.File(directory, fileName);  // 使用用户输入的文件名
            try (java.io.FileWriter writer = new java.io.FileWriter(javaFile)) {
                writer.write(javaClassesCode);  // 写入生成的 Java 代码
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
