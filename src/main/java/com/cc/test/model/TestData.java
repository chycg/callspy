package com.cc.test.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * @author chenyong
 * @date 2018年11月8日
 */
@Data
@TableName("test_data")
public class TestData implements Serializable {

	private static final long serialVersionUID = -4002506437351270368L;

	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 测试时间
	 */
	@TableField
	private Date addTime;

	/**
	 * 工程名
	 */
	@TableField
	private String project;

	/**
	 * 主机，端口
	 */
	@TableField
	private String host;

	/**
	 * 路径
	 */
	@TableField
	private String url;

	/**
	 * 类名
	 */
	@TableField
	private String className;

	/**
	 * 方法名
	 */
	@TableField
	private String methodName;

	/**
	 * 入参，json
	 */
	@TableField
	private String args;

	/**
	 * 出参，json
	 */
	@TableField
	private String result;

	/**
	 * 执行时长，ms
	 */
	@TableField
	private Long duration;

	/**
	 * 成功1，失败0
	 */
	@TableField
	private Integer status;

	/**
	 * success count
	 */
	@TableField
	private Integer okCount = 0;

	/**
	 * 失败次数
	 */
	@TableField
	private Integer errorCount = 0;

	/**
	 * 更新时间，批量测试时更新
	 */
	@TableField
	private Date updateTime;
}
