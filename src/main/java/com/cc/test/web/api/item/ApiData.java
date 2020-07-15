package com.cc.test.web.api.item;

import java.io.Serializable;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-02-06 11:31
 **/
public interface ApiData extends Serializable {

	/**
	 * 数据类型
	 *
	 * @return
	 */
	default boolean sameType(ApiData data) {
		return data.getClass() == getClass();
	}

	DataType dataType();

	/**
	 * 注释更新
	 *
	 * @param data
	 */
	void updateDesc(ApiData data);
}
