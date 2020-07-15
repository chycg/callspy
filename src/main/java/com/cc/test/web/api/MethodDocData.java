package com.cc.test.web.api;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-03-27 18:27
 **/
@Data
public class MethodDocData implements Serializable {

	private static final long serialVersionUID = 9041971519452071308L;

	private String name;

    private Integer catId;

    private String className;

    private String methodName;

    private String doc;
}
