package com.veeja.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.veeja.entity.PlateRecoDebugEntity;

@Mapper
public interface PlateRecoDebugMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PlateRecoDebugEntity record);

    int insertSelective(PlateRecoDebugEntity record);

    PlateRecoDebugEntity selectByPrimaryKey(Integer id);

    List<PlateRecoDebugEntity> selectByCondition(Map<String, Object> map);

    int updateByPrimaryKeySelective(PlateRecoDebugEntity record);

    int updateByPrimaryKey(PlateRecoDebugEntity record);
    
    int deleteByParentId(@Param("parentId")Integer parentId);
    
    int batchInsert(@Param("list")List<PlateRecoDebugEntity> list);
}