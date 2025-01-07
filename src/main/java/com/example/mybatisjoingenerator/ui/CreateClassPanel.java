package com.example.mybatisjoingenerator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
//import java.awt.*;
import java.awt.*;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
    private final JButton choosePathButton = new JButton("选择保存路径");
    /********第三行*********/
    //文件保存绝对路径：源码根路径 + 包名
    private final JLabel savePathLabel = new JLabel("保存路径:");
    private final Project project;
    private final String title;
    private JTextField className;
    //private Path rootPath;
    private JTextField packageName;
    //路径选择
    private JFileChooser packagePathChooser;
    private JTextField savePath;
    private String rootPathString = "";
    private boolean packageNameIsUpdate = true;
    private boolean savePathIsUpdate = true;

    //private Boolean userUpdate

    public CreateClassPanel(Project project, String title) {
        this.project = project;
        this.title = title;
        init();
        buildUi();
        listener();
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
        Path resolve = basePath.resolve(path);
        if (resolve.toFile().exists()) {
            rootPathString = resolve.toString();
            packagePathChooser.setCurrentDirectory(resolve.toFile());
        } else {
            rootPathString = basePath.toString();
            packagePathChooser.setCurrentDirectory(basePath.toFile());
        }

    }

    private void listener() {
        choosePathButton.addActionListener(e -> {
            int returnValue = packagePathChooser.showOpenDialog(null);  // 弹出文件夹选择对话框
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // 获取选择的路径
                File selectedFolder = packagePathChooser.getSelectedFile();
                Path path = Paths.get(selectedFolder.getAbsolutePath());
                Map<String, String> stringMap = calculatePathDiff(path);

                if (stringMap.get("rootPathString") == null) {
                    Messages.showWarningDialog("请选择java源码目录", "警告");
                } else {

                    String savePathString = stringMap.get("savePathString");
                    String packagePathString = stringMap.get("packagePathString");
                    packageNameIsUpdate = false;
                    savePathIsUpdate = false;
                    rootPathString = stringMap.get("rootPathString");
                    savePath.setText(savePathString);
                    packageName.setText(packagePathString);
                    packageNameIsUpdate = true;
                    savePathIsUpdate = true;
                }
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
                if (input.isEmpty() || isValidPackageName(input)) {
                    packageName.setForeground(JBColor.BLACK);  // 合法时设为黑色
                    if (packageNameIsUpdate) {
                        savePathIsUpdate = false;
                        savePath.setText(createPath(rootPathString, input).toString());
                        savePathIsUpdate = true;
                    }
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

        //如果用户指定修改包名，自动计算保存路径
        savePath.getDocument().addDocumentListener(new DocumentListener() {
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

            private void handleTextUpdate() {
                String input = savePath.getText().trim();

                if (isValidDirectoryPathFormat(input)) {
                    Path path = Paths.get(input);
                    Map<String, String> stringMap = calculatePathDiff(path);
                    if (stringMap.get("rootPathString") != null) {
                        savePath.setForeground(JBColor.BLACK);
                        if (savePathIsUpdate) {
                            packageNameIsUpdate = false;
                            rootPathString = stringMap.get("rootPathString");
                            packageName.setText(stringMap.get("packagePathString"));
                            packageNameIsUpdate = true;
                        }
                    } else {
                        savePath.setForeground(JBColor.RED);  // 不合法时
                    }
                } else {
                    savePath.setForeground(JBColor.RED);  // 不合法时设为红色
                }
            }
        });
    }

    /**
     * 解析 path 中最后一个 "src/main/java" 路径段的前后部分。
     * 前部分作为 Path 返回，后部分转换为包路径字符串。
     * 考虑不同的操作系统路径分隔符。
     *
     * @param path 输入的目录 Path 对象
     * @return PathDiff 对象，包含前路径和包路径，如果未找到 "src/main/java" 则 prePath 为原路径，packagePath 为空字符串
     */
    public Map<String, String> calculatePathDiff(Path path) {
        Map<String, String> objectObjectHashMap = new HashMap<>();
        if (path == null) {
            return objectObjectHashMap;
        }

        objectObjectHashMap.put("savePathString", path.toString());

        // 将路径元素存储到列表中
        List<String> pathElements = new ArrayList<>();
        path.forEach(element -> pathElements.add(element.toString()));

        // 定义需要查找的路径片段
        String[] target = {"src", "main", "java"};

        // 搜索最后一次出现 "src/main/java" 的起始位置
        int lastIndex = -1;
        for (int i = 0; i <= pathElements.size() - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (!pathElements.get(i + j).equals(target[j])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                lastIndex = i + target.length;
            }
        }

        if (lastIndex == -1) {
            // 未找到 "src/main/java"，返回原路径和空包路径
            return objectObjectHashMap;
        }

        // 构建 prePath
        Path prePath = path.getRoot();
        for (int i = 0; i < lastIndex; i++) {
            prePath = prePath.resolve(pathElements.get(i));
        }

        // 获取 "src/main/java" 后面的部分
        List<String> subPathElements = pathElements.subList(lastIndex, pathElements.size());

        if (subPathElements.isEmpty()) {
            // "src/main/java" 后没有子路径
            objectObjectHashMap.put("rootPathString", path.toString());
            objectObjectHashMap.put("packagePathString", "");
            return objectObjectHashMap;
        }

        // 将路径元素用 "." 连接成包路径
        String packagePath = String.join(".", subPathElements);

        objectObjectHashMap.put("rootPathString", prePath.toString());
        objectObjectHashMap.put("packagePathString", packagePath);
        return objectObjectHashMap;
    }

    /**
     * 通过根路径字符串和包名字符串创建一个完整的 Path 对象。
     *
     * @param rootPathStr 根路径字符串，例如 "C:\\Users\\user\\project\\src\\main\\java" 或 "/home/user/project/src/main/java"
     * @param packageName 包名字符串，例如 "com.example.util"
     * @return 组合后的 Path 对象
     */
    public static Path createPath(String rootPathStr, String packageName) {
        if (rootPathStr == null || rootPathStr.trim().isEmpty()) {
            throw new IllegalArgumentException("根路径不能为空");
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            return Paths.get(rootPathStr);
        }

        // 分解包名
        String[] packageParts = packageName.split("\\.");

        // 组合路径
        return Paths.get(rootPathStr, packageParts);
    }

    // 正则表达式模式匹配Windows路径
    private static final Pattern WINDOWS_PATH_PATTERN = Pattern.compile("^[A-Za-z]:\\\\((?:[^\\\\/:*?\"<>|]+\\\\)*[^\\\\/:*?\"<>|]*)?$");

    // 正则表达式模式匹配Unix/Linux/Mac路径
    private static final Pattern UNIX_PATH_PATTERN = Pattern.compile("^/(?:[^/]+/)*$");

    /**
     * 检查给定的字符串是否是一个格式上有效的文件夹绝对路径。
     * 考虑不同操作系统的路径分隔符和字符限制。
     *
     * @param inputPath 要验证的路径字符串
     * @return 如果格式有效则返回 true，否则返回 false
     */
    public static boolean isValidDirectoryPathFormat(String inputPath) {
        if (inputPath == null || inputPath.isEmpty()) {
            return false;
        }

        // 判断操作系统类型
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        Pattern pattern = isWindows ? WINDOWS_PATH_PATTERN : UNIX_PATH_PATTERN;

        // 匹配路径格式
        return pattern.matcher(inputPath).matches();
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
