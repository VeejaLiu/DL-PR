package com.veeja.service.impl;

import com.veeja.service.PlateTypeService;
import com.veeja.entity.PlateTypeEntity;
import com.veeja.mapper.PlateTypeMapper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 服务实现层
 *
 * @author veeja
 * @date 2020-09-30T16:54:41.823
 */
@Service
public class PlateTypeServiceImpl implements PlateTypeService {

    @Autowired
    private PlateTypeMapper plateTypeMapper;

    @Override
    public PlateTypeEntity getByPrimaryKey(Integer id) {
        return plateTypeMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageInfo<PlateTypeEntity> queryByPage(Integer pageNo, Integer pageSize, Map<String, Object> map) {
        PageHelper.startPage(pageNo, pageSize);
        return (PageInfo<PlateTypeEntity>) new PageInfo(plateTypeMapper.selectByCondition(map));
    }

    @Override
    public List<PlateTypeEntity> queryByCondition(Map<String, Object> map) {
        return plateTypeMapper.selectByCondition(map);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> save(PlateTypeEntity plateTypeEntity) {
        plateTypeEntity.setId(0);
        plateTypeMapper.insertSelective(plateTypeEntity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", plateTypeEntity.getId());
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Integer deleteById(Integer id) {
        return plateTypeMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Integer updateById(PlateTypeEntity plateTypeEntity) {
        if (null == plateTypeEntity || plateTypeEntity.getId() <= 0) {
            return 0;
        }
        return plateTypeMapper.updateByPrimaryKeySelective(plateTypeEntity);
    }


}
