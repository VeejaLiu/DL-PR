package com.veeja.service;

import com.veeja.entity.PlateTypeEntity;

import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;


/**
 * 服务实现层接口
 * @author veeja
 * @date 2020-09-30T16:54:41.820
 */
public interface PlateTypeService {
	
    public PlateTypeEntity getByPrimaryKey(Integer id);
    
    public PageInfo<PlateTypeEntity> queryByPage(Integer pageNo, Integer pageSize, Map<String, Object> map);
    
    public List<PlateTypeEntity> queryByCondition(Map<String, Object> map);
    
    public Map<String, Object> save(PlateTypeEntity plateTypeEntity);

	public Integer deleteById(Integer id);

    public Integer updateById(PlateTypeEntity plateTypeEntity);
}
