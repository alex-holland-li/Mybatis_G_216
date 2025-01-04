package com.example.mybatisjoingenerator.ui;

import com.example.mybatisjoingenerator.context.DataBaseContext;
import com.example.mybatisjoingenerator.generators.CodeGenerator;
import com.example.mybatisjoingenerator.models.TableField;
import com.example.mybatisjoingenerator.models.UserSelection;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JoinQueryPanel {
    // UI 组件
    public JComboBox<String> databaseComboBox;  // 数据库选择下拉框
    public JComboBox<String> schemaComboBox;    // 模式选择下拉框
    public JComboBox<String> mainTableComboBox; // 主表选择下拉框
    public JComboBox<String> joinTableComboBox; // 副表选择下拉框
    public JComboBox<String> relationTypeComboBox; // 关联关系选择下拉框
    public JComboBox<String> mainJoinFieldComboBox;  // 主表关联字段选择下拉框
    public JComboBox<String> joinJoinFieldComboBox;  // 副表关联字段选择下拉框
    // 主面板
    JPanel mainPanel;
    JButton generateButton;  // 生成代码按钮
    private Project project;
    private JTextField mainTableAliasField;  // 主表别名输入框
    private JTextField joinTableAliasField;  // 副表别名输入框

    private JComboBox<String> mainFieldSelectionComboBox;  // 主表字段选择下拉框
    private JButton addMainFieldButton;  // 添加主表字段按钮
    private JTable selectedMainFieldsTable;  // 已选择主表字段表格
    private SelectedFieldsTableModel selectedMainFieldsTableModel;  // 主表已选字段表格模型

    private JComboBox<String> joinFieldSelectionComboBox;  // 副表字段选择下拉框
    private JButton addJoinFieldButton;  // 添加副表字段按钮
    private JTable selectedJoinFieldsTable;  // 已选择副表字段表格
    private SelectedFieldsTableModel selectedJoinFieldsTableModel;  // 副表已选字段表格模型

    private JTextField packagePathTextField;  // 包路径输入框
    private JFileChooser packagePathChooser;  // 包路径输入框

    private JCheckBox autoGenerateJavaFileCheckBox;  // 是否自动生成Java文件的勾选框

    private JCheckBox createNewJavaObject;  // 是否自动生成Java文件的勾选框

    private JTextField fileNameTextField;  // 文件名输入框
    private JTextField joinObjectNameTextField;  // 副表对象命名输入框
    private JLabel joinObjectJLabel;

    private JPanel packagePathPanel;


    private String previousDatabaseSelection = null;
    private String previousSchemaSelection = null;
    private String previousMainTableSelection = null;
    private String previousJoinTableSelection = null;


    public JoinQueryPanel(Project project) {
        this.project = project;
        // 初始化组件
        initializeUiComponents();  // 进行 UI 组件初始化
        buildUI();  // 构建 UI
        addListeners();  // 为 UI 组件添加监听器
    }

    // 初始化 UI 组件
    private void initializeUiComponents() {
        // 初始化下拉框
        databaseComboBox = new SearchableComboBox();
        databaseComboBox.setModel(new DefaultComboBoxModel<>(getDatabaseNames()));

        //模式选择
        schemaComboBox = new SearchableComboBox();
        //主表选择
        mainTableComboBox = new SearchableComboBox();
        //副表选择
        joinTableComboBox = new SearchableComboBox();
        //关联关系
        relationTypeComboBox = new SearchableComboBox();
        relationTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"普通", "一对一", "一对多"}));
        //主表关联字段选择
        mainJoinFieldComboBox = new SearchableComboBox();
        //副表关联字段选择
        joinJoinFieldComboBox = new SearchableComboBox();

        // 初始化按钮
        generateButton = new JButton("生成代码");

        // 初始化文本框
        mainTableAliasField = new JTextField(10);  // 主表别名输入框
        joinTableAliasField = new JTextField(10);  // 副表别名输入框

        // 初始化字段选择下拉框
        mainFieldSelectionComboBox = new SearchableComboBox();
        joinFieldSelectionComboBox = new SearchableComboBox();

        // 初始化添加字段按钮
        addMainFieldButton = new JButton("添加主表字段");
        addJoinFieldButton = new JButton("添加副表字段");

        // 初始化已选字段表格
        selectedMainFieldsTableModel = new SelectedFieldsTableModel();
        selectedMainFieldsTable = new JTable(selectedMainFieldsTableModel);

        selectedJoinFieldsTableModel = new SelectedFieldsTableModel();
        selectedJoinFieldsTable = new JTable(selectedJoinFieldsTableModel);

        // 初始化文件路径选择器
        packagePathChooser = new JFileChooser();
        packagePathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  // 只选择文件夹
        packagePathChooser.setCurrentDirectory(new java.io.File(project.getBasePath() + "/src/main/java"));

        createNewJavaObject = new JCheckBox("生成新Java对象", false);


        // 初始化勾选框
        autoGenerateJavaFileCheckBox = new JCheckBox("自动保存Java对象源码文件", true); // 默认勾选

        // 文件名输入框
        fileNameTextField = new JTextField(20);
        fileNameTextField.setText("User");  // 默认文件名为 "User"

        // 副表对象命名文本框，默认隐藏
        joinObjectNameTextField = new JTextField(20);
        joinObjectNameTextField.setText("UserCouponRecords");  // 默认副表对象名
        joinObjectNameTextField.setVisible(false);  // 默认不显示

        joinObjectJLabel = new JLabel("副表对象命名:");
        joinObjectJLabel.setVisible(false);

    }

    // 构建UI界面
    private void buildUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // 设置主面板的布局为垂直方向

        // 数据库选择面板
        JPanel databasePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        databasePanel.add(new JLabel("数据库:"));
        databasePanel.add(databaseComboBox);
        mainPanel.add(databasePanel);

        // 模式选择面板
        JPanel schemaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schemaPanel.add(new JLabel("模式:"));
        schemaPanel.add(schemaComboBox);
        mainPanel.add(schemaPanel);

        // 主表选择和别名面板
        JPanel mainTablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainTablePanel.setBorder(BorderFactory.createTitledBorder("主表选择"));
        mainTablePanel.add(new JLabel("主表:"));
        mainTablePanel.add(mainTableComboBox);
        mainTablePanel.add(new JLabel("主表别名:"));
        mainTablePanel.add(mainTableAliasField);
        mainPanel.add(mainTablePanel);

        // 副表选择和别名面板
        JPanel joinTablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinTablePanel.setBorder(BorderFactory.createTitledBorder("副表选择"));
        joinTablePanel.add(new JLabel("副表:"));
        joinTablePanel.add(joinTableComboBox);
        joinTablePanel.add(new JLabel("副表别名:"));
        joinTablePanel.add(joinTableAliasField);
        mainPanel.add(joinTablePanel);

        // 关联关系选择面板
        JPanel relationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        relationPanel.setBorder(BorderFactory.createTitledBorder("关联关系"));
        relationPanel.add(new JLabel("关联关系:"));
        relationPanel.add(relationTypeComboBox);
        mainPanel.add(relationPanel);

        // 关联条件选择面板
        JPanel joinConditionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinConditionPanel.setBorder(BorderFactory.createTitledBorder("关联条件"));
        joinConditionPanel.add(new JLabel("主表字段:"));
        joinConditionPanel.add(mainJoinFieldComboBox);
        joinConditionPanel.add(new JLabel("副表字段:"));
        joinConditionPanel.add(joinJoinFieldComboBox);
        mainPanel.add(joinConditionPanel);

        // 主表字段选择面板
        JPanel mainFieldsSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainFieldsSelectionPanel.setBorder(BorderFactory.createTitledBorder("主表字段选择"));
        mainFieldsSelectionPanel.add(new JLabel("选择主表字段:"));
        mainFieldsSelectionPanel.add(mainFieldSelectionComboBox);
        mainFieldsSelectionPanel.add(addMainFieldButton);
        mainPanel.add(mainFieldsSelectionPanel);

        // 已选主表字段表格面板
        JPanel selectedMainFieldsPanel = new JPanel(new BorderLayout());
        selectedMainFieldsPanel.setBorder(BorderFactory.createTitledBorder("已选主表字段"));
        JScrollPane mainFieldsScrollPane = new JScrollPane(selectedMainFieldsTable);
        mainFieldsScrollPane.setPreferredSize(new Dimension(800, 100));
        selectedMainFieldsPanel.add(mainFieldsScrollPane, BorderLayout.CENTER);
        mainPanel.add(selectedMainFieldsPanel);

        // 副表字段选择面板
        JPanel joinFieldsSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinFieldsSelectionPanel.setBorder(BorderFactory.createTitledBorder("副表字段选择"));
        joinFieldsSelectionPanel.add(new JLabel("选择副表字段:"));
        joinFieldsSelectionPanel.add(joinFieldSelectionComboBox);
        joinFieldsSelectionPanel.add(addJoinFieldButton);
        mainPanel.add(joinFieldsSelectionPanel);

        // 已选副表字段表格面板
        JPanel selectedJoinFieldsPanel = new JPanel(new BorderLayout());
        selectedJoinFieldsPanel.setBorder(BorderFactory.createTitledBorder("已选副表字段"));
        JScrollPane joinFieldsScrollPane = new JScrollPane(selectedJoinFieldsTable);
        joinFieldsScrollPane.setPreferredSize(new Dimension(800, 100));
        selectedJoinFieldsPanel.add(joinFieldsScrollPane, BorderLayout.CENTER);
        mainPanel.add(selectedJoinFieldsPanel);

        // 添加是否自动生成文件的勾选框
        JPanel autoGeneratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoGeneratePanel.add(autoGenerateJavaFileCheckBox);
        mainPanel.add(autoGeneratePanel);

        // 添加包路径选择按钮
        packagePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        packagePathPanel.add(new JLabel("选择包路径:"));
        JButton choosePathButton = new JButton("选择路径");

        choosePathButton.addActionListener(e -> {
            int returnValue = packagePathChooser.showOpenDialog(null);  // 弹出文件夹选择对话框
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // 获取选择的路径
                java.io.File selectedFolder = packagePathChooser.getSelectedFile();
                // 更新路径文本框
                packagePathTextField.setText(selectedFolder.getAbsolutePath());
            }
        });

        packagePathTextField = new JTextField(20);
        packagePathTextField.setText(packagePathChooser.getCurrentDirectory().getAbsolutePath());  // 默认显示当前路径

        packagePathPanel.add(packagePathTextField);
        packagePathPanel.add(choosePathButton);
        mainPanel.add(packagePathPanel);

        // 添加文件名输入框
        JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileNamePanel.add(new JLabel("根对象名:"));
        fileNameTextField = new JTextField(20);
        fileNameTextField.setText("");  // 默认文件名
        fileNamePanel.add(fileNameTextField);
        mainPanel.add(fileNamePanel);

        // 添加副表对象命名文本框，仅在选择一对一或一对多时显示
        JPanel joinObjectNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinObjectNamePanel.add(joinObjectJLabel);
        joinObjectNamePanel.add(joinObjectNameTextField);  // 添加副表对象命名框
        mainPanel.add(joinObjectNamePanel);

        // 生成按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(generateButton);
        mainPanel.add(buttonPanel);
    }

    private String[] getDatabaseNames() {
        Set<String> databaseNames = DataBaseContext.getAllDatabaseNames();
        return databaseNames.toArray(new String[0]);
    }

    // 添加事件监听器
    private void addListeners() {
        // 监听数据库选择，判断数据库是否发生变化，若有变化则重置相关字段
        databaseComboBox.addActionListener(event -> {
            String selectedDatabase = (String) databaseComboBox.getSelectedItem();
            if (!StringUtils.isEmpty(selectedDatabase) && !selectedDatabase.equals(previousDatabaseSelection)) {
                //修改数据库连接
                resetOnDatabaseChange(selectedDatabase);
            }
        });

        // 监听模式选择，判断模式是否发生变化，若有变化则重置相关字段
        schemaComboBox.addActionListener(event -> {
            String selectedSchema = (String) schemaComboBox.getSelectedItem();
            if (!StringUtils.isEmpty(selectedSchema) && !selectedSchema.equals(previousSchemaSelection)) {
                //修改schema
                resetOnSchemaChange(selectedSchema);
            }
        });

        // 监听主表选择，判断主表是否发生变化，若有变化则重置相关字段
        mainTableComboBox.addActionListener(event -> {
            String selectedMainTable = (String) mainTableComboBox.getSelectedItem();
            if (!StringUtils.isEmpty(selectedMainTable) && !selectedMainTable.equals(previousMainTableSelection)) {
                //修改主表选择
                resetOnMainTableChange(selectedMainTable);
            }
        });

        // 监听副表选择，判断副表是否发生变化，若有变化则重置相关字段
        joinTableComboBox.addActionListener(event -> {
            String selectedJoinTable = (String) joinTableComboBox.getSelectedItem();
            if (!StringUtils.isEmpty(selectedJoinTable) && !selectedJoinTable.equals(previousJoinTableSelection)) {
                //修改副表选择
                resetOnJoinTableChange(selectedJoinTable);
            }
        });
        // 监听添加主表字段按钮点击事件
        addMainFieldButton.addActionListener(event -> addSelectedMainField());

        // 监听添加副表字段按钮点击事件
        addJoinFieldButton.addActionListener(event -> addSelectedJoinField());
        // 为已选字段表格添加右键菜单
        addTableDeleteFunctionality(selectedMainFieldsTable, selectedMainFieldsTableModel);
        addTableDeleteFunctionality(selectedJoinFieldsTable, selectedJoinFieldsTableModel);

        // 点击生成按钮，生成代码并展示
        generateButton.addActionListener(e -> {
            // 获取用户选择的数据
            UserSelection selection = getUserSelection();

            // 检查用户是否做出了有效选择
            if (selection == null) {
                return;
            }

            // 生成代码
            CodeGenerator generator = new CodeGenerator();
            String sqlCode = generator.generateSelectQuery(selection);
            String resultMapCode = generator.generateResultMap(selection);
            String mapperCode = generator.generateMapperInterface(selection);
            String javaClassesCode = generator.generateJavaClasses(selection);

            // 获取包路径和文件名（如果勾选了自动生成）
            String packagePath = packagePathTextField.getText();
            String fileName = fileNameTextField.getText() + ".java";  // 默认添加 .java 后缀
            boolean autoGenerate = autoGenerateJavaFileCheckBox.isSelected();


            // 如果勾选了自动生成，保存文件到指定路径
            if (autoGenerate) {
                generator.saveJavaClasses(packagePath, fileName, javaClassesCode);
            }

            // 创建并显示 CodeDisplayFrame
            CodeDisplayFrame codeDisplayFrame = new CodeDisplayFrame(sqlCode, resultMapCode, mapperCode, javaClassesCode);
            codeDisplayFrame.setVisible(true);
        });


        // 监听关联关系选择变化，控制副表对象命名框的显示与隐藏
        relationTypeComboBox.addActionListener(event -> {
            String selectedRelationType = (String) relationTypeComboBox.getSelectedItem();
            if ("一对一".equalsIgnoreCase(selectedRelationType) || "一对多".equalsIgnoreCase(selectedRelationType)) {
                joinObjectNameTextField.setVisible(true);  // 显示副表对象命名框
                joinObjectJLabel.setVisible(true);
            } else {
                joinObjectJLabel.setVisible(false);
                joinObjectNameTextField.setVisible(false);  // 隐藏副表对象命名框
            }
        });

        // 监听勾选框的选中状态
        autoGenerateJavaFileCheckBox.addItemListener(e -> {
            boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;  // 判断勾选框是否被选中
            if (isSelected) {
                // 用户选择生成 Java 文件，显示文件路径选择器
                packagePathPanel.setVisible(true);
            } else {
                // 用户取消选择，不生成文件，禁用路径选择器
                packagePathPanel.setVisible(false);
            }
        });
    }

    private void saveJavaClassesToFile(String javaClassesCode, String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            Messages.showErrorDialog("请指定包路径！", "错误");
            return;
        }

        try {
            String filePath = packageName.replace(".", "/") + "/GeneratedJavaClass.java";
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(javaClassesCode);
            }

            Messages.showInfoMessage("Java类文件已保存至：" + file.getAbsolutePath(), "保存成功");

        } catch (IOException ex) {
            Messages.showErrorDialog("保存文件时出错：" + ex.getMessage(), "错误");
        }
    }

    private String getPackageNameFromUser() {
        return packagePathTextField.getText().trim();  // 获取用户输入的包路径
    }


    // 为表格添加右键菜单功能
    private void addTableDeleteFunctionality(JTable table, SelectedFieldsTableModel model) {
        table.setComponentPopupMenu(createTablePopupMenu(table, model));
    }

    // 创建右键菜单
    private JPopupMenu createTablePopupMenu(JTable table, SelectedFieldsTableModel model) {
        JPopupMenu popupMenu = new JPopupMenu();

        // 创建删除菜单项
        JMenuItem deleteItem = new JMenuItem("删除选中项");

        // 为删除菜单项添加事件监听器
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();  // 获取选中的行
            if (selectedRow != -1) {  // 如果有选中的行
                model.removeRow(selectedRow);  // 从表格模型中移除该行
            }
        });

        popupMenu.add(deleteItem);  // 将删除菜单项添加到右键菜单
        return popupMenu;  // 返回右键菜单
    }

    // 主表选择字段
    private void addSelectedMainField() {
        String selectedField = (String) mainFieldSelectionComboBox.getSelectedItem();
        if (selectedField != null && !selectedField.isEmpty()) {
            // 检查是否已添加
            if (!selectedMainFieldsTableModel.containsField(selectedField)) {
                // 将字段添加到表格模型中
                String dataType = DataBaseContext.getColumnType(previousDatabaseSelection, previousSchemaSelection, previousMainTableSelection, selectedField);
                String javaType = mapDataTypeToJavaType(dataType);
                selectedMainFieldsTableModel.addRow(new SelectedField(selectedField, toCamelCase(selectedField), javaType));
            } else {
                Messages.showWarningDialog("该字段已添加。", "警告");
            }
        }
    }

    // 副表选择字段
    private void addSelectedJoinField() {
        String selectedField = (String) joinFieldSelectionComboBox.getSelectedItem();
        if (!StringUtils.isEmpty(selectedField)) {
            // 检查是否已添加
            if (!selectedJoinFieldsTableModel.containsField(selectedField)) {
                // 将字段添加到表格模型中
                String dataType = DataBaseContext.getColumnType(previousDatabaseSelection, previousSchemaSelection, previousJoinTableSelection, selectedField);
                String javaType = mapDataTypeToJavaType(dataType);
                selectedJoinFieldsTableModel.addRow(new SelectedField(selectedField, toCamelCase(selectedField), javaType));
            } else {
                Messages.showWarningDialog("该字段已添加。", "警告");
            }
        }
    }

    // 修改选择数据库
    private void resetOnDatabaseChange(String selectedDatabase) {
        // 重置所有与数据库相关的字段
        //模式列表
        Set<String> schemaNames = DataBaseContext.getAllSchemaNames(selectedDatabase);
        schemaComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));

        //主表列表
        mainTableComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //副表列表
        joinTableComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //主表关联字段列表
        mainJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //副表关联字段列表
        joinJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //主表字段选择列表
        mainFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //副表字段选择列表
        joinFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //已选择主表字段
        selectedMainFieldsTableModel.clear();
        //已选择副表字段
        selectedJoinFieldsTableModel.clear();

        // 清空别名输入框
        mainTableAliasField.setText("");
        joinTableAliasField.setText("");

        previousSchemaSelection = null;
        previousMainTableSelection = null;
        previousJoinTableSelection = null;
        previousDatabaseSelection = selectedDatabase;
    }

    // 修改Schema
    private void resetOnSchemaChange(String selectedSchema) {
        //主表列表
        Set<String> schemaNames = DataBaseContext.getAllTableNames(previousDatabaseSelection, selectedSchema);
        mainTableComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));

        //副表列表
        joinTableComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));

        //主表关联字段列表
        mainJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //副表关联字段列表
        joinJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //主表字段选择列表
        mainFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //副表字段选择列表
        joinFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(new String[]{}));

        //已选择主表字段
        selectedMainFieldsTableModel.clear();
        //已选择副表字段
        selectedJoinFieldsTableModel.clear();

        // 清空别名输入框
        mainTableAliasField.setText("");
        joinTableAliasField.setText("");

        previousMainTableSelection = null;
        previousJoinTableSelection = null;
        previousSchemaSelection = selectedSchema;
    }

    // 修改主表发生变化时，重置所有相关字段
    private void resetOnMainTableChange(String selectedMainTable) {
        Set<String> schemaNames = DataBaseContext.getTableColumns(previousDatabaseSelection, previousSchemaSelection, selectedMainTable).keySet();

        //主表关联字段列表
        mainJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));

        //主表字段选择列表
        mainFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));

        //已选择主表字段
        selectedMainFieldsTableModel.clear();

        // 清空主表别名输入框
        mainTableAliasField.setText(selectedMainTable);

        previousMainTableSelection = selectedMainTable;
    }

    // 当选择的副表发生变化时，重置所有相关字段
    private void resetOnJoinTableChange(String selectedJoinTable) {
        Set<String> schemaNames = DataBaseContext.getTableColumns(previousDatabaseSelection, previousSchemaSelection, selectedJoinTable).keySet();
        joinJoinFieldComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));
        joinFieldSelectionComboBox.setModel(new DefaultComboBoxModel<>(schemaNames.toArray(new String[0])));
        selectedJoinFieldsTableModel.clear();
        joinTableAliasField.setText(selectedJoinTable);
        previousJoinTableSelection = selectedJoinTable;
    }

    // 将蛇形命名转为驼峰命名
    private String toCamelCase(String str) {
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

    // 将数据库类型映射为 Java 类型
    private String mapDataTypeToJavaType(String sqlType) {
        if (sqlType == null) {
            return "String";
        }
        sqlType = sqlType.toLowerCase().trim();

        // 处理 'tinyint(1)' 为 Boolean
        if (sqlType.startsWith("tinyint") && sqlType.contains("(1)")) {
            return "Boolean";
        }

        // 去掉 'unsigned' 和长度规范（例如： "bigint unsigned" -> "bigint"）
        String baseType = sqlType.replaceAll("\\s+unsigned", "").split("\\(")[0].trim();

        switch (baseType) {
            // 整型
            case "tinyint":
                return "Byte";
            case "smallint":
                return "Short";
            case "mediumint":
                return "Integer";
            case "int":
            case "integer":
                return "Integer";
            case "bigint":
                return "Long";

            // 浮点型
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "decimal":
            case "numeric":
                return "BigDecimal";

            // 日期和时间类型
            case "date":
                return "LocalDate";
            case "datetime":
                return "LocalDateTime";
            case "timestamp":
                return "Long"; // 或者 "LocalDateTime" 根据需求
            case "time":
                return "LocalTime";
            case "year":
                return "Integer"; // 或者使用 "Year" 类型

            // 字符串类型
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
                return "String";
            case "binary":
            case "varbinary":
            case "tinyblob":
            case "blob":
            case "mediumblob":
            case "longblob":
                return "byte[]";

            // ENUM 和 SET
            case "enum":
            case "set":
                return "String"; // 或者使用自定义的 Enum 类型

            // JSON 类型
            case "json":
                return "String"; // 或者使用 "JsonNode" 需要 Jackson 依赖

            // 空间类型
            case "geometry":
            case "point":
            case "linestring":
            case "polygon":
            case "multipoint":
            case "multilinestring":
            case "multipolygon":
            case "geometrycollection":
                return "String"; // 或者使用专门的空间数据类型

            default:
                return "String";
        }
    }

    // 返回主面板
    public JPanel getMainPanel() {
        return mainPanel;
    }

    private UserSelection getUserSelection() {
        // 获取数据库、模式、主表和副表的选择
        String selectedDatabase = (String) databaseComboBox.getSelectedItem();
        String selectedSchema = (String) schemaComboBox.getSelectedItem();
        String mainTable = (String) mainTableComboBox.getSelectedItem();
        String joinTable = (String) joinTableComboBox.getSelectedItem();
        String relationType = (String) relationTypeComboBox.getSelectedItem();

        // 获取主表和副表的别名
        String mainTableAlias = mainTableAliasField.getText().trim();
        String joinTableAlias = joinTableAliasField.getText().trim();

        // 确保主表别名和副表别名不重复
        if (!mainTableAlias.isEmpty() && mainTableAlias.equals(joinTableAlias)) {
            Messages.showErrorDialog("主表别名和副表别名必须唯一。", "错误");
            return null;
        }

        // 获取关联条件
        String joinCondition = "";
        if (mainTable != null && joinTable != null) {
            String mainJoinField = (String) mainJoinFieldComboBox.getSelectedItem();
            String joinJoinField = (String) joinJoinFieldComboBox.getSelectedItem();
            if (mainJoinField != null && joinJoinField != null) {
                joinCondition = mainTableAlias + "." + mainJoinField + " = " + joinTableAlias + "." + joinJoinField;
            }
        }

        // 获取已选主表字段和副表字段
        List<TableField> selectedMainFields = selectedMainFieldsTableModel.getSelectedFields();
        List<TableField> selectedJoinFields = selectedJoinFieldsTableModel.getSelectedFields();

        // 检查是否有选中的字段
        if (selectedMainFields.isEmpty() || selectedJoinFields.isEmpty()) {
            Messages.showErrorDialog("请确保选择了主表和副表的字段。", "错误");
            return null;
        }

        // 检查是否选择了关联条件
        if (joinCondition.isEmpty()) {
            Messages.showErrorDialog("请确保指定了关联条件。", "错误");
            return null;
        }

        // 检查是否有数据库、模式、主表、副表的选择
        if (selectedDatabase == null || selectedSchema == null || mainTable == null || joinTable == null) {
            Messages.showErrorDialog("请确保选择了数据库、模式、主表和副表。", "错误");
            return null;
        }

        // 获取根对象名、副表对象名、文件路径和是否自动保存文件
        String rootObjectName = fileNameTextField.getText().trim();
        String joinObjectName = joinObjectNameTextField.getText().trim();
        String filePath = fileNameTextField.getText().trim();
        boolean saveJavaToFile = autoGenerateJavaFileCheckBox.isSelected();

        // 返回封装了用户选择的 UserSelection 对象
        return new UserSelection(
                selectedDatabase,
                selectedSchema,
                mainTable,
                mainTableAlias.isEmpty() ? mainTable : mainTableAlias, // 使用别名或主表名
                joinTable,
                joinTableAlias.isEmpty() ? joinTable : joinTableAlias, // 使用别名或副表名
                joinCondition,
                selectedMainFields,
                selectedJoinFields,
                relationType,
                rootObjectName,   // 新增根对象名
                joinObjectName,   // 新增副表对象名
                filePath,         // 新增文件路径
                saveJavaToFile    // 是否自动生成 Java 文件
        );
    }


    // SelectedField 类：用于存储字段的数据库名和 Java 字段名
    class SelectedField {
        private String databaseFieldName;
        private String javaFieldName;

        private String javaType;

        public SelectedField(String databaseFieldName, String javaFieldName, String javaType) {
            this.databaseFieldName = databaseFieldName;
            this.javaFieldName = javaFieldName;
            this.javaType = javaType;
        }

        public String getDatabaseFieldName() {
            return databaseFieldName;
        }

        public void setDatabaseFieldName(String databaseFieldName) {
            this.databaseFieldName = databaseFieldName;
        }

        public String getJavaFieldName() {
            return javaFieldName;
        }

        public void setJavaFieldName(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String getJavaType() {
            return javaType;
        }

        public void setJavaType(String javaType) {
            this.javaType = javaType;
        }
    }

    // SelectedFieldsTableModel 类：自定义表格模型，用于已选字段表格
    class SelectedFieldsTableModel extends AbstractTableModel {
        private final String[] columnNames = {"数据库字段名", "Java 字段名"};  // 表格列标题
        private final List<SelectedField> data = new ArrayList<>();  // 用于存储已选择字段的数据

        @Override
        public int getRowCount() {
            return data.size();  // 返回表格的行数
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;  // 返回表格的列数
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];  // 返回列标题
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SelectedField field = data.get(rowIndex);  // 获取指定行的数据
            switch (columnIndex) {
                case 0:  // 数据库字段名列
                    return field.getDatabaseFieldName();
                case 1:  // Java 字段名列
                    return field.getJavaFieldName();
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;  // 只允许编辑 Java 字段名列
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                data.get(rowIndex).setJavaFieldName((String) aValue);  // 更新 Java 字段名
                fireTableCellUpdated(rowIndex, columnIndex);  // 刷新表格
            }
        }

        // 添加一个字段到表格中
        public void addRow(SelectedField field) {
            data.add(field);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);  // 刷新表格
        }

        // 删除指定行的数据
        public void removeRow(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < data.size()) {
                data.remove(rowIndex);
                fireTableRowsDeleted(rowIndex, rowIndex);  // 刷新表格
            }
        }

        // 清空所有选中的字段
        public void clear() {
            int size = data.size();
            if (size > 0) {
                data.clear();
                fireTableRowsDeleted(0, size - 1);  // 刷新表格
            }
        }

        // 判断字段是否已存在
        public boolean containsField(String fieldName) {
            for (SelectedField field : data) {
                if (field.getDatabaseFieldName().equals(fieldName)) {
                    return true;
                }
            }
            return false;
        }

        // 获取所有已选字段对象
        public List<TableField> getSelectedFields() {
            List<TableField> fields = new ArrayList<>();
            for (SelectedField field : data) {
                fields.add(new TableField("", "", field.getDatabaseFieldName(), field.getJavaFieldName(), field.getJavaType(), false));
            }
            return fields;
        }
    }
}
