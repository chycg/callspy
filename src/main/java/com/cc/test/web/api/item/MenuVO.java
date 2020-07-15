package com.cc.test.web.api.item;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-03-26 17:28
 **/
@Data
public class MenuVO implements Serializable {

	private static final long serialVersionUID = 963573918328194746L;

	private Integer id;

	private String name;

	private String path;

	private Integer projectId;
}
