package ${mapperPackage};

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import ${packageName}.${className};

@Mapper
public interface ${className}Mapper {

// 插入一条记录
void insert(${className} ${className?uncap_first});

// 根据ID删除记录
void deleteById(@Param("id") Long id);

// 更新记录
void update(${className} ${className?uncap_first});

// 根据ID查询记录
${className} selectById(@Param("id") Long id);

// 查询所有记录
List<${className}> selectAll();
}