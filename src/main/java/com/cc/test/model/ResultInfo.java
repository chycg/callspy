package com.cc.test.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.cc.test.util.Result;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultInfo implements Serializable {

	private static final long serialVersionUID = 8249945739380386866L;

	private boolean success;

	private String code;

	private Object data;

	private String msg;

	private String message;

	private Integer totalItems;

	private Integer totalPages;

	private Integer currentPage;

	private Integer itemsPerPage;

	private Integer pageIndex;

	private Integer pageSize;

	private Integer totalRecords;

	private long costTime;

	private Map<Object, Object> attached;

	private String exception;

	/**
	 * 是否连接错误，如：rpc地址错误或服务未启动导致
	 */
	private Boolean connectionError;

	public static ResultInfo makeResult(Object result) {
		ResultInfo info = new ResultInfo();
		BeanUtils.copyProperties(result, info);

		return info;
	}

	public static ResultInfo success() {
		return success(null);
	}

	public static ResultInfo success(Object data) {
		ResultInfo r = new ResultInfo();
		r.success = true;
		r.setData(data);
		r.msg = "操作成功";

		return r;
	}

	public static ResultInfo fail(String message) {
		ResultInfo r = new ResultInfo();
		r.msg = message;
		return r;
	}

	public static ResultInfo failedLoad() {
		return fail("接口查找失败，请重新加载facade接口");
	}

	public static ResultInfo fail(Result<?> result) {
		ResultInfo r = new ResultInfo();
		r.success = false;
		r.msg = result.getMsg();
		r.code = result.getCode();
		r.setData(result.getData());

		return r;
	}

	public ResultInfo attach(Object key, Object value) {
		if (attached == null) {
			attached = new HashMap<>();
		}

		attached.put(key, value);
		return this;
	}
}
