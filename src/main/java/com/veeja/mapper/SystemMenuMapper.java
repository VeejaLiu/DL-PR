package com.veeja.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.veeja.entity.SystemMenuEntity;

@Mapper
public interface SystemMenuMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SystemMenuEntity record);

    int insertSelective(SystemMenuEntity record);

    SystemMenuEntity selectByPrimaryKey(Integer id);

    List<SystemMenuEntity> selectByCondition(Map<String, Object> map);

    int updateByPrimaryKeySelective(SystemMenuEntity record);

    int updateByPrimaryKey(SystemMenuEntity record);
}