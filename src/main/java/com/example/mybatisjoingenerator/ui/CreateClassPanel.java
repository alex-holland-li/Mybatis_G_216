package com.example.mybatisjoingenerator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author liyun
 * @program Mybatis_G_216
 * @create 2025/1/5
 **/
public class CreateClassPanel extends JPanel {
    /********第一行*********/
    // 对象名称
    private final JLabel classNameLabel = new JLabel("对象名称:");
    /********第二行*********/
    // 包名，包名的分隔符是"."
    private final JLabel packageNameLabel = new JLabel("包名:");
    //选择保存绝对路径，选择后自动计算出包名(选择的路径-项目源码根路径)
    private final JButton choosePathButton = new JButton("选择包路径");
    /********第三行*********/
    //文件保存绝对路径：源码根路径 + 包名
    private final JLabel savePathLabel = new JLabel("保存路径:");
    private final Project project;
    private final String title;
    private JTextField className;
    private Path rootPath;
    private JTextField packageName;
    //路径选择
    private JFileChooser packagePathChooser;
    private JTextField savePath;

    public CreateClassPanel(Project project, String title) {
        this.project = project;
        this.title = title;
        init();
        buildUi();
        listener();
    }

    public static String calculatePathDiff(Path path1, Path path2) {
        return path1.relativize(path2).toString().replace(File.separator, ".");
    }

    private void listener() {
        choosePathButton.addActionListener(e -> {
            int returnValue = packagePathChooser.showOpenDialog(null);  // 弹出文件夹选择对话框
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // 获取选择的路径
                File selectedFolder = packagePathChooser.getSelectedFile();
                savePath.setText(Paths.get(selectedFolder.getAbsolutePath()).toString());
                //根据选择的路径计算包名
                packageName.setText(calculatePathDiff(rootPath, Paths.get(savePath.getText())));
            }
        });

        //如果用户指定修改包名，自动计算保存路径
        packageName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleTextUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleTextUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleTextUpdate();
            }

            // 通用处理方法
            private void handleTextUpdate() {
                String input = packageName.getText().trim();
                if (isValidPackageName(input)) {
                    packageName.setForeground(JBColor.BLACK);  // 合法时设为黑色
                    //计算保存路径
                    savePath.setText(rootPath.toString() + File.separator + input.replace(".", File.separator));
                } else {
                    packageName.setForeground(JBColor.RED);  // 不合法时设为红色
                }
            }

            // 使用正则表达式验证包名
            private boolean isValidPackageName(String input) {
                // 包名的正则表达式规则：
                // 1. 包名由小写字母、数字和点组成
                // 2. 每一部分（用点分隔）只能包含字母、数字和下划线，且不能以数字开头
                String packageNameRegex = "^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$";
                return Pattern.matches(packageNameRegex, input);
            }
        });
    }

    private void buildUi() {
        // 设置布局管理器
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        // 设置边框
        setBorder(BorderFactory.createTitledBorder(title));

        // 通用设置
        gbc.insets = JBUI.insets(10); // 上、左、下、右边距
        gbc.anchor = GridBagConstraints.WEST; // 组件对齐方式

        // 第一行：对象名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(classNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(className, gbc);

        // 第二行：包名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(packageNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(packageName, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(choosePathButton, gbc);

        // 第三行：保存路径
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(savePathLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(savePath, gbc);
    }

    private void init() {
        // 初始化文本字段
        className = new JTextField(20);
        packageName = new JTextField(20);
        savePath = new JTextField(80);

        // 初始化文件路径选择器
        packagePathChooser = new JFileChooser();
        packagePathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  // 只选择文件夹
        Path basePath = Paths.get(Objects.requireNonNull(project.getBasePath()));
        Path path = Paths.get("src/main/java");
        rootPath = basePath.resolve(path);
        packagePathChooser.setCurrentDirectory(rootPath.toFile());

    }


    public String getClassName() {
        return className.getText().trim();
    }

    public String getPackageName() {
        return packageName.getText().trim();
    }

    public String getSavePath() {
        return savePath.getText().trim();
    }


}
