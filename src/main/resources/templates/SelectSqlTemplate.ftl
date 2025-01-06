<select id="${id}" resultMap="${resultMapId}">
    SELECT
    ${selectFields}
    FROM ${from}
    <#if joinClause?trim != "">
        ${joinClause}
    </#if>
</select>
