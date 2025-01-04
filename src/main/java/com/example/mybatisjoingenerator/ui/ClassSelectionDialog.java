package com.example.mybatisjoingenerator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassSelectionDialog extends JDialog {
    private final JTextField searchTextField;
    private final JList<String> classList;
    private final DefaultListModel<String> listModel;
    private String selectedClassName;

    public ClassSelectionDialog(Project project) {
        setTitle("选择类");
        setSize(400, 300);
        setLocationRelativeTo(null);

        // 初始化组件
        searchTextField = new JTextField();
        listModel = new DefaultListModel<>();
        classList = new JList<>(listModel);

        // 加载并展示当前项目中的类
        loadClasses(project);

        // 设置布局
        setLayout(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("搜索类:"));
        searchPanel.add(searchTextField);
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(classList), BorderLayout.CENTER);

        // 搜索框事件监听
        searchTextField.addCaretListener(e -> filterClasses(searchTextField.getText()));

        // 类选择事件监听
        classList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedClassName = classList.getSelectedValue();
                if (selectedClassName != null) {
                    dispose();
                }
            }
        });
    }

    // 获取当前项目中所有的 Java 类
    private void loadClasses(Project project) {
        List<String> classNames = getAllJavaClasses(project);
        for (String className : classNames) {
            listModel.addElement(className);
        }
    }

    // 获取项目中的所有 Java 类
    private List<String> getAllJavaClasses(Project project) {
        List<String> classNames = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);

        // 获取项目中的所有源代码目录
        VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();

        for (VirtualFile sourceRoot : sourceRoots) {
            PsiDirectory directory = psiManager.findDirectory(sourceRoot);
            if (directory != null) {
                // 遍历目录下的所有文件
                collectJavaClasses(directory, classNames);
            }
        }
        return classNames;
    }

    // 递归遍历目录中的所有 Java 文件，并提取类名
    private void collectJavaClasses(PsiDirectory directory, List<String> classNames) {
        PsiFile[] files = directory.getFiles();
        for (PsiFile file : files) {
            // 检查是否是 Java 文件
            if (file.getFileType().getName().equalsIgnoreCase("JAVA")) {
                PsiClass[] classes = PsiTreeUtil.getChildrenOfType(file, PsiClass.class);  // 获取所有类
                if (classes != null) {
                    for (PsiClass psiClass : classes) {
                        // 获取类的完全限定名
                        classNames.add(psiClass.getQualifiedName());
                    }
                }
            }

            // 如果是目录，则继续递归
            if (file instanceof PsiDirectory) {
                collectJavaClasses((PsiDirectory) file, classNames);
            }
        }
    }

    // 根据搜索条件过滤类
    private void filterClasses(String searchText) {
        listModel.clear();
        List<String> filteredClasses = getFilteredClasses(searchText);
        for (String className : filteredClasses) {
            listModel.addElement(className);
        }
    }

    // 根据搜索条件返回匹配的类
    private List<String> getFilteredClasses(String searchText) {
        List<String> classNames = getAllJavaClasses();
        List<String> filtered = new ArrayList<>();
        for (String className : classNames) {
            if (className.toLowerCase().contains(searchText.toLowerCase())) {
                filtered.add(className);
            }
        }
        return filtered;
    }

    // 获取当前选择的类
    public String getSelectedClass() {
        return selectedClassName;
    }
}
