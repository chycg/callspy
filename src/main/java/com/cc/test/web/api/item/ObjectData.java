package com.cc.test.web.api.item;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.cc.test.util.ArrayUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 对象类数据
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectData implements ApiData {

	private static final long serialVersionUID = 7996344422789178573L;

	private String type = "object";

	/**
	 * 根级设置title="empty object"；下级不用设置title
	 */
	private String title;

	private Map<String, ApiData> properties;

	private List<String> required;

	private String description;

	@Override
	public DataType dataType() {
		return DataType.OBJECT;
	}

	@Override
	public void updateDesc(ApiData data) {
		if (data == null || !sameType(data))
			return;

		ObjectData od = (ObjectData) data;
		String desc = od.getDescription();
		if (ArrayUtils.isNotEmpty(desc)) {
			this.description = desc;
		}

		for (Map.Entry<String, ApiData> entry : properties.entrySet()) {
			String k = entry.getKey();
			ApiData v = entry.getValue();

			v.updateDesc(od.properties.get(k));
		}
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}