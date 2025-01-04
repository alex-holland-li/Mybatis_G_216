package com.example.mybatisjoingenerator.actions;

import com.example.mybatisjoingenerator.context.DataBaseContext;
import com.example.mybatisjoingenerator.ui.JoinQueryPanel;
import com.intellij.database.psi.DbDataSource;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.util.Collection;

public class GenerateJoinCodeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("未能获取当前项目。", "错误");
            return;
        }


        // 初始化数据库信息
        Collection<DbDataSource> dataSources = getAllDataSources(project);
        if (dataSources.isEmpty()) {
            Messages.showErrorDialog("未找到任何数据源。", "错误");
            return;
        }

        DataBaseContext.init(dataSources);

        // 创建并显示 JoinQueryPanel
        displayJoinQueryPanel(project);
    }

    private void displayJoinQueryPanel(Project project) {
        // 创建 JoinQueryPanel 实例
        JoinQueryPanel joinQueryPanel = new JoinQueryPanel(project);

        // 创建一个 JFrame 来显示 JoinQueryPanel
        JFrame frame = new JFrame("连接查询");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时释放资源
        frame.getContentPane().add(joinQueryPanel.getMainPanel());  // 将 JoinQueryPanel 添加到 JFrame
        frame.pack();  // 调整窗口大小以适应内容
        frame.setLocationRelativeTo(null);  // 居中显示窗口
        frame.setVisible(true);  // 显示窗口
    }

    /**
     * 获取所有数据源。
     *
     * @param project 当前项目
     * @return 数据源集合
     */
    private Collection<DbDataSource> getAllDataSources(Project project) {
        com.intellij.database.psi.DbPsiFacade dbFacade = com.intellij.database.psi.DbPsiFacade.getInstance(project);
        return dbFacade.getDataSources(); // 获取所有数据源
    }
}
