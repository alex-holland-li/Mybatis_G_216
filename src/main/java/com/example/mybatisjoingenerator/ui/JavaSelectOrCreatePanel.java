package com.example.mybatisjoingenerator.ui;

import com.example.mybatisjoingenerator.models.UserSelection;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * JavaSelectOrCreatePanel 处理用户关于生成类的选择
 */
public class JavaSelectOrCreatePanel extends JPanel {
    JLabel resultObject = new JLabel("结果类：");
    JLabel relationType = new JLabel("关联关系：");
    // 选择对象或生成对象单选框
    JCheckBox create = new JCheckBox("生成新类");
    JCheckBox search = new JCheckBox("选择现有");
    JCheckBox createMainSearchJoin = new JCheckBox("主对象生成，被连接对象选择");
    JCheckBox createSwagger = new JCheckBox("生成Swagger");
    JCheckBox createValidator = new JCheckBox("生成参数校验注解");
    JCheckBox createLombok = new JCheckBox("生成Lombok注解");
    private SearchableComboBox relationTypeComboBox;
    // 添加 ItemListener 来确保每次只有一个复选框被选中
    private ClassSearchPanel mainClassSearchPanel;
    private ClassSearchPanel joinClassSearchPanel;
    private ClassSearchPanel classSearchPanel;

    private CreateClassPanel mainCreateClassPanel;
    private CreateClassPanel joinCreateClassPanel;
    private CreateClassPanel createClassPanel;

    private JPanel kuozhan;

    private Project project;
    private String title;

    private String previousRelationTypeSelection = "";

    // 2:选择对象、1：生成对象、3：生成对象+选择对象
    private int selectedIndex = 0;

    // 新增的 UI 组件
    private JTextField rootObjectNameField; // 根对象名输入框
    private JTextField joinObjectNameField; // 链接对象名输入框
    private JTextField filePathField;       // 文件保存路径输入框
    private JCheckBox saveJavaToFileCheckBox; // 是否保存 Java 文件复选框

    private JLabel rootObjectNameLabel = new JLabel("根对象名:");
    private JLabel joinObjectNameLabel = new JLabel("链接对象名:");
    private JLabel filePathLabel = new JLabel("文件保存路径:");

    public JavaSelectOrCreatePanel(Project project, String title) {
        this.project = project;
        this.title = title;

        init();
        buildUi();
        listener();
    }

    private void buildUi() {

        // 设置布局管理器
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 设置边框
        setBorder(BorderFactory.createTitledBorder(title));

        // 基础面板：结果类标签和生成类型复选框
        JPanel base = new JPanel(new FlowLayout(FlowLayout.LEFT));
        base.add(resultObject);
        base.add(search);
        base.add(create);
        base.add(createMainSearchJoin);
        add(base);

        // 关联关系选择面板
        JPanel relationTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        relationTypePanel.add(relationType);
        relationTypePanel.add(relationTypeComboBox);
        add(relationTypePanel);

        // 创建面板
        JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createPanel.add(createClassPanel);
        add(createPanel);

        // 主对象创建面板
        JPanel mainCreatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainCreatePanel.add(mainCreateClassPanel);
        add(mainCreatePanel);

        // 连接对象创建面板
        JPanel joinCreatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinCreatePanel.add(joinCreateClassPanel);
        add(joinCreatePanel);

        // 对象选择面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(classSearchPanel);
        add(searchPanel);

        // 主对象选择面板
        JPanel mainClassSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainClassSearch.add(mainClassSearchPanel);
        add(mainClassSearch);

        // 连接对象选择面板
        JPanel joinClassSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        joinClassSearch.add(joinClassSearchPanel);
        add(joinClassSearch);

        // 扩展选项面板（Lombok, Swagger, Validator）
        kuozhan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kuozhan.add(createLombok);
        kuozhan.add(createSwagger);
        kuozhan.add(createValidator);
        kuozhan.setVisible(false);
        add(kuozhan);

        // 新增的对象名和文件路径输入面板
        addObjectNameAndFilePathPanels();
    }

    private void addObjectNameAndFilePathPanels() {
        // 根对象名和链接对象名面板
        JPanel objectNamesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        objectNamesPanel.add(rootObjectNameLabel);
        rootObjectNameField = new JTextField(15);
        objectNamesPanel.add(rootObjectNameField);
        objectNamesPanel.add(joinObjectNameLabel);
        joinObjectNameField = new JTextField(15);
        objectNamesPanel.add(joinObjectNameField);
        add(objectNamesPanel);

        // 文件保存路径和保存选项面板
        JPanel filePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePathPanel.add(filePathLabel);
        filePathField = new JTextField(30);
        filePathPanel.add(filePathField);
        saveJavaToFileCheckBox = new JCheckBox("自动保存 Java 文件");
        filePathPanel.add(saveJavaToFileCheckBox);
        add(filePathPanel);
    }

    private void init() {
        // 关联关系
        relationTypeComboBox = new SearchableComboBox();

        createClassPanel = new CreateClassPanel(project, "对象生成");
        mainCreateClassPanel = new CreateClassPanel(project, "主对象生成");
        joinCreateClassPanel = new CreateClassPanel(project, "连接对象生成");

        classSearchPanel = new ClassSearchPanel(project, "对象选择");
        mainClassSearchPanel = new ClassSearchPanel(project, "主对象选择");
        joinClassSearchPanel = new ClassSearchPanel(project, "连接对象选择");

        mainCreateClassPanel.setVisible(false);
        joinCreateClassPanel.setVisible(false);
        classSearchPanel.setVisible(false);
        mainClassSearchPanel.setVisible(false);
        joinClassSearchPanel.setVisible(false);
        createClassPanel.setVisible(false);
    }

    private void listener() {
        ItemListener itemListener = e -> {
            // 如果该复选框被选中
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // 取消其他复选框的选中状态
                if (e.getSource() == create) {
                    selectedIndex = 1;
                    search.setSelected(false);
                    createMainSearchJoin.setSelected(false);

                    kuozhan.setVisible(true);
                    createClassPanel.setVisible(false);
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);

                    relationTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"普通", "一对一", "一对多"}));
                } else if (e.getSource() == search) {
                    selectedIndex = 2;
                    create.setSelected(false);
                    createMainSearchJoin.setSelected(false);

                    kuozhan.setVisible(false);
                    createClassPanel.setVisible(false);
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);

                    relationTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"普通", "一对一", "一对多"}));
                } else if (e.getSource() == createMainSearchJoin) {
                    selectedIndex = 3;
                    create.setSelected(false);
                    search.setSelected(false);

                    kuozhan.setVisible(true);
                    createClassPanel.setVisible(false);
                    mainCreateClassPanel.setVisible(true);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(true);

                    relationTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"一对一", "一对多"}));
                } else {
                    // 其他情况恢复选中状态
                    if (selectedIndex == 1) {
                        create.setSelected(true);
                    } else if (selectedIndex == 2) {
                        search.setSelected(true);
                    } else if (selectedIndex == 3) {
                        createMainSearchJoin.setSelected(true);
                    }
                }
            }
        };

        // 为每个 JCheckBox 添加 ItemListener
        create.addItemListener(itemListener);
        search.addItemListener(itemListener);
        createMainSearchJoin.addItemListener(itemListener);

        relationTypeComboBox.addActionListener(event -> {
            String selectedRelationType = (String) relationTypeComboBox.getSelectedItem();
            if (!StringUtils.isEmpty(selectedRelationType) && !selectedRelationType.equals(previousRelationTypeSelection)) {
                // 修改 relationType
                relationTypeChange(selectedRelationType);
            }
        });
    }

    // 关联关系改变
    private void relationTypeChange(String selectedRelationType) {
        previousRelationTypeSelection = selectedRelationType;
        switch (selectedIndex) {
            // create
            case 1:
                if ("普通".equalsIgnoreCase(selectedRelationType)) {
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);
                    createClassPanel.setVisible(true);
                } else {
                    mainCreateClassPanel.setVisible(true);
                    joinCreateClassPanel.setVisible(true);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);
                    createClassPanel.setVisible(false);
                }
                break;
            // search
            case 2:
                if ("普通".equalsIgnoreCase(selectedRelationType)) {
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(true);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);
                    createClassPanel.setVisible(false);
                } else {
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(true);
                    joinClassSearchPanel.setVisible(true);
                    createClassPanel.setVisible(false);
                }
                break;
            // createMainSearchJoin
            case 3:
                // 对于 case 3，根据 relationType 可能需要更多逻辑
                // 目前不需要特殊处理
                break;
            default:
                break;
        }
    }

    /**
     * 获取当前用户选择的生成类型
     */
    public UserSelection.GenerationType getGenerationType() {
        if (create.isSelected()) {
            return UserSelection.GenerationType.GENERATE_NEW_CLASS;
        } else if (search.isSelected()) {
            return UserSelection.GenerationType.SELECT_EXISTING;
        } else if (createMainSearchJoin.isSelected()) {
            return UserSelection.GenerationType.GENERATE_MAIN_SELECT_JOIN;
        } else {
            throw new IllegalStateException("未选择生成类型");
        }
    }

    /**
     * 获取根对象名
     */
    public String getRootObjectName() {
        return rootObjectNameField.getText().trim();
    }

    /**
     * 获取链接对象名
     */
    public String getJoinObjectName() {
        return joinObjectNameField.getText().trim();
    }

    /**
     * 获取文件保存路径
     */
    public String getFilePath() {
        return filePathField.getText().trim();
    }

    /**
     * 获取是否保存 Java 文件
     */
    public boolean getSaveJavaToFile() {
        return saveJavaToFileCheckBox.isSelected();
    }

    /**
     * 获取关联关系类型
     */
    public String getRelationType() {
        return previousRelationTypeSelection;
    }

    // 新增的 Getter 方法
    public boolean isCreateSwagger() {
        return createSwagger.isSelected();
    }

    public boolean isCreateValidator() {
        return createValidator.isSelected();
    }

    public boolean isCreateLombok() {
        return createLombok.isSelected();
    }
}
