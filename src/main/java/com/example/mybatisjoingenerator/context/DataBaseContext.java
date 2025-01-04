package com.example.mybatisjoingenerator.context;

import com.intellij.database.model.*;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;

import java.util.*;

public class DataBaseContext {
    // 数据库信息存储对象 Map<数据库名， Map<Schema名，Map<表名，Map<字段名，字段类型>>>>
    public static final Map<String, Map<String, Map<String, Map<String, String>>>> DATABASE_MAP = new HashMap<>();

    /**
     * 添加数据库信息到上下文。
     *
     * @param databaseName 数据库名
     * @param schemaName   模式名
     * @param tableInfo    表信息
     */
    public static void addDatabaseInfo(String databaseName, String schemaName, Map<String, Map<String, String>> tableInfo) {
        DATABASE_MAP
                .computeIfAbsent(databaseName, k -> new HashMap<>())
                .put(schemaName, tableInfo);
    }

    /**
     * 初始化数据库信息。
     *
     * @param dataSources 数据源集合
     */
    public static void init(Collection<DbDataSource> dataSources) {
        // 获取数据库信息并存储到上下文
        for (DbDataSource dataSource : dataSources) {
            String databaseName = dataSource.getName();

            // 获取该数据源的所有 schemas
            JBIterable<? extends DasNamespace> schemas = DasUtil.getSchemas(dataSource);
            Map<String, Map<String, Map<String, String>>> databaseInfo = new HashMap<>();
            // 遍历每个 schema
            for (DasNamespace schema : schemas) {
                String schemaName = schema.getName();
                databaseInfo.put(schemaName, getTableInfo(schema));
            }
            DATABASE_MAP.put(databaseName, databaseInfo);
        }

        // 打印数据库信息用于调试
    }

    /**
     * 获取表信息。
     *
     * @param dasNamespace 命名空间
     * @return 表信息映射
     */
    private static Map<String, Map<String, String>> getTableInfo(DasNamespace dasNamespace) {
        Map<String, Map<String, String>> tableInfo = new HashMap<>();
        // 获取该命名空间下的所有子对象，过滤出表对象
        JBIterable<? extends DasObject> children = dasNamespace.getDasChildren(ObjectKind.TABLE);

        // 遍历所有子对象，检查它们是否是表类型
        for (DasObject child : children) {
            if (child instanceof DasTable) {
                DasTable table = (DasTable) child;
                String tableName = table.getName();
                // 获取表的注释（如果有）
                String comment = table.getComment();
                System.out.println("Table: " + tableName + " - Comment: " + comment);

                Map<String, String> columnsInfo = new HashMap<>();
                // 获取表中的列
                JBIterable<? extends DasObject> columns = table.getDasChildren(ObjectKind.COLUMN);
                for (DasObject column : columns) {
                    if (column instanceof DasColumn dasColumn) {
                        System.out.println("  Column: " + dasColumn.getName());
                        columnsInfo.put(column.getName(), dasColumn.getDataType().typeName);
                    }
                }
                tableInfo.put(child.getName(), columnsInfo);
            }
        }
        return tableInfo;
    }

    /**
     * 获取所有数据库名称。
     *
     * @return 数据库名称集合
     */
    public static Set<String> getAllDatabaseNames() {
        return DATABASE_MAP.keySet();
    }

    /**
     * 获取指定数据库的所有模式名称。
     *
     * @param databaseName 数据库名
     * @return 模式名称集合
     */
    public static Set<String> getAllSchemaNames(String databaseName) {
        if (DATABASE_MAP.containsKey(databaseName)) {
            return DATABASE_MAP.get(databaseName).keySet();
        }
        return new HashSet<>();
    }

    /**
     * 获取指定数据库和模式下的所有表名。
     *
     * @param databaseName 数据库名
     * @param schemaName   模式名
     * @return 表名集合
     */
    public static Set<String> getAllTableNames(String databaseName, String schemaName) {
        if (DATABASE_MAP.containsKey(databaseName)) {
            Map<String, Map<String, Map<String, String>>> schemas = DATABASE_MAP.get(databaseName);
            if (schemas.containsKey(schemaName)) {
                return schemas.get(schemaName).keySet();
            }
        }
        return new HashSet<>();
    }

    /**
     * 获取所有表名（忽略数据库和模式）。
     *
     * @return 表名集合
     */
    public static Set<String> getAllTableNames() {
        Set<String> tableNames = new HashSet<>();
        for (Map<String, Map<String, Map<String, String>>> schemas : DATABASE_MAP.values()) {
            for (Map<String, Map<String, String>> tables : schemas.values()) {
                tableNames.addAll(tables.keySet());
            }
        }
        return tableNames;
    }

    /**
     * 获取指定数据库、模式和表的字段信息。
     *
     * @param databaseName 数据库名
     * @param schemaName   模式名
     * @param tableName    表名
     * @return 字段信息映射，如果不存在则返回null
     */
    public static Map<String, String> getTableColumns(String databaseName, String schemaName, String tableName) {
        if (DATABASE_MAP.containsKey(databaseName)) {
            Map<String, Map<String, Map<String, String>>> schemas = DATABASE_MAP.get(databaseName);
            if (schemas.containsKey(schemaName)) {
                Map<String, Map<String, String>> tables = schemas.get(schemaName);
                return tables.get(tableName);
            }
        }
        return null;
    }

    public static String getColumnType(String previousDatabaseSelection, String previousSchemaSelection, String previousMainTableSelection, String selectedField) {
        return DATABASE_MAP.get(previousDatabaseSelection).get(previousSchemaSelection).get(previousMainTableSelection).get(selectedField);
    }
}

