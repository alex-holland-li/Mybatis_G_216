package com.example.mybatisjoingenerator.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 用户界面面板，包含搜索输入框、按钮、结果列表、详细信息显示区域，以及选中类的信息显示区域。
 *
 * @author 李运
 */
public class ClassSearchPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(ClassSearchPanel.class);

    private final Project project;
    private final JTextField searchField;
    private final JButton fuzzySearchButton;
    private final JButton exactSearchButton;
    private final JList<PsiClass> classList;
    private final DefaultListModel<PsiClass> listModel;
    private final JTextArea classDetailsArea;
    private final ClassSearcher classSearcher;
    // 新增的显示组件
    private final JLabel classNameLabel;
    private final JLabel packageNameLabel;
    private final JLabel fullyQualifiedNameLabel;
    private String currentQuery = "";
    private String title;

    String className;
    String packageName;
    String fullyQualifiedName;

    public ClassSearchPanel(Project project, String title) {
        this.title = title;
        this.project = project;
        this.classSearcher = new ClassSearcher(project);
        this.searchField = new JTextField();
        this.fuzzySearchButton = new JButton("模糊搜索");
        this.exactSearchButton = new JButton("精确搜索");
        this.listModel = new DefaultListModel<>();
        this.classList = new JList<>(listModel);
        this.classDetailsArea = new JTextArea();

        // 初始化新增的显示组件
        this.classNameLabel = new JLabel("类名: ");
        this.packageNameLabel = new JLabel("包名: ");
        this.fullyQualifiedNameLabel = new JLabel("全限定名: ");

        initUI();
    }

    /**
     * 初始化用户界面。
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(title));


        // 顶部搜索区
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);

        // 搜索按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(fuzzySearchButton);
        buttonPanel.add(exactSearchButton);
        searchPanel.add(buttonPanel, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        // 中间结果列表
        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classList.setCellRenderer(new QualifiedNameListCellRenderer(this)); // 设置自定义渲染器
        JScrollPane listScrollPane = new JScrollPane(classList);
        // 移除固定大小
        // listScrollPane.setPreferredSize(new Dimension(1000, 60));

        // 右侧详情显示区
        classDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(classDetailsArea);
        // 移除固定大小
        // detailsScrollPane.setPreferredSize(new Dimension(1000, 60));

        // 创建 JSplitPane，水平分隔
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailsScrollPane);
        splitPane.setResizeWeight(0.5); // 初始分割比例为50%
        splitPane.setContinuousLayout(true); // 平滑布局
        splitPane.setOneTouchExpandable(true); // 添加展开/收缩按钮

        // 设置最小大小，防止组件过小
        listScrollPane.setMinimumSize(new Dimension(100, 60));
        detailsScrollPane.setMinimumSize(new Dimension(100, 60));

        add(splitPane, BorderLayout.CENTER);

        // 下方选中类信息显示区
        JPanel selectedClassInfoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        selectedClassInfoPanel.setBorder(BorderFactory.createTitledBorder("选中类信息"));
        selectedClassInfoPanel.add(classNameLabel);
        selectedClassInfoPanel.add(packageNameLabel);
        selectedClassInfoPanel.add(fullyQualifiedNameLabel);
        add(selectedClassInfoPanel, BorderLayout.SOUTH);

        // 绑定事件
        fuzzySearchButton.addActionListener(e -> performSearch(SearchType.FUZZY));
        exactSearchButton.addActionListener(e -> performSearch(SearchType.EXACT));
        searchField.addActionListener(e -> performSearch(SearchType.FUZZY)); // 绑定回车键默认模糊搜索

        // 添加右键上下文菜单监听器
        classList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) { // 检查是否为弹出菜单触发事件
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) { // 某些平台的弹出菜单触发在 mouseReleased
                    showContextMenu(e);
                }
            }

            /**
             * 显示右键上下文菜单。
             *
             * @param e 鼠标事件
             */
            private void showContextMenu(MouseEvent e) {
                int index = classList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return; // 点击在空白处，不显示菜单
                }

                // 选择被点击的项
                classList.setSelectedIndex(index);
                PsiClass selectedClass = listModel.getElementAt(index);
                if (selectedClass == null) {
                    return;
                }

                // 创建上下文菜单
                JPopupMenu contextMenu = new JPopupMenu();
                JMenuItem navigateItem = new JMenuItem("跳转到类定义");
                navigateItem.addActionListener(event -> navigateToClass(selectedClass));
                contextMenu.add(navigateItem);

                // 显示菜单
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // 添加双击事件监听器
        classList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) { // 双击且是左键
                    int index = classList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        PsiClass selectedClass = listModel.getElementAt(index);
                        if (selectedClass != null) {
                            storeClassInfoToUI(selectedClass);
                        }
                    }
                }
            }
        });

        // 添加列表选择监听器以显示详细信息
        classList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displayClassDetails(classList.getSelectedValue());
            }
        });
    }

    /**
     * 执行搜索操作。
     *
     * @param searchType 搜索类型（模糊或精确）
     */
    private void performSearch(SearchType searchType) {
        String query = searchField.getText().trim();
        currentQuery = query;
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入类名进行搜索", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 创建一个模态对话框显示进度
        final JDialog progressDialog = new JDialog((Frame) null, "搜索中...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(BorderLayout.CENTER, progressBar);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(200, 75);
        progressDialog.setLocationRelativeTo(this);

        // 使用 SwingWorker 在后台线程执行搜索
        SwingWorker<List<PsiClass>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PsiClass> doInBackground() {
                if (searchType == SearchType.FUZZY) {
                    return classSearcher.searchClassesFuzzy(query);
                } else {
                    return classSearcher.searchClassesExact(query);
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    List<PsiClass> classes = get();
                    listModel.clear();
                    for (PsiClass psiClass : classes) {
                        listModel.addElement(psiClass);
                    }

                    if (classes.isEmpty()) {
                        JOptionPane.showMessageDialog(ClassSearchPanel.this, "未找到匹配的类", "结果", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("搜索出错", e);
                    JOptionPane.showMessageDialog(ClassSearchPanel.this, "搜索出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * 存储选中类的信息到 UI 并更新显示。
     *
     * @param psiClass 选中的 PsiClass
     */
    private void storeClassInfoToUI(PsiClass psiClass) {
        if (psiClass == null) {
            return;
        }

        className = psiClass.getName();
        packageName = getPackageName(psiClass);
        fullyQualifiedName = getFullyQualifiedName(psiClass);

        // 更新显示组件
        classNameLabel.setText("类名: " + (className != null ? className : "匿名类"));
        packageNameLabel.setText("包名: " + (packageName != null ? packageName : "默认包"));
        fullyQualifiedNameLabel.setText("全限定名: " + (fullyQualifiedName != null ? fullyQualifiedName : "未定义"));
    }

    /**
     * 执行跳转到类定义的操作。
     *
     * @param psiClass 选中的 PsiClass
     */
    private void navigateToClass(PsiClass psiClass) {
        if (psiClass == null) {
            return;
        }

        // 获取包含该类的 PsiFile
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile == null) {
            LOG.warn("无法找到类 " + getFullyQualifiedName(psiClass) + " 所在的文件。");
            return;
        }

        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            LOG.warn("无法找到类 " + getFullyQualifiedName(psiClass) + " 所在的虚拟文件。");
            return;
        }

        // 获取类在文件中的偏移量
        int offset = psiClass.getTextOffset();
        if (offset == -1) {
            LOG.warn("无法找到类 " + getFullyQualifiedName(psiClass) + " 的文本偏移量。");
            return;
        }

        // 使用 OpenFileDescriptor 跳转到指定位置
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, offset);
        if (descriptor != null) {
            descriptor.navigate(true);
        } else {
            LOG.warn("无法创建 OpenFileDescriptor 来导航到类 " + getFullyQualifiedName(psiClass));
        }
    }

    /**
     * 显示选中类的详细信息。
     *
     * @param psiClass 选中的 PsiClass
     */
    private void displayClassDetails(PsiClass psiClass) {
        if (psiClass == null) {
            classDetailsArea.setText("");
            return;
        }
        StringBuilder details = new StringBuilder();
        details.append("类名: ").append(psiClass.getName()).append("\n");
        details.append("全限定名: ").append(getFullyQualifiedName(psiClass)).append("\n");

        String packageName = getPackageName(psiClass);
        details.append("包名: ").append(packageName).append("\n");
        classDetailsArea.setText(details.toString());

        // 调试输出
        LOG.info("类名: " + (psiClass.getName() != null ? psiClass.getName() : "匿名类"));
        LOG.info("全限定名: " + getFullyQualifiedName(psiClass));
        LOG.info("包名: " + packageName);
    }

    /**
     * 获取类的包名。
     *
     * @param psiClass PsiClass 实例
     * @return 包名字符串
     */
    private String getPackageName(PsiClass psiClass) {
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile instanceof PsiJavaFile) {
            return ((PsiJavaFile) psiFile).getPackageName();
        }

        // 通过目录获取包名
        PsiDirectory directory = psiFile.getContainingDirectory();
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
        if (psiPackage != null && !psiPackage.getQualifiedName().isEmpty()) {
            return psiPackage.getQualifiedName();
        }

        return "默认包";
    }

    /**
     * 获取 PsiClass 的全限定名，包括包名和外部类名（如果有）。
     *
     * @param psiClass 要获取全限定名的 PsiClass
     * @return 全限定名字符串，如果无法构建则返回简单名
     */
    public String getFullyQualifiedName(PsiClass psiClass) {
        if (psiClass == null) {
            return "";
        }

        StringBuilder qualifiedName = new StringBuilder();

        // 获取包名
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile instanceof PsiJavaFile) {
            String packageName = ((PsiJavaFile) psiFile).getPackageName();
            LOG.info("PsiJavaFile.getPackageName(): " + packageName);
            if (!packageName.isEmpty()) {
                qualifiedName.append(packageName).append(".");
            } else {
                // 尝试通过目录获取包名
                PsiDirectory directory = psiFile.getContainingDirectory();
                PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
                if (psiPackage != null && !psiPackage.getQualifiedName().isEmpty()) {
                    LOG.info("通过目录获取的包名: " + psiPackage.getQualifiedName());
                    qualifiedName.append(psiPackage.getQualifiedName()).append(".");
                } else {
                    LOG.warn("无法通过 PsiJavaFile 和目录获取包名，可能位于默认包或解析失败。");
                }
            }
        }

        // 递归获取外部类名（处理内部类）
        PsiClass containingClass = psiClass.getContainingClass();
        if (containingClass != null) {
            String outerQualifiedName = getFullyQualifiedName(containingClass);
            if (!outerQualifiedName.isEmpty()) {
                qualifiedName.append(outerQualifiedName).append("$");
            }
        }

        // 添加当前类名
        qualifiedName.append(psiClass.getName());

        LOG.info("构建的全限定名: " + qualifiedName.toString());

        return qualifiedName.toString();
    }

    /**
     * 获取当前搜索关键字。
     *
     * @return 当前搜索关键字
     */
    public String getCurrentQuery() {
        return currentQuery;
    }


    public String getSelectedClassName() {
        return className;
    }

    public String getSelectedFullyQualifiedName() {
        return fullyQualifiedName;
    }


    /**
     * 搜索类型枚举。
     */
    private enum SearchType {
        FUZZY,
        EXACT
    }
}
