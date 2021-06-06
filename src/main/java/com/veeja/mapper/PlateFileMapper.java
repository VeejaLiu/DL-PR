package com.veeja.mapper;

import com.veeja.entity.PlateFileEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlateFileMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PlateFileEntity record);

    int insertSelective(PlateFileEntity record);

    PlateFileEntity selectByPrimaryKey(Integer id);

    List<PlateFileEntity> selectByCondition(Map<String, Object> map);

    int updateByPrimaryKeySelective(PlateFileEntity record);

    int updateByPrimaryKey(PlateFileEntity record);

    List<PlateFileEntity> getUnRecogniseList();
}