package com.example.mybatisjoingenerator.ui.ToolWindow;

import com.example.mybatisjoingenerator.ui.JoinQueryPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JoinQueryToolWindow {
    public static final Key<JoinQueryToolWindow> KEY = Key.create("JoinQueryToolWindow");
    private JoinQueryPanel joinQueryPanel;
    private Project project;

    public JoinQueryToolWindow(@NotNull Project project) {
        this.project = project;
        this.joinQueryPanel = new JoinQueryPanel(project);
    }

    /**
     * 获取 JoinQueryPanel 实例
     *
     * @return JoinQueryPanel
     */
    public JoinQueryPanel getJoinQueryPanel() {
        return joinQueryPanel;
    }

    public JPanel getMainPanel() {
        return joinQueryPanel.getMainPanel();
    }
}
