<resultMap id="${resultMapId}" type="${mainClass}">
    <#-- 主表字段映射 -->
    <#list mainFields as field>
        <result column="${mainAlias}_${field.columnName}" property="${field.javaFieldName}"/>
    </#list>
    <#if relationType == "普通">
        <#list joinFields as joinField>
            <result column="${joinAlias}_${joinField.columnName}" property="${joinField.javaFieldName}"/>
        </#list>
    </#if>
    <#-- 一对一关联映射 -->
    <#if relationType == "一对一">
        <association property="${joinObjectName}" javaType="${joinClass}">
            <#list joinFields as joinField>
                <result column="${joinAlias}_${joinField.columnName}" property="${joinField.javaFieldName}"/>
            </#list>
        </association>
    </#if>
    <#-- 一对多关联映射 -->
    <#if relationType == "一对多">
        <collection property="${joinObjectName}" ofType="${joinClass}">
            <#list joinFields as joinField>
                <result column="${joinAlias}_${joinField.columnName}" property="${joinField.javaFieldName}"/>
            </#list>
        </collection>
    </#if>
</resultMap>
