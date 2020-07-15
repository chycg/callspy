package com.cc.test.service;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.test.model.TestData;

@Mapper
public interface TestDataMapper extends BaseMapper<TestData> {

	/**
	 * 新增成功次数
	 * 
	 * @param id
	 * @return
	 */
	@Update("update test_data set ok_count = ok_count + 1, update_time = now() where id = #{id}")
	int addOkCount(@Param("id") Long id);

	/**
	 * 失败次数
	 * 
	 * @param id
	 * @return
	 */
	@Update("update test_data set error_count = error_count + 1, update_time = now() where id = #{id}")
	int addErrorCount(@Param("id") Long id);

}