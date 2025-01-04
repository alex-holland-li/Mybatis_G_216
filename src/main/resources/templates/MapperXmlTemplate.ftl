<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${mapperPackage}.${className}Mapper">

    <resultMap id="BaseResultMap" type="${packageName}.${className}">
        <#list fields as field>
            <result column="${field.columnName}" property="${field.javaName}"/>
        </#list>
    </resultMap>

    <!-- 插入一条记录 -->
    <insert id="insert" parameterType="${packageName}.${className}">
        <![CDATA[
        INSERT INTO ${tableName}
        (
        <#list fields as field>
            ${field.columnName}<#if field_has_next>,</#if>
        </#list>
        )
        VALUES
        (
        <#list fields as field>
            <#noparse>#{</#noparse>${field.javaName}<#noparse>}</#noparse>
            <#if field_has_next>,</#if>
        </#list>
        )
        ]]>
    </insert>

    <!-- 根据ID删除记录 -->
    <delete id="deleteById" parameterType="long">
        <![CDATA[
        DELETE FROM ${tableName} WHERE id = <#noparse>#{id}</#noparse>
        ]]>
    </delete>

    <!-- 更新记录 -->
    <update id="update" parameterType="${packageName}.${className}">
        <![CDATA[
        UPDATE ${tableName}
        <set>
            <#list fields as field>
                <#if field.columnName != "id">
                    ${field.columnName} = <#noparse>#{</#noparse>${field.javaName}<#noparse>}</#noparse>
                    <#if field_has_next>,</#if>
                </#if>
            </#list>
        </set>
        WHERE id = <#noparse>#{id}</#noparse>
        ]]>
    </update>

    <!-- 根据ID查询记录 -->
    <select id="selectById" parameterType="long" resultMap="BaseResultMap">
        <![CDATA[
        SELECT
        <#list fields as field>
            ${field.columnName}<#if field_has_next>,</#if>
        </#list>
        FROM ${tableName} WHERE id = <#noparse>#{id}</#noparse>
        ]]>
    </select>

    <!-- 查询所有记录 -->
    <select id="selectAll" resultMap="BaseResultMap">
        <![CDATA[
        SELECT
        <#list fields as field>
            ${field.columnName}<#if field_has_next>,</#if>
        </#list>
        FROM ${tableName}
        ]]>
    </select>

</mapper>
