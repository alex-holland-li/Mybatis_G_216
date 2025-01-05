package com.example.mybatisjoingenerator.ui;

import com.intellij.psi.PsiClass;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义列表渲染器，突出显示匹配的搜索关键字为天蓝色。
 */
public class QualifiedNameListCellRenderer extends DefaultListCellRenderer {
    private final ClassSearchPanel searchPanel;

    public QualifiedNameListCellRenderer(ClassSearchPanel searchPanel) {
        this.searchPanel = searchPanel;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // 调用父类方法获取默认渲染器
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) value;
            String qualifiedName = searchPanel.getFullyQualifiedName(psiClass);
            String displayText;

            if (qualifiedName == null || qualifiedName.isEmpty()) {
                // 区分匿名类和局部类
                displayText = (psiClass.getName() != null)
                        ? "局部类: " + psiClass.getName()
                        : "匿名类";
            } else {
                displayText = qualifiedName;
            }

            // 获取当前搜索关键字
            String query = searchPanel.getCurrentQuery();
            if (query != null && !query.isEmpty()) {
                String lowerDisplayText = displayText.toLowerCase();
                String lowerQuery = query.toLowerCase();
                int indexOfMatch = lowerDisplayText.indexOf(lowerQuery);
                if (indexOfMatch != -1) {
                    // 使用 <span> 标签并设置颜色为天蓝色
                    StringBuilder sb = new StringBuilder("<html>");
                    sb.append(displayText.substring(0, indexOfMatch));
                    sb.append("<span style=\"color: #87CEEB;\">")
                            .append(displayText.substring(indexOfMatch, indexOfMatch + query.length()))
                            .append("</span>");
                    sb.append(displayText.substring(indexOfMatch + query.length()));
                    sb.append("</html>");
                    setText(sb.toString());
                } else {
                    setText(displayText);
                }
            } else {
                setText(displayText);
            }
        }

        return this;
    }
}
