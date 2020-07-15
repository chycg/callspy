package com.cc.test.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("config_data")
public class ConfigData implements Serializable {

	private static final long serialVersionUID = -2899062651497209633L;

	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * facade path
	 */
	@TableField
	private String facadePath;

	/**
	 * 类名
	 */
	@TableField
	private Integer effect;

	/**
	 * 是否显示其他Controller
	 */
	@TableField
	private Integer showService;

	/**
	 * 常用接口类数量
	 */
	@TableField
	private Integer oftenSize;

	/**
	 * 接口测试，token
	 */
	@TableField
	private String token;

	/**
	 * yapi token
	 */
	@TableField
	private String apiToken;

	/**
	 * 更新时间，批量测试时更新
	 */
	@TableField
	private Date updateTime = new Date();

}
