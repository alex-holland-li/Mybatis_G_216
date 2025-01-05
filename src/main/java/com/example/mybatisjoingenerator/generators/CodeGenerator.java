package com.example.mybatisjoingenerator.generators;

import com.example.mybatisjoingenerator.models.TableField;
import com.example.mybatisjoingenerator.models.UserSelection;
import com.example.mybatisjoingenerator.models.UserSelection.GenerationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CodeGenerator 类仅负责生成和保存 Java 类
 */
public class CodeGenerator {

    private final CodeGeneratorHelper helper = new CodeGeneratorHelper();

    /**
     * 根据用户选择生成 Java 对象
     *
     * @param selection 用户选择的数据
     * @return 生成的 Java 类源码字符串
     */
    public String generate(UserSelection selection) {
        String savePath = selection.getFilePath();
        GenerationType generationType = selection.getGenerationType();

        StringBuilder allJavaClassesCode = new StringBuilder();

        switch (generationType) {
            case GENERATE_NEW_CLASS:
                // 生成新的 Java 类，并保存
                String javaClassesCode = generateJavaClasses(selection, savePath);
                allJavaClassesCode.append(javaClassesCode);
                break;
            case GENERATE_MAIN_SELECT_JOIN:
                // 生成主对象类和连接对象类，并保存
                String mainAndJoinCode = generateMainAndJoinClasses(selection, savePath);
                allJavaClassesCode.append(mainAndJoinCode);
                break;
            case SELECT_EXISTING:
                // 用户选择现有类，无需生成新的 Java 类
                allJavaClassesCode.append("// 用户选择了现有的 Java 类，无需生成新的类。\n");
                break;
            default:
                throw new IllegalArgumentException("未知的生成类型: " + generationType);
        }

        return allJavaClassesCode.toString();
    }

    /**
     * 生成 Java 类
     * 仅用于 GENERATE_NEW_CLASS
     *
     * @param selection 用户选择的数据
     * @param savePath  保存路径
     * @return 生成的 Java 类源码字符串
     */
    public String generateJavaClasses(UserSelection selection, String savePath) {
        String className = capitalize(selection.getRootObjectName());
        String packageName = selection.getRootObjectName(); // 假设 rootObjectName 是包名
        StringBuilder classCode = new StringBuilder();
        classCode.append(String.format("package %s;\n\n", packageName));

        // 添加必要的导入语句
        List<String> imports = collectImports(selection);
        for (String imp : imports) {
            classCode.append("import ").append(imp).append(";\n");
        }
        if (!imports.isEmpty()) {
            classCode.append("\n");
        }

        // 添加类注解
        if (selection.isCreateSwagger()) {
            classCode.append("@ApiModel(description = \"").append(className).append("对象\")\n");
        }
        if (selection.isCreateLombok()) {
            classCode.append("@Data\n"); // Lombok 的@Data注解
        }

        // 添加类声明
        classCode.append(String.format("public class %s {\n\n", className));

        // 添加主表字段
        for (TableField field : selection.getSelectedMainFields()) {
            // 添加注解（例如 Swagger 和 Validator）
            if (selection.isCreateSwagger()) {
                classCode.append("    @ApiModelProperty(value = \"").append(field.getJavaFieldName()).append("\")\n");
            }
            if (selection.isCreateValidator()) {
                // 根据需要，可以添加不同类型的校验注解
                // 这里以非空校验为例
                classCode.append("    @NotNull\n");
            }

            classCode.append(String.format("    private %s %s;\n",
                    field.getJavaType(), field.getJavaFieldName()));
        }

        // 根据关联关系类型生成 Java 类结构
        String relationType = selection.getRelationType();  // 获取关联关系类型
        if ("一对一".equalsIgnoreCase(relationType)) {
            // 一对一：副表字段封装成一个独立的内部类
            String joinClassName = capitalize(selection.getJoinObjectName());
            classCode.append(String.format("    private %s %s;\n", joinClassName, toCamelCase(selection.getJoinObjectName())));
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            // 一对多：副表字段封装成 List
            String joinClassName = capitalize(selection.getJoinObjectName());
            classCode.append(String.format("    private List<%s> %sList;\n", joinClassName, toCamelCase(selection.getJoinObjectName())));
        } else {
            // 普通：副表字段直接作为成员变量
            for (TableField field : selection.getSelectedJoinFields()) {
                // 添加注解
                if (selection.isCreateSwagger()) {
                    classCode.append("    @ApiModelProperty(value = \"").append(field.getJavaFieldName()).append("\")\n");
                }
                if (selection.isCreateValidator()) {
                    classCode.append("    @NotNull\n");
                }

                classCode.append(String.format("    private %s %s;\n",
                        field.getJavaType(), field.getJavaFieldName()));
            }
        }

        // 添加 Getter 和 Setter 方法（如果没有使用 Lombok）
        if (!selection.isCreateLombok()) {
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
        }

        // 添加副表类到最后
        if ("一对一".equalsIgnoreCase(relationType)) {
            generateInnerClass(classCode, capitalize(selection.getJoinObjectName()), selection, false);  // 一对一，生成非List的副表类
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            generateInnerClass(classCode, capitalize(selection.getJoinObjectName()), selection, true);  // 一对多，生成List的副表类
        }

        classCode.append("}\n");

        // 保存生成的类
        helper.saveJavaFile(savePath, className + ".java", classCode.toString());

        return classCode.toString(); // 返回生成的类源码
    }

    /**
     * 生成主对象类和连接对象类
     *
     * @param selection 用户选择的数据
     * @param savePath  保存路径
     * @return 生成的所有 Java 类源码字符串
     */
    public String generateMainAndJoinClasses(UserSelection selection, String savePath) {
        StringBuilder allClassesCode = new StringBuilder();

        // 生成主对象类
        String mainClassCode = generateJavaClasses(selection, savePath);
        allClassesCode.append(mainClassCode);

        // 生成连接对象类（如果有）
        if ("一对一".equalsIgnoreCase(selection.getRelationType()) || "一对多".equalsIgnoreCase(selection.getRelationType())) {
            String joinClassName = capitalize(selection.getJoinObjectName());
            String packageName = selection.getJoinObjectName(); // 假设 joinObjectName 是包名
            StringBuilder joinClassCode = new StringBuilder();
            joinClassCode.append(String.format("package %s;\n\n", packageName));

            // 添加必要的导入语句
            List<String> imports = collectImportsForJoinClass(selection);
            for (String imp : imports) {
                joinClassCode.append("import ").append(imp).append(";\n");
            }
            if (!imports.isEmpty()) {
                joinClassCode.append("\n");
            }

            // 添加类注解
            if (selection.isCreateSwagger()) {
                joinClassCode.append("@ApiModel(description = \"").append(joinClassName).append("对象\")\n");
            }
            if (selection.isCreateLombok()) {
                joinClassCode.append("@Data\n"); // Lombok 的@Data注解
            }

            // 添加类声明
            joinClassCode.append(String.format("public class %s {\n\n", joinClassName));

            // 添加连接对象类字段
            for (TableField field : selection.getSelectedJoinFields()) {
                // 添加注解
                if (selection.isCreateSwagger()) {
                    joinClassCode.append("    @ApiModelProperty(value = \"").append(field.getJavaFieldName()).append("\")\n");
                }
                if (selection.isCreateValidator()) {
                    joinClassCode.append("    @NotNull\n");
                }

                joinClassCode.append(String.format("    private %s %s;\n",
                        field.getJavaType(), field.getJavaFieldName()));
            }

            // 添加 Getter 和 Setter 方法（如果没有使用 Lombok）
            if (!selection.isCreateLombok()) {
                joinClassCode.append("\n    // Getters and Setters\n");
                for (TableField field : selection.getSelectedJoinFields()) {
                    String camelField = capitalize(field.getJavaFieldName());
                    joinClassCode.append(String.format("    public %s get%s() {\n        return %s;\n    }\n\n",
                            field.getJavaType(), camelField, field.getJavaFieldName()));
                    joinClassCode.append(String.format("    public void set%s(%s %s) {\n        this.%s = %s;\n    }\n\n",
                            camelField, field.getJavaType(), field.getJavaFieldName(),
                            field.getJavaFieldName(), field.getJavaFieldName()));
                }
            }

            // 结束连接对象类定义
            joinClassCode.append("}\n");

            // 保存连接对象类代码
            helper.saveJavaFile(savePath, joinClassName + ".java", joinClassCode.toString());

            allClassesCode.append(joinClassCode.toString());
        }

        return allClassesCode.toString();
    }

    /**
     * 收集生成 Java 类所需的导入语句
     */
    private List<String> collectImports(UserSelection selection) {
        List<String> imports = new ArrayList<>();

        if (selection.isCreateSwagger()) {
            imports.add("io.swagger.annotations.ApiModel");
            imports.add("io.swagger.annotations.ApiModelProperty");
        }

        if (selection.isCreateValidator()) {
            imports.add("javax.validation.constraints.NotNull");
        }

        if (selection.isCreateLombok()) {
            imports.add("lombok.Data");
        }

        // 根据需要添加其他导入
        for (TableField field : selection.getSelectedMainFields()) {
            switch (field.getJavaType()) {
                case "BigDecimal":
                    imports.add("java.math.BigDecimal");
                    break;
                case "LocalDate":
                    imports.add("java.time.LocalDate");
                    break;
                case "LocalDateTime":
                    imports.add("java.time.LocalDateTime");
                    break;
                case "LocalTime":
                    imports.add("java.time.LocalTime");
                    break;
                // 添加其他需要的导入
                default:
                    break;
            }
        }

        for (TableField field : selection.getSelectedJoinFields()) {
            switch (field.getJavaType()) {
                case "BigDecimal":
                    imports.add("java.math.BigDecimal");
                    break;
                case "LocalDate":
                    imports.add("java.time.LocalDate");
                    break;
                case "LocalDateTime":
                    imports.add("java.time.LocalDateTime");
                    break;
                case "LocalTime":
                    imports.add("java.time.LocalTime");
                    break;
                // 添加其他需要的导入
                default:
                    break;
            }
        }

        if ("一对多".equalsIgnoreCase(selection.getRelationType())) {
            imports.add("java.util.List");
        }

        return imports.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 收集生成连接类所需的导入语句
     */
    private List<String> collectImportsForJoinClass(UserSelection selection) {
        List<String> imports = new ArrayList<>();

        if (selection.isCreateSwagger()) {
            imports.add("io.swagger.annotations.ApiModel");
            imports.add("io.swagger.annotations.ApiModelProperty");
        }

        if (selection.isCreateValidator()) {
            imports.add("javax.validation.constraints.NotNull");
        }

        if (selection.isCreateLombok()) {
            imports.add("lombok.Data");
        }

        // 根据需要添加其他导入
        for (TableField field : selection.getSelectedJoinFields()) {
            switch (field.getJavaType()) {
                case "BigDecimal":
                    imports.add("java.math.BigDecimal");
                    break;
                case "LocalDate":
                    imports.add("java.time.LocalDate");
                    break;
                case "LocalDateTime":
                    imports.add("java.time.LocalDateTime");
                    break;
                case "LocalTime":
                    imports.add("java.time.LocalTime");
                    break;
                // 添加其他需要的导入
                default:
                    break;
            }
        }

        return imports.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 生成副表内部类
     */
    private void generateInnerClass(StringBuilder classCode, String joinClassName, UserSelection selection, boolean isList) {
        classCode.append("\n    // 副表类：").append(joinClassName).append("\n");
        classCode.append(String.format("    public static class %s {\n", joinClassName));

        // 添加副表字段
        for (TableField field : selection.getSelectedJoinFields()) {
            // 添加注解
            if (selection.isCreateSwagger()) {
                classCode.append("        @ApiModelProperty(value = \"").append(field.getJavaFieldName()).append("\")\n");
            }
            if (selection.isCreateValidator()) {
                classCode.append("        @NotNull\n");
            }

            classCode.append(String.format("        private %s %s;\n", field.getJavaType(), field.getJavaFieldName()));
        }

        // 添加 Getter 和 Setter 方法（如果没有使用 Lombok）
        if (!selection.isCreateLombok()) {
            classCode.append("\n        // Getters and Setters\n");
            for (TableField field : selection.getSelectedJoinFields()) {
                String camelField = capitalize(field.getJavaFieldName());
                classCode.append(String.format("        public %s get%s() {\n            return %s;\n        }\n\n",
                        field.getJavaType(), camelField, field.getJavaFieldName()));
                classCode.append(String.format("        public void set%s(%s %s) {\n            this.%s = %s;\n        }\n\n",
                        camelField, field.getJavaType(), field.getJavaFieldName(),
                        field.getJavaFieldName(), field.getJavaFieldName()));
            }
        }

        // 结束副表类定义
        classCode.append("    }\n");
    }

    /**
     * 生成 MyBatis SQL 和 ResultMap 供用户复制
     * 注意：这些方法不保存任何文件，只返回字符串
     */

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
        String whereClause = "<where>\n        <!-- 添加查询条件 -->\n    </where>";

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
                    toCamelCase(selection.getJoinObjectName()), capitalize(selection.getJoinObjectName())));  // 为副表创建嵌套对象

            // 映射副表字段到主表中的属性
            for (TableField field : selection.getSelectedJoinFields()) {
                resultMap.append(String.format("        <result column=\"%s_%s\" property=\"%s\" />\n",
                        joinAlias, field.getColumnName(), field.getJavaFieldName()));
            }
            resultMap.append("    </association>\n");
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            // 一对多：使用 collection 来处理副表字段
            resultMap.append(String.format("    <collection property=\"%sList\" ofType=\"%s\">\n",
                    toCamelCase(selection.getJoinObjectName()), capitalize(selection.getJoinObjectName())));
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
     * 生成 Mapper 接口方法
     * 这里只生成方法部分，不生成整个接口类
     */
    public String generateMapperInterface(UserSelection selection) {
        StringBuilder methodsCode = new StringBuilder();

        // 生成查询主表列表的方法
        String mainTable = capitalize(selection.getMainTable());
        String methodName = "select" + mainTable + "List";
        methodsCode.append(String.format("    List<%s> %s(Map<String, Object> params);\n", mainTable, methodName));

        // 根据关联关系生成相应的方法
        String relationType = selection.getRelationType();
        String joinTable = capitalize(selection.getJoinObjectName());

        if ("一对一".equalsIgnoreCase(relationType)) {
            String methodNameOneToOne = "select" + joinTable + "ById";
            methodsCode.append(String.format("    %s %s(int id);\n", joinTable, methodNameOneToOne));
        } else if ("一对多".equalsIgnoreCase(relationType)) {
            String methodNameOneToMany = "select" + joinTable + "ListByParentId";
            methodsCode.append(String.format("    List<%s> %s(int parentId);\n", joinTable, methodNameOneToMany));
        }

        return methodsCode.toString();  // 返回生成的接口方法代码
    }

    /**
     * 生成完整的 Mapper 接口代码，包括接口声明和方法
     */
    public String generateMapperInterfaceFull(UserSelection selection) {
        String mapperName = capitalize(selection.getMainTable()) + "Mapper";
        String mainClass = capitalize(selection.getMainTable());

        StringBuilder mapperCode = new StringBuilder();
        mapperCode.append(String.format("public interface %s {\n\n", mapperName));
        mapperCode.append(generateMapperInterface(selection));
        mapperCode.append("\n}");

        return mapperCode.toString();
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

    /**
     * 将蛇形命名转为驼峰命名
     */
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_' || c == ' ') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
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
        if (selection.getJoinObjectName().equals(joinAlias)) {
            // 如果表别名和表名一样，就不用指定表别名
            return String.format("    %s %s ON %s",
                    joinType,
                    selection.getJoinObjectName(),
                    selection.getJoinCondition());
        } else {
            // 示例：仅支持单一关联关系
            return String.format("    %s %s AS %s ON %s",
                    joinType,
                    selection.getJoinObjectName(),
                    joinAlias,
                    selection.getJoinCondition());
        }
    }
}
