package com.cc.test.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.test.model.TestData;

@Service
public class TestDataService extends ServiceImpl<TestDataMapper, TestData> {

	@Autowired
	private TestDataMapper mapper;

	public Integer addTestData(TestData td) {
		td.setAddTime(new Date());
		return mapper.insert(td);
	}

	/**
	 * 仅查询当前工程下的测试用例
	 * 
	 * @param td
	 * @return
	 */
	public List<TestData> listTestData(String project, String host, Integer status) {
		QueryWrapper<TestData> condition = new QueryWrapper<>();
		if (status == null)
			status = 1;

		condition.eq("project", project).eq("status", status).eq("host", host);
		return list(condition);
	}

	public TestData getLatestTestData(String project) {
		QueryWrapper<TestData> condition = new QueryWrapper<>();
		condition.eq("project", project).eq("status", 1);
		condition.orderByDesc("id");
		condition.notLike("host", "insidegateway");

		return getOne(condition);
	}

	public void addOkCount(Long id) {
		mapper.addOkCount(id);
	}

	public void addErrorCount(Long id) {
		mapper.addErrorCount(id);
	}

}
