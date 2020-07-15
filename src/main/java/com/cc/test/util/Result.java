package com.cc.test.util;

import lombok.Data;

@Data
public class Result<T> {

	private String code;

	private String msg;

	private boolean success;

	private T data;
}
