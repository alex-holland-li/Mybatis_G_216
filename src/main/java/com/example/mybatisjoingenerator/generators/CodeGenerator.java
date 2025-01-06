package com.example.mybatisjoingenerator.generators;

import com.example.mybatisjoingenerator.context.DataBaseContext;
import com.example.mybatisjoingenerator.models.MyBatisGenerationResult;
import com.example.mybatisjoingenerator.models.TableField;
import com.example.mybatisjoingenerator.models.UserSelection;
import com.example.mybatisjoingenerator.ui.JavaSelectOrCreatePanel.GeneratedClassInfo;
import com.example.mybatisjoingenerator.ui.JavaSelectOrCreatePanel.SelectedClassInfo;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DataType;
import com.intellij.ide.util.PropertiesComponent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CodeGenerator 类负责生成 MyBatis 的 SELECT 查询、resultMap 和 Mapper 接口方法
 */
public class CodeGenerator {

    private final CodeGeneratorHelper helper = new CodeGeneratorHelper();
    private final Configuration cfg;

    public CodeGenerator() {
        cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates"); // 确保模板文件在 resources/templates 目录下
        cfg.setDefaultEncoding("UTF-8");
    }

    /**
     * 将数据库表名转换为标准的Java类名（驼峰命名法）
     *
     * @param tableName 数据库表名（例如：user_table）
     * @return 转换后的Java类名（例如：UserTable）
     */
    public static String toJavaClassName(String tableName) {
        StringBuilder className = new StringBuilder();
        String[] parts = tableName.split("_");

        for (String part : parts) {
            // 将每个部分的首字母大写，并将其余字母小写
            className.append(part.substring(0, 1).toUpperCase());
            className.append(part.substring(1).toLowerCase());
        }

        return className.toString();
    }

    public static String toJavaFileName(String tableName) {
        StringBuilder className = new StringBuilder();
        String[] parts = tableName.split("_");

        int i = 1;
        for (String part : parts) {
            // 将第一个字母的小写
            if (i++ == 1) {
                className.append(part.substring(0, 1).toLowerCase());
            } else {
                className.append(part.substring(0, 1).toUpperCase());
            }
            className.append(part.substring(1).toLowerCase());
        }

        return className.toString();
    }

    /**
     * 根据用户选择生成 MyBatis 相关配置
     *
     * @param selection 用户选择的数据
     * @return 生成的 MyBatis 相关配置封装类
     */
    public MyBatisGenerationResult generate(UserSelection selection) {
        MyBatisGenerationResult result = new MyBatisGenerationResult();

        // 局部变量保存生成的代码
        String mainJavaCode = null;
        String joinJavaCode = null;

        switch (selection.getGenerationType()) {
            case GENERATE_NEW_CLASS:
                if ("普通".equalsIgnoreCase(selection.getRelationType())) {
                    // 1. GENERATE_NEW_CLASS 且 普通
                    mainJavaCode = generateJavaClass(selection.getGeneratedClassInfo(), selection);
                } else if ("一对一".equalsIgnoreCase(selection.getRelationType()) ||
                        "一对多".equalsIgnoreCase(selection.getRelationType())) {
                    // 2. GENERATE_NEW_CLASS 且 一对一或一对多
                    mainJavaCode = generateJavaClass(selection.getGeneratedMainClassInfo(), selection);
                    joinJavaCode = generateJavaClass(selection.getGeneratedJoinClassInfo(), selection);
                }
                break;
            case GENERATE_MAIN_SELECT_JOIN:
                // 3. GENERATE_MAIN_SELECT_JOIN
                mainJavaCode = generateJavaClass(selection.getGeneratedMainClassInfo(), selection);
                break;
            case SELECT_EXISTING:
                // 4. SELECT_EXISTING 且 普通 或 5. SELECT_EXISTing 且 一对一或一对多
                // 不生成 Java 类，但生成 resultMap
                break;
            default:
                throw new IllegalArgumentException("未知的生成类型: " + selection.getGenerationType());
        }

        String resultMapCode = generateResultMap(selection);

        // 生成 SELECT 查询
        String selectSql = generateSelectQuery(selection);
        // 生成 Mapper 接口方法
        String mapperMethods = generateMapperInterfaceMethods(selection);

        // 封装生成结果
        result.setResultMap(resultMapCode);
        result.setSelectQuery(selectSql);
        result.setMapperInterface(mapperMethods);

        // 将生成的 Java 代码保存到结果中（如果需要）
        result.setMainJavaCode(mainJavaCode);
        result.setJoinJavaCode(joinJavaCode);

        return result;
    }

    /**
     * 生成 Java 类并保存到文件
     *
     * @param classInfo 生成类的信息
     * @param selection 用户选择的数据
     * @return
     */
    private String generateJavaClass(GeneratedClassInfo classInfo, UserSelection selection) {
        try {
            PropertiesComponent properties = PropertiesComponent.getInstance();
            String userName = properties.getValue("user.name");
            if (userName == null || userName.isEmpty()) {
                userName = System.getProperty("user.name");
            }
            // 加载模板
            Template template = cfg.getTemplate("EntityTemplate.ftl");

            // 准备数据模型
            Map<String, Object> templateData = prepareTemplateData(selection, classInfo);
            templateData.put("author", userName);

            // 生成代码
            StringWriter out = new StringWriter();
            template.process(templateData, out);
            String classCode = out.toString();

            // 保存生成的类
            helper.saveFile(classInfo.getSavePath(),
                    classInfo.getClassName() + ".java", classCode);

            return classCode;

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            System.err.println("生成 Java 类时发生错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 生成 SELECT 查询语句
     *
     * @param selection 用户选择的数据
     * @return 生成的 SELECT 查询语句字符串
     */
    private String generateSelectQuery(UserSelection selection) {
        try {
            Template selectTemplate = cfg.getTemplate("SelectSqlTemplate.ftl");

            Map<String, Object> selectData = new HashMap<>();
            selectData.put("id", getSelectId(selection));
            selectData.put("resultMapId", getResultMapId(selection));


            // 构建 SELECT 字段部分
            String selectFields = buildSelectFieldsForMyBatis(selection.getSelectedMainFields(), selection.getMainTableAlias());
            selectFields += ", \n" + buildSelectFieldsForMyBatis(selection.getSelectedJoinFields(), selection.getJoinTableAlias());
            selectData.put("selectFields", selectFields);

            selectData.put("from", createFromClause(selection));

            // 构建 JOIN 子句
            String joinClause = buildJoinClause(selection, selection.getJoinTableAlias());
            selectData.put("joinClause", joinClause);

            // 生成 SELECT SQL
            StringWriter selectOut = new StringWriter();
            selectTemplate.process(selectData, selectOut);
            return selectOut.toString();

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return "// 生成 SELECT 查询时发生错误：" + e.getMessage();
        }
    }

    private String getResultMapId(UserSelection selection) {
        return getSelectId(selection) + "ResultMap";
    }

    private String getSelectId(UserSelection selection) {
        return "select" + toJavaClassName(selection.getMainTable()) + "With" + toJavaClassName(selection.getJoinTable());
    }

    private String createFromClause(UserSelection selection) {
        String mainTableAlias = selection.getMainTableAlias();
        if (mainTableAlias != null && mainTableAlias.trim().length() > 0
                && !mainTableAlias.equalsIgnoreCase(selection.getMainTable())) {
            return selection.getMainTable() + " AS " + mainTableAlias;

        }
        return selection.getMainTable();
    }

    /**
     * 生成 resultMap
     *
     * @param selection 用户选择的数据
     * @return 生成的 resultMap 字符串
     */
    private String generateResultMap(UserSelection selection) {
        try {
            Template resultMapTemplate = cfg.getTemplate("ResultMapTemplate.ftl");

            Map<String, Object> resultMapData = new HashMap<>();
            resultMapData.put("resultMapId", getResultMapId(selection));
            resultMapData.put("mainClass", getMainClassFullyQualifiedName(selection));
            resultMapData.put("mainAlias", selection.getMainTableAlias());
            resultMapData.put("relationType", selection.getRelationType());

            // 主表字段
            List<Map<String, Object>> mainFields = selection.getSelectedMainFields().stream()
                    .map(field -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("columnName", field.getColumnName());
                        map.put("javaFieldName", field.getJavaFieldName());
                        return map;
                    })
                    .collect(Collectors.toList());
            resultMapData.put("mainFields", mainFields);

            List<Map<String, Object>> joinFields = selection.getSelectedJoinFields().stream()
                    .map(field -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("columnName", field.getColumnName());
                        map.put("javaFieldName", field.getJavaFieldName());
                        return map;
                    })
                    .collect(Collectors.toList());
            resultMapData.put("joinAlias", selection.getJoinTableAlias());
            resultMapData.put("joinFields", joinFields);
            resultMapData.put("joinObjectName", getJoinObjectName(selection));
            resultMapData.put("joinClass", getJoinFullyQualifiedName(selection));


            // 生成 resultMap
            StringWriter resultMapOut = new StringWriter();
            resultMapTemplate.process(resultMapData, resultMapOut);
            return resultMapOut.toString();

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return "// 生成 resultMap 时发生错误：" + e.getMessage();
        }
    }

    private String getMainClassFullyQualifiedName(UserSelection selection) {
        if (selection.getSelectedMainClassInfo() != null) {
            return getMainClassFullyQualifiedName(selection.getSelectedMainClassInfo());
        } else if (selection.getGeneratedMainClassInfo() != null) {
            return getMainClassFullyQualifiedName(selection.getGeneratedMainClassInfo());
        } else if (selection.getGeneratedClassInfo() != null) {
            return getMainClassFullyQualifiedName(selection.getGeneratedClassInfo());
        } else if (selection.getSelectedClassInfo() != null) {
            return getMainClassFullyQualifiedName(selection.getSelectedClassInfo());
        }
        return "";
    }

    /**
     * 生成 Mapper 接口方法
     *
     * @param selection 用户选择的数据
     * @return 生成的接口方法代码字符串
     */
    private String generateMapperInterfaceMethods(UserSelection selection) {

        //生成接口：返回参数 方法名 ()
        StringBuilder methodsCode = new StringBuilder();
        methodsCode.append("\n\t");
        methodsCode.append("    ");

        // 根据关联关系生成相应的方法
        String relationType = selection.getRelationType();
        UserSelection.GenerationType generationType = selection.getGenerationType();

        switch (relationType) {
            case "一对一", "一对多" -> {
                if (generationType == UserSelection.GenerationType.GENERATE_NEW_CLASS) {
                    // 生成两个新类
                    methodsCode.append(getMainClassName(selection.getGeneratedMainClassInfo()));
                    methodsCode.append(" ");
                } else if (generationType == UserSelection.GenerationType.SELECT_EXISTING) {
                    //选择两个类
                    methodsCode.append(getMainClassName(selection.getSelectedMainClassInfo()));
                    methodsCode.append(" ");

                } else if (generationType == UserSelection.GenerationType.GENERATE_MAIN_SELECT_JOIN) {
                    methodsCode.append(getMainClassName(selection.getGeneratedMainClassInfo()));
                    methodsCode.append(" ");
                }
            }
            case "普通" -> {
                if (generationType == UserSelection.GenerationType.GENERATE_NEW_CLASS) {
                    // 生成一个类
                    methodsCode.append(getMainClassName(selection.getGeneratedClassInfo()));
                    methodsCode.append(" ");
                } else if (generationType == UserSelection.GenerationType.SELECT_EXISTING) {
                    //选择一个类
                    methodsCode.append(getMainClassName(selection.getSelectedClassInfo()));
                    methodsCode.append(" ");
                }
            }
            default -> {
            }
        }

        //方法名
        methodsCode.append(getSelectId(selection));
        methodsCode.append("();\n\t");

        return methodsCode.toString();  // 返回生成的接口方法代码
    }

    private String getMainClassName(SelectedClassInfo selectedMainClassInfo) {
        return selectedMainClassInfo.getClassName();
    }

    private String getMainClassFullyQualifiedName(SelectedClassInfo selectedMainClassInfo) {
        return selectedMainClassInfo.getFullyQualifiedName();
    }

    private String getMainClassName(GeneratedClassInfo generatedMainClassInfo) {
        return generatedMainClassInfo.getClassName();
    }

    private String getMainClassFullyQualifiedName(GeneratedClassInfo generatedMainClassInfo) {
        return generatedMainClassInfo.getPackageName() + "." + generatedMainClassInfo.getClassName();
    }

    /**
     * 生成校验注解列表
     *
     * @param selection 用户选择的数据
     * @param classInfo 生成类的信息
     * @param field     表字段信息
     * @return 校验注解的字符串列表
     */
    private List<String> generateValidationAnnotations(UserSelection selection, GeneratedClassInfo classInfo, TableField field) {

        // 确定字段所属的表名
        String tableName;
        if (classInfo.isJoinClass()) {
            tableName = selection.getJoinTable();
        } else {
            tableName = selection.getMainTable();
        }

        // 使用 DataBaseContext 获取 DasColumn 对象
        DasColumn dasColumn = DataBaseContext.getColumn(
                selection.getSelectedDatabase(),
                selection.getSelectedSchema(),
                tableName,
                field.getColumnName()
        );

        return createValidationAnnotations(dasColumn);

    }


    /**
     * 根据 DasColumn 信息生成参数校验注解列表
     *
     * @param dasColumn 数据库列信息
     * @return 校验注解列表
     */
    private List<String> createValidationAnnotations(DasColumn dasColumn) {
        List<String> annotations = new ArrayList<>();

        if (dasColumn == null) {
            return annotations; // 返回空列表，不添加任何注解
        }

        // 检查是否为 NOT NULL
        if (dasColumn.isNotNull()) {
            annotations.add("@NotNull");
        }

        // 获取列的数据类型
        String dataType = dasColumn.getDataType().typeName.toLowerCase();

        // 处理 'tinyint(1)' 为 Boolean
        if (dataType.startsWith("tinyint") && dasColumn.getDataType().size == 1) {
            dataType = "boolean";
        } else {
            // 去掉 'unsigned' 和长度规范（例如： "bigint unsigned" -> "bigint"）
            dataType = dataType.replaceAll("\\s+unsigned", "").split("\\(")[0].trim();
        }

        // 获取 DataType 的相关信息
        DataType typeInfo = dasColumn.getDataType();
        int size = typeInfo.size;
        int scale = typeInfo.scale;
        List<String> enumValues = typeInfo.enumValues;

        // 根据 dataType 和 DataType 信息生成参数校验规则注解
        switch (dataType) {
            case "boolean":
                // 通常不需要额外的校验注解
                break;

            // 整型
            case "tinyint":
            case "smallint":
            case "mediumint":
            case "int":
            case "integer":
            case "bigint":
                //addIntegerAnnotations(annotations, dataType);
                break;

            // 浮点型
            case "float":
            case "double":
            case "decimal":
            case "numeric":
                addFloatingPointAnnotations(annotations, dataType, size, scale);
                break;

            // 日期和时间类型
            case "date":
            case "datetime":
            case "timestamp":
                //annotations.add("@PastOrPresent"); // 根据具体需求调整
                break;
            case "time":
                // 使用 @Pattern 校验时间格式，例如 "HH:mm:ss"
                annotations.add("@Pattern(regexp = \"^(?:[01]\\d|2[0-3]):(?:[0-5]\\d):(?:[0-5]\\d)$\", message = \"时间格式必须为 HH:mm:ss\")");
                break;
            case "year":
                // 假设年份在某个合理范围内，可以根据 size 调整
                //annotations.add("@Min(value = 1900)");
                //annotations.add("@Max(value = 2100)");
                break;

            // 字符串类型
            case "varchar":
            case "char":
            case "text":
            case "tinytext":
            case "mediumtext":
            case "longtext":
                if (size > 0) {
                    annotations.add("@Size(max = " + size + ")");
                }
                break;

            // 二进制类型
            case "binary":
            case "varbinary":
            case "tinyblob":
            case "blob":
            case "mediumblob":
            case "longblob":
                // 通常不需要标准的 Bean Validation 注解
                break;

            // ENUM 类型
            case "enum":
                if (enumValues != null && !enumValues.isEmpty()) {
                    String regex = buildEnumRegex(enumValues);
                    annotations.add("@Pattern(regexp = \"" + regex + "\", message = \"值必须为 " + enumValues + "\")");
                }
                break;

            // SET 类型
            case "set":
                if (enumValues != null && !enumValues.isEmpty()) {
                    String regex = buildSetRegex(enumValues);
                    annotations.add("@Pattern(regexp = \"" + regex + "\", message = \"值必须为 " + enumValues + " 的组合\")");
                }
                break;

            // JSON 类型
            case "json":
                // 简单校验 JSON 字符串格式
                annotations.add("@Pattern(regexp = \"^\\\\{.*\\\\}$\", message = \"必须是有效的 JSON 字符串\")");
                break;

            // 空间类型
            case "geometry":
            case "point":
            case "linestring":
            case "polygon":
            case "multipoint":
            case "multilinestring":
            case "multipolygon":
            case "geometrycollection":
                // 空间类型通常不需要标准的 Bean Validation 注解
                break;

            default:
                System.err.println("警告: 未处理的数据类型 " + dataType + "。将不添加校验注解。");
        }

        return annotations;
    }

    /**
     * 为整型数据类型添加 @Min 和 @Max 注解
     *
     * @param annotations 校验注解列表
     * @param dataType    数据类型
     */
    private void addIntegerAnnotations(List<String> annotations, String dataType) {
        switch (dataType) {
            case "tinyint":
                annotations.add("@Min(value = -128)");
                annotations.add("@Max(value = 127)");
                break;
            case "smallint":
                annotations.add("@Min(value = -32768)");
                annotations.add("@Max(value = 32767)");
                break;
            case "mediumint":
                annotations.add("@Min(value = -8388608)");
                annotations.add("@Max(value = 8388607)");
                break;
            case "int":
            case "integer":
                annotations.add("@Min(value = -2147483648)");
                annotations.add("@Max(value = 2147483647)");
                break;
            case "bigint":
                annotations.add("@Min(value = -9223372036854775808L)");
                annotations.add("@Max(value = 9223372036854775807L)");
                break;
            default:
                // 如果有其他整型类型，可以在这里处理
                break;
        }
    }

    /**
     * 为浮点型数据类型添加相应的校验注解
     *
     * @param annotations 校验注解列表
     * @param dataType    数据类型
     * @param size        总位数
     * @param scale       小数位数
     */
    private void addFloatingPointAnnotations(List<String> annotations, String dataType, int size, int scale) {
        switch (dataType) {
            case "float":
                // @DecimalMin 和 @DecimalMax 根据具体需求设置
                //annotations.add("@DecimalMin(value = \"-3.4028235E38\")");
                //annotations.add("@DecimalMax(value = \"3.4028235E38\")");
                break;
            case "double":
                //annotations.add("@DecimalMin(value = \"-1.7976931348623157E308\")");
                //annotations.add("@DecimalMax(value = \"1.7976931348623157E308\")");
                break;
            case "decimal":
            case "numeric":
                if (size > 0 && scale >= 0) {
                    annotations.add("@Digits(integer = " + (size - scale) + ", fraction = " + scale + ")");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 构建 ENUM 类型的正则表达式
     *
     * @param enumValues ENUM 的可选值
     * @return 正则表达式字符串
     */
    private String buildEnumRegex(List<String> enumValues) {
        StringBuilder regex = new StringBuilder("^(");
        for (int i = 0; i < enumValues.size(); i++) {
            regex.append(Pattern.quote(enumValues.get(i)));
            if (i < enumValues.size() - 1) {
                regex.append("|");
            }
        }
        regex.append(")$");
        return regex.toString();
    }

    /**
     * 构建 SET 类型的正则表达式
     *
     * @param enumValues SET 的可选值
     * @return 正则表达式字符串
     */
    private String buildSetRegex(List<String> enumValues) {
        StringBuilder regex = new StringBuilder("^(?:" + String.join("|", enumValues) + ")(?:,(?:" + String.join("|", enumValues) + "))*$");
        return regex.toString();
    }

    /**
     * 准备模板数据用于生成 Java 类
     *
     * @param selection 用户选择的数据
     * @param classInfo 生成类的信息
     * @return 数据模型
     */
    private Map<String, Object> prepareTemplateData(UserSelection selection, GeneratedClassInfo classInfo) {
        Map<String, Object> data = new HashMap<>();

        data.put("packageName", classInfo.getPackageName());
        data.put("className", classInfo.getClassName());
        data.put("createSwagger", selection.isCreateSwagger());
        data.put("createValidator", selection.isCreateValidator());
        data.put("createLombok", selection.isCreateLombok());
        data.put("relationType", selection.getRelationType());

        // 处理字段
        List<Map<String, Object>> fields = new ArrayList<>();
        List<TableField> selectedFields;
        if (classInfo.isJoinClass()) {
            selectedFields = selection.getSelectedJoinFields();
        } else {
            selectedFields = selection.getSelectedMainFields();
        }

        for (TableField field : selectedFields) {
            Map<String, Object> fieldData = new HashMap<>();
            fieldData.put("javaType", field.getJavaType());
            fieldData.put("javaFieldName", field.getJavaFieldName());

            if (selection.isCreateValidator()) {
                // 生成校验注解
                List<String> validationAnnotations = generateValidationAnnotations(selection, classInfo, field);
                fieldData.put("validationAnnotations", validationAnnotations);
            }

            if (selection.isCreateSwagger()) {
                fieldData.put("swaggerAnnotation", "@ApiModelProperty(value = \"" + field.getComment() + "\")");
            }

            fields.add(fieldData);
        }
        data.put("fields", fields);

        // 关联关系相关
        if (!"普通".equalsIgnoreCase(selection.getRelationType())) {
            String joinClass = getJoinClassName(selection);
            String joinObjectName = getJoinObjectName(selection);
            String joinFullyQualifiedName = getJoinFullyQualifiedName(selection);

            data.put("joinClass", joinClass);
            data.put("joinObjectName", joinObjectName);
            data.put("joinFullyQualifiedName", joinFullyQualifiedName);

            if ("一对多".equalsIgnoreCase(selection.getRelationType())) {
                data.put("isList", true);
            } else {
                data.put("isList", false);
            }
        }

        // 添加 isJoinClass 变量
        data.put("isJoinClass", classInfo.isJoinClass());

        return data;
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
     * 构建 SELECT 字段部分用于 MyBatis Template
     *
     * @param fields     表字段列表
     * @param tableAlias 表别名
     * @return 构建后的 SELECT 字段字符串
     */
    private String buildSelectFieldsForMyBatis(List<TableField> fields, String tableAlias) {
        return fields.stream()
                .map(field -> String.format("%s.%s AS %s_%s ",
                        tableAlias,
                        field.getColumnName(),
                        tableAlias,
                        field.getColumnName()))
                .collect(Collectors.joining(", \n"));
    }

    /**
     * 构建 JOIN 子句
     *
     * @param selection 用户选择的数据
     * @param joinAlias 表别名
     * @return 构建后的 JOIN 子句字符串
     */
    private String buildJoinClause(UserSelection selection, String joinAlias) {

        String joinType = "LEFT JOIN"; // 可根据 relationType 选择 JOIN 类型
        String joinCondition = selection.getJoinCondition();

        if (selection.getJoinTableAlias() != null && !selection.getJoinTableAlias().isEmpty()
                && !joinAlias.equals(selection.getJoinTable())) {
            return String.format("%s %s AS %s \nON %s",
                    joinType,
                    selection.getJoinTable(),
                    joinAlias,
                    joinCondition);
        } else {
            // 如果没有别名，直接使用表名
            return String.format("%s %s \nON %s",
                    joinType,
                    selection.getJoinTable(),
                    joinCondition);
        }
    }

    /**
     * 获取连接类的类型名。
     *
     * @param selection 用户选择的数据
     * @return 连接类的类型名
     */
    private String getJoinClassName(UserSelection selection) {
        if (selection.getGeneratedJoinClassInfo() != null) {
            return selection.getGeneratedJoinClassInfo().getClassName();
        } else if (selection.getSelectedJoinClassInfo() != null) {
            return selection.getSelectedJoinClassInfo().getClassName();
        } else {
            return "";
        }
    }

    /**
     * 获取连接类的字段名（类型名首字母小写）。
     *
     * @param selection 用户选择的数据
     * @return 连接类的字段名
     */
    private String getJoinObjectName(UserSelection selection) {
        if (selection.getJoinObjectName() != null && !selection.getJoinObjectName().isEmpty()) {
            return selection.getJoinObjectName();
        }

        String className = getJoinClassName(selection);
        if (className.isEmpty()) {
            return "";
        }
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * 获取连接类的全限定名。
     *
     * @param selection 用户选择的数据
     * @return 连接类的全限定名
     */
    private String getJoinFullyQualifiedName(UserSelection selection) {
        if (selection.getGeneratedJoinClassInfo() != null) {
            return selection.getGeneratedJoinClassInfo().getPackageName() + "." + selection.getGeneratedJoinClassInfo().getClassName();
        } else if (selection.getSelectedJoinClassInfo() != null) {
            return selection.getSelectedJoinClassInfo().getFullyQualifiedName();
        } else {
            return "";
        }
    }

}
