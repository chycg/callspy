package com.cc.test.web.api.item;

import java.io.Serializable;
import lombok.Data;

/**
 * 表单数据
 *
 * @author: chenyong
 * @description:
 * @create: 2020-03-25 14:05
 **/
@Data
public class FormVO implements Serializable {

	private static final long serialVersionUID = 7106879668016500035L;

	private String name;

	private String type = "text";

	private Integer required = 1;

	private String desc;

	private Object example;

	@Override
	public String toString() {
		return "FormData{" + "name='" + name + '\'' + ", required=" + required + ", example=" + example + ", desc='" + desc + '\'' + '}';
	}
}
