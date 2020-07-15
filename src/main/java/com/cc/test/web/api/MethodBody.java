package com.cc.test.web.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cc.test.web.api.item.ApiData;
import com.cc.test.web.api.item.FormVO;
import com.cc.test.web.api.item.ObjectData;

import lombok.Data;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-03-25 13:38
 **/
@Data
public class MethodBody implements Serializable {

	private static final long serialVersionUID = -6922771940092546585L;

	private String comment = "";

	private List<FormVO> form = new ArrayList<>();

	/**
	 * 请求体有可能是json或json数组
	 */
	private ApiData request;

	/**
	 * 返回的数据一定是单个json
	 */
	private ObjectData response;

	public void addFormData(FormVO data) {
		form.add(data);
	}
}
