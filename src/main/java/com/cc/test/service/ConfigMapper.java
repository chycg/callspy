package com.cc.test.service;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.test.model.ConfigData;

@Mapper
public interface ConfigMapper extends BaseMapper<ConfigData> {


}