package com.cc.test.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.test.model.ConfigData;

@Service
public class ConfigService extends ServiceImpl<BaseMapper<ConfigData>, ConfigData> {

	public void updateConfig(ConfigData cd) {
		QueryWrapper<ConfigData> condition = new QueryWrapper<>();
		ConfigData data = getOne(condition);
		if (data == null) {
			save(cd);
			return;
		}

		cd.setId(data.getId());
		updateById(cd);
	}

	public ConfigData getConfigData() {
		return getOne(new QueryWrapper<>());
	}
}
