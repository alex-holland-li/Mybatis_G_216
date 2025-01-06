package com.example.mybatisjoingenerator.context;

import com.intellij.database.model.*;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;

import java.util.*;

/**
 * @author 李运
 */
public class DataBaseContext {
    // 数据库信息存储对象 Map<数据库名， Map<Schema名，Map<表名，Map<字段名，字段DasColumn>>>>
    public static final Map<String, Map<String, Map<String, Map<String, DasColumn>>>> DATABASE_MAP = new HashMap<>();

    /**
     * 添加数据库信息到上下文。
     *
     * @param databaseName 数据库名
     * @param schemaName   模式名
     * @param tableInfo    表信息
     */
    public static void addDatabaseInfo(String databaseName, String schemaName, Map<String, Map<String, DasColumn>> tableInfo) {
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
            Map<String, Map<String, Map<String, DasColumn>>> databaseInfo = new HashMap<>();
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
    private static Map<String, Map<String, DasColumn>> getTableInfo(DasNamespace dasNamespace) {
        Map<String, Map<String, DasColumn>> tableInfo = new HashMap<>();
        // 获取该命名空间下的所有子对象，过滤出表对象
        JBIterable<? extends DasObject> children = dasNamespace.getDasChildren(ObjectKind.TABLE);

        // 遍历所有子对象，检查它们是否是表类型
        for (DasObject child : children) {
            if (child instanceof DasTable table) {
                String tableName = table.getName();
                // 获取表的注释（如果有）
                String comment = table.getComment();
                Map<String, DasColumn> columnsInfo = new HashMap<>();
                // 获取表中的列
                JBIterable<? extends DasObject> columns = table.getDasChildren(ObjectKind.COLUMN);
                for (DasObject column : columns) {
                    if (column instanceof DasColumn dasColumn) {
                        columnsInfo.put(column.getName(), dasColumn);
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
            Map<String, Map<String, Map<String, DasColumn>>> schemas = DATABASE_MAP.get(databaseName);
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
        for (Map<String, Map<String, Map<String, DasColumn>>> schemas : DATABASE_MAP.values()) {
            for (Map<String, Map<String, DasColumn>> tables : schemas.values()) {
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
    public static Map<String, DasColumn> getTableColumns(String databaseName, String schemaName, String tableName) {
        if (DATABASE_MAP.containsKey(databaseName)) {
            Map<String, Map<String, Map<String, DasColumn>>> schemas = DATABASE_MAP.get(databaseName);
            if (schemas.containsKey(schemaName)) {
                Map<String, Map<String, DasColumn>> tables = schemas.get(schemaName);
                return tables.get(tableName);
            }
        }
        return null;
    }

    public static String getColumnType(String previousDatabaseSelection, String previousSchemaSelection, String previousMainTableSelection, String selectedField) {
        return DATABASE_MAP.get(previousDatabaseSelection).get(previousSchemaSelection).get(previousMainTableSelection).get(selectedField).getDataType().typeName;
    }

    public static DasColumn getColumn(String previousDatabaseSelection, String previousSchemaSelection, String previousMainTableSelection, String selectedField) {
        return DATABASE_MAP.get(previousDatabaseSelection).get(previousSchemaSelection).get(previousMainTableSelection).get(selectedField);
    }
}

