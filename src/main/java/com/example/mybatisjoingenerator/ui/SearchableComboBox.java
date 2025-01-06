package com.example.mybatisjoingenerator.ui;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 李运
 */
public class SearchableComboBox<T> extends JComboBox<T> {

    private List<T> allItems;  // 存储所有项的列表

    private String currentText = "";

    public SearchableComboBox() {
        super();
        this.setEditable(true);  // 启用编辑模式，允许输入

        // 监听输入框的文本变化
        this.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                currentText = ((JTextField) getEditor().getEditorComponent()).getText();
                filterItems();  // 根据输入文本过滤下拉框项
            }
        });
    }

    // 重写 setModel 方法，使得每次设置新模型时支持搜索功能
    @Override
    public void setModel(ComboBoxModel<T> aModel) {
        super.setModel(aModel);  // 调用父类的 setModel 方法
        // 获取模型中的所有项，并存储在 allItems 列表中
        allItems = new ArrayList<>();
        for (int i = 0; i < aModel.getSize(); i++) {
            allItems.add(aModel.getElementAt(i));
        }
        currentText = "";
        filterItems();  // 初始时，清空过滤，显示所有项
    }

    // 根据输入文本过滤下拉框的内容
    private void filterItems() {
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) getModel();

        // 如果文本为空，显示所有项；否则，根据文本过滤项
        List<T> filteredItems = allItems;
        if (!StringUtils.isEmpty(currentText)) {
            filteredItems = allItems.stream()
                    .filter(item -> item.toString().toLowerCase().contains(currentText.toLowerCase()))  // 匹配文本
                    .toList();
        }
        model.removeAllElements();
        model.addAll(filteredItems);  // 添加过滤后的项
        setEditorText();
    }

    public void setEditorText() {
        if (getEditor() != null) {
            ((JTextField) getEditor().getEditorComponent()).setText(currentText);  // 恢复编辑框的文本内容，避免被重置
        }
    }
}
