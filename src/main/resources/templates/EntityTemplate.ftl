package ${packageName};

<#-- 导入语句 -->
<#if createSwagger>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if createValidator>
import javax.validation.constraints.*;
</#if>
<#if createLombok>
import lombok.Data;
</#if>
<#if (!isJoinClass!false) && relationType != "普通">
import ${joinFullyQualifiedName};
<#if isList?? && isList>
import java.util.List;
</#if>
</#if>

/**
*
* @author liyun
*/

<#-- 类注解 -->
<#if createSwagger>
@ApiModel(description = "${className}对象")
</#if>
<#if createLombok>
@Data
</#if>
public class ${className} {

<#-- 字段定义 -->
    <#list fields as field>
    <#if createSwagger && field.swaggerAnnotation??>
    ${field.swaggerAnnotation}
    </#if>
    <#if createValidator && field.validationAnnotations?? && (field.validationAnnotations?size > 0) >
    <#list field.validationAnnotations as annotation>
    ${annotation}
    </#list>
    </#if>
    private ${field.javaType} ${field.javaFieldName};

    </#list>
<#-- 关联字段 -->
    <#if (!isJoinClass!false) && relationType != "普通">
    <#if relationType == "一对一">
    private ${joinClass} ${joinObjectName};
    </#if>
    <#if relationType == "一对多">
    private List<${joinClass}> ${joinObjectName};
    </#if>
    </#if>

<#-- Getter 和 Setter 方法（如果未使用 Lombok） -->
    <#if !createLombok>
    // Getters and Setters
    <#list fields as field>
    public ${field.javaType} get${field.javaFieldName?cap_first}() {
        return ${field.javaFieldName};
    }

    public void set${field.javaFieldName?cap_first}(${field.javaType} ${field.javaFieldName}) {
        this.${field.javaFieldName} = ${field.javaFieldName};
    }

</#list>
<#-- 关联字段的 Getter 和 Setter -->
    <#if (!isJoinClass!false) && relationType != "普通">
    <#if relationType == "一对一">
    public ${joinClass} get${joinObjectName?cap_first}() {
        return ${joinObjectName};
    }

    public void set${joinObjectName?cap_first}(${joinClass} ${joinObjectName}) {
        this.${joinObjectName} = ${joinObjectName};
    }

    </#if>
    <#if relationType == "一对多">
    public List<${joinClass}> get${joinObjectName?cap_first}List() {
        return ${joinObjectName};
    }

    public void set${joinObjectName?cap_first}List(List<${joinClass}> ${joinObjectName}) {
        this.${joinObjectName} = ${joinObjectName};
    }

    </#if>
    </#if>
    </#if>
}
