package com.example.mybatisjoingenerator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 一个用于展示生成代码的独立窗口，包含四个子面板，每个子面板支持独立复制到剪贴板。
 */
public class CodeDisplayFrame extends JFrame {
    private CodePanel sqlPanel;
    private CodePanel resultMapPanel;
    private CodePanel mapperInterfacePanel;
    private CodePanel javaClassesPanel;

    /**
     * 构造函数
     *
     * @param sqlCode         MyBatis XML 查询动态 SQL
     * @param resultMapCode   MyBatis XML resultMap
     * @param mapperCode      Mapper 接口代码
     * @param javaClassesCode Java 对象源码
     */
    public CodeDisplayFrame(String sqlCode, String resultMapCode, String mapperCode, String javaClassesCode) {
        setTitle("生成的代码");
        setSize(900, 800);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口时仅销毁当前窗口

        initializeComponents(sqlCode, resultMapCode, mapperCode, javaClassesCode);
        layoutComponents();
    }

    /**
     * 初始化组件
     *
     * @param sqlCode         MyBatis XML 查询动态 SQL
     * @param resultMapCode   MyBatis XML resultMap
     * @param mapperCode      Mapper 接口代码
     * @param javaClassesCode Java 对象源码
     */
    private void initializeComponents(String sqlCode, String resultMapCode, String mapperCode, String javaClassesCode) {
        sqlPanel = new CodePanel("=== MyBatis XML 查询动态 SQL ===", sqlCode);
        resultMapPanel = new CodePanel("=== MyBatis XML resultMap ===", resultMapCode);
        mapperInterfacePanel = new CodePanel("=== Mapper 接口 ===", mapperCode);
        javaClassesPanel = new CodePanel("=== Java 对象源码 ===", javaClassesCode);
    }

    /**
     * 布局组件
     */
    private void layoutComponents() {
        setLayout(new GridLayout(4, 1, 10, 10)); // 四行一列，间距为10

        add(sqlPanel);
        add(resultMapPanel);
        add(mapperInterfacePanel);
        add(javaClassesPanel);
    }

    /**
     * 内部类，用于展示单个代码片段的面板
     */
    class CodePanel extends JPanel {
        private JLabel titleLabel;
        private JTextArea codeTextArea;
        private JButton copyButton;

        /**
         * 构造函数
         *
         * @param title 标题
         * @param code  代码内容
         */
        public CodePanel(String title, String code) {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createTitledBorder(title));

            // 初始化组件
            codeTextArea = new JTextArea(code);
            codeTextArea.setEditable(false);
            codeTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            codeTextArea.setCaretPosition(0); // 将光标置于顶部

            JScrollPane scrollPane = new JScrollPane(codeTextArea);
            scrollPane.setPreferredSize(new Dimension(800, 150));

            copyButton = new JButton("复制到剪贴板");

            // 添加组件
            add(scrollPane, BorderLayout.CENTER);
            add(copyButton, BorderLayout.SOUTH);

            // 添加事件监听器
            copyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    copyCodeToClipboard();
                }
            });
        }

        /**
         * 将代码复制到剪贴板
         */
        private void copyCodeToClipboard() {
            String code = codeTextArea.getText();
            StringSelection stringSelection = new StringSelection(code);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, "代码已复制到剪贴板。", "复制成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
