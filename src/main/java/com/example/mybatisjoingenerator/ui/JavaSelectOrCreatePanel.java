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
 * 包括生成新类、选择现有类以及主对象生成被连接对象选择等选项
 * 同时支持生成 Swagger 注解、参数校验注解和 Lombok 注解
 *
 * @author
 */
public class JavaSelectOrCreatePanel extends JPanel {
    JLabel resultObject = new JLabel("结果类：");
    JLabel relationType = new JLabel("关联关系：");
    JLabel joinObjectNameLabel = new JLabel("连接对象字段名：");
    JTextField joinObjectNameTextField;
    // 选择对象或生成对象单选框
    JCheckBox create = new JCheckBox("生成新类");
    JCheckBox search = new JCheckBox("选择现有");
    JCheckBox createMainSearchJoin = new JCheckBox("主对象生成，被连接对象选择");
    JCheckBox createSwagger = new JCheckBox("生成Swagger");
    JCheckBox createValidator = new JCheckBox("生成参数校验注解");
    JCheckBox createLombok = new JCheckBox("生成Lombok注解");
    private SearchableComboBox relationTypeComboBox;
    // 添加 ItemListener 来确保每次只有一个复选框被选中
    //选择主对象类设置面板
    private ClassSearchPanel mainClassSearchPanel;
    //选择连接对象类面板
    private ClassSearchPanel joinClassSearchPanel;
    // 如果封装结果对象不是嵌套，选择对象类面板
    private ClassSearchPanel classSearchPanel;

    //生成主对象类设置面板
    private CreateClassPanel mainCreateClassPanel;
    // 生成连接对象类设置面板
    private CreateClassPanel joinCreateClassPanel;
    // 如果生成封装结果对象不是嵌套，生成对象类设置面板
    private CreateClassPanel createClassPanel;

    private JPanel kuozhan;

    private Project project;
    private String title;

    private String previousRelationTypeSelection = "";

    // 1：生成对象、2:选择对象、3：生成对象+选择对象
    private int selectedIndex = 0;

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
        relationTypePanel.add(joinObjectNameLabel);
        relationTypePanel.add(joinObjectNameTextField);
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

        joinObjectNameTextField = new JTextField(20);
        joinObjectNameTextField.setVisible(false);
        joinObjectNameLabel.setVisible(false);
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
                    joinObjectNameLabel.setVisible(false);
                    joinObjectNameTextField.setVisible(false);
                } else {
                    mainCreateClassPanel.setVisible(true);
                    joinCreateClassPanel.setVisible(true);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(false);
                    joinClassSearchPanel.setVisible(false);
                    createClassPanel.setVisible(false);

                    joinObjectNameLabel.setVisible(true);
                    joinObjectNameTextField.setVisible(true);
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

                    joinObjectNameLabel.setVisible(false);
                    joinObjectNameTextField.setVisible(false);
                } else {
                    mainCreateClassPanel.setVisible(false);
                    joinCreateClassPanel.setVisible(false);
                    classSearchPanel.setVisible(false);
                    mainClassSearchPanel.setVisible(true);
                    joinClassSearchPanel.setVisible(true);
                    createClassPanel.setVisible(false);

                    joinObjectNameLabel.setVisible(true);
                    joinObjectNameTextField.setVisible(true);
                }
                break;
            // createMainSearchJoin
            case 3:
                // 对于 case 3，根据 relationType 可能需要更多逻辑
                // 目前不需要特殊处理

                joinObjectNameLabel.setVisible(true);
                joinObjectNameTextField.setVisible(true);
                break;
            default:
                break;
        }
    }

    /**
     * 获取当前用户选择的生成类型
     * 返回是'生成新类'还是'选择类'还是'主对象生成，被连接对象选择' 标识
     * 用于后续的生成代码逻辑
     * 1：生成对象、2:选择对象、3：生成对象+选择对象
     * 1.生成对象时:
     * 如果是'普通'则根据 createClassPanel获取信息生成
     * 如果是'一对一'，'一对多' 根据 mainCreateClassPanel , joinCreateClassPanel获取信息生成
     * 2.选择对象时:
     * 如果是'普通'则根据 classSearchPanel获取信息处理
     * 如果是'一对一'，'一对多' 根据 mainClassSearchPanel , joinClassSearchPanel获取信息处理
     * 3.生成对象+选择对象时:
     * 根据 mainCreateClassPanel , joinClassSearchPanel获取信息处理
     *
     * @return UserSelection.GenerationType 枚举类型
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
     * 获取关联关系类型
     *
     * @return String 关联关系类型
     */
    public String getRelationType() {
        return previousRelationTypeSelection;
    }

    /**
     * 获取生成主对象信息
     *
     * @return GeneratedClassInfo 生成主对象类的信息
     */
    public GeneratedClassInfo getGeneratedMainClassInfo() {
        if (mainCreateClassPanel.isVisible()) {
            return new GeneratedClassInfo(
                    mainCreateClassPanel.getPackageName(),
                    mainCreateClassPanel.getClassName(),
                    mainCreateClassPanel.getSavePath(),
                    false
            );
        }
        return null;
    }

    /**
     * 获取选择主对象信息
     *
     * @return SelectedClassInfo 选择主对象类的信息
     */
    public SelectedClassInfo getSelectedMainClassInfo() {
        if (mainClassSearchPanel.isVisible()) {
            return new SelectedClassInfo(
                    mainClassSearchPanel.getSelectedClassName(),
                    mainClassSearchPanel.getSelectedFullyQualifiedName()
            );
        }
        return null;
    }

    /**
     * 获取生成链接对象信息
     *
     * @return GeneratedClassInfo 生成链接对象类的信息
     */
    public GeneratedClassInfo getGeneratedJoinClassInfo() {
        if (joinCreateClassPanel.isVisible()) {
            return new GeneratedClassInfo(
                    joinCreateClassPanel.getPackageName(),
                    joinCreateClassPanel.getClassName(),
                    joinCreateClassPanel.getSavePath(),
                    true
            );
        }
        return null;
    }

    /**
     * 获取选择链接对象信息
     *
     * @return SelectedClassInfo 选择链接对象类的信息
     */
    public SelectedClassInfo getSelectedJoinClassInfo() {
        if (joinClassSearchPanel.isVisible()) {
            return new SelectedClassInfo(
                    joinClassSearchPanel.getSelectedClassName(),
                    joinClassSearchPanel.getSelectedFullyQualifiedName()
            );
        }
        return null;
    }

    /**
     * 获取生成对象信息
     *
     * @return GeneratedClassInfo 生成对象类的信息
     */
    public GeneratedClassInfo getGeneratedClassInfo() {
        if (createClassPanel.isVisible()) {
            return new GeneratedClassInfo(
                    createClassPanel.getPackageName(),
                    createClassPanel.getClassName(),
                    createClassPanel.getSavePath(),
                    false
            );
        }
        return null;
    }

    /**
     * 获取选择对象信息
     *
     * @return SelectedClassInfo 选择对象类的信息
     */
    public SelectedClassInfo getSelectedClassInfo() {
        if (classSearchPanel.isVisible()) {
            return new SelectedClassInfo(
                    classSearchPanel.getSelectedClassName(),
                    classSearchPanel.getSelectedFullyQualifiedName()
            );
        }
        return null;
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

    public String getJoinObjectName() {
        return joinObjectNameTextField.getText();
    }

    public static class SelectedClassInfo {
        private String className;
        private String fullyQualifiedName;

        public SelectedClassInfo(String className, String fullyQualifiedName) {
            this.className = className;
            this.fullyQualifiedName = fullyQualifiedName;
        }

        public String getClassName() {
            return className;
        }

        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }
    }

    /**
     * 生成的类信息
     */
    public class GeneratedClassInfo {
        private String packageName;
        private String className;
        private String savePath;
        private boolean isJoinClass; // 是否为连接类

        public GeneratedClassInfo(String packageName, String className, String savePath, boolean isJoinClass) {
            this.packageName = packageName;
            this.className = className;
            this.savePath = savePath;
            this.isJoinClass = isJoinClass;
        }

        // Getters and Setters

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getSavePath() {
            return savePath;
        }

        public void setSavePath(String savePath) {
            this.savePath = savePath;
        }

        public boolean isJoinClass() {
            return isJoinClass;
        }

        public void setJoinClass(boolean joinClass) {
            isJoinClass = joinClass;
        }
    }

}
