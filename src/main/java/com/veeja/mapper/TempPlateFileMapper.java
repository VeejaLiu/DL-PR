package com.veeja.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.veeja.entity.TempPlateFileEntity;

@Mapper
public interface TempPlateFileMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TempPlateFileEntity record);

    int insertSelective(TempPlateFileEntity record);

    TempPlateFileEntity selectByPrimaryKey(Integer id);

    List<TempPlateFileEntity> selectByCondition(Map<String, Object> map);

    int updateByPrimaryKeySelective(TempPlateFileEntity record);

    int updateByPrimaryKey(TempPlateFileEntity record);
    
    int turncateTable();
    
    int batchInsert(@Param("list")List<TempPlateFileEntity> list);
    
    int updateFileInfo();
    
    
}