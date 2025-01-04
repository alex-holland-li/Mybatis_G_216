package com.example.mybatisjoingenerator.ui.ToolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JoinQueryToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建工具窗口对象
        JoinQueryToolWindow joinQueryToolWindow = new JoinQueryToolWindow(project);

        // 创建内容
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(joinQueryToolWindow.getMainPanel(), "", false);

        // 添加到工具窗口
        toolWindow.getContentManager().addContent(content);

        // 绑定工具窗口与 JoinQueryToolWindow 实例（使用用户数据）
        Objects.requireNonNull(toolWindow.getContentManager().getContent(0)).putUserData(JoinQueryToolWindow.KEY, joinQueryToolWindow);
    }
}
