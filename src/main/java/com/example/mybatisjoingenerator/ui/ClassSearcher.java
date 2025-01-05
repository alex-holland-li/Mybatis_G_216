package com.example.mybatisjoingenerator.ui;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责根据类名进行搜索，包括模糊搜索和精确搜索。
 */
public class ClassSearcher {

    private final Project project;

    public ClassSearcher(@NotNull Project project) {
        this.project = project;
    }

    /**
     * 模糊搜索类名，返回匹配的 PsiClass 列表。
     *
     * @param partialName 类名的部分或模糊名称
     * @return 匹配的 PsiClass 列表
     */
    public List<PsiClass> searchClassesFuzzy(@NotNull String partialName) {
        return ReadAction.compute(() -> {
            List<PsiClass> result = new ArrayList<>();
            String lowerPartialName = partialName.toLowerCase();

            // 获取 PsiShortNamesCache 实例
            PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
            if (cache == null) {
                return result;
            }

            // 获取所有类名
            String[] allClassNames = cache.getAllClassNames();

            for (String className : allClassNames) {
                if (className.toLowerCase().contains(lowerPartialName)) { // 不区分大小写的模糊匹配
                    PsiClass[] classes = cache.getClassesByName(className, GlobalSearchScope.allScope(project));
                    for (PsiClass psiClass : classes) {
                        if (!isAnonymousOrLocalClass(psiClass)) {
                            result.add(psiClass);
                        }
                    }
                }
            }

            return result;
        });
    }

    /**
     * 精确搜索类名，返回匹配的 PsiClass 列表。
     *
     * @param exactName 完全匹配的类名
     * @return 匹配的 PsiClass 列表
     */
    public List<PsiClass> searchClassesExact(@NotNull String exactName) {
        return ReadAction.compute(() -> {
            List<PsiClass> result = new ArrayList<>();

            // 获取 PsiShortNamesCache 实例
            PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
            if (cache == null) {
                return result;
            }

            // 获取匹配的类
            PsiClass[] classes = cache.getClassesByName(exactName, GlobalSearchScope.allScope(project));
            for (PsiClass psiClass : classes) {
                if (!isAnonymousOrLocalClass(psiClass)) {
                    result.add(psiClass);
                }
            }

            return result;
        });
    }

    /**
     * 判断一个 PsiClass 是否为匿名类或局部类。
     *
     * @param psiClass 要检查的类
     * @return 如果是匿名类或局部类，则返回 true；否则返回 false
     */
    private boolean isAnonymousOrLocalClass(@NotNull PsiClass psiClass) {
        // 匿名类没有名称
        if (psiClass.getName() == null) {
            return true; // 匿名类
        }

        // 检查父级是否为方法或初始化器
        return psiClass.getParent() instanceof com.intellij.psi.PsiMethod ||
                psiClass.getParent() instanceof com.intellij.psi.PsiClassInitializer;
    }
}
