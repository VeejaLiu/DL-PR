package com.veeja.mapper;

import com.veeja.entity.PlateTypeEntity;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlateTypeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PlateTypeEntity record);

    int insertSelective(PlateTypeEntity record);

    PlateTypeEntity selectByPrimaryKey(Integer id);

    List<PlateTypeEntity> selectByCondition(Map map);

    int updateByPrimaryKeySelective(PlateTypeEntity record);

    int updateByPrimaryKey(PlateTypeEntity record);
}