package ${packageName};

<#if useLombok>
    import lombok.Data;
</#if>
<#if useJpa>
    import javax.persistence.*;
</#if>
<#if javaTypes?seq_contains("LocalDate")>
    import java.time.LocalDate;
</#if>
<#if javaTypes?seq_contains("LocalDateTime")>
    import java.time.LocalDateTime;
</#if>
<#if javaTypes?seq_contains("LocalTime")>
    import java.time.LocalTime;
</#if>

<#if useLombok>
    @Data
</#if>
<#if useJpa>
    @Entity
    @Table(name = "${tableName}")
</#if>

/**
* @author Mybatis_G_216
*/
public class ${className} {

<#list fields as field>
    <#if useJpa>
        @Column(name = "${field.columnName}")
    </#if>
    private ${field.javaType} ${field.javaName};

</#list>

// Getters and Setters (如果不使用 Lombok)
<#if !useLombok>
    <#list fields as field>
        public ${field.javaType} get${field.javaName?cap_first}() {
        return ${field.javaName};
        }

        public void set${field.javaName?cap_first}(${field.javaType} ${field.javaName}) {
        this.${field.javaName} = ${field.javaName};
        }

    </#list>
</#if>
}
