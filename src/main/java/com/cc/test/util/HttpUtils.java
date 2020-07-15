package com.cc.test.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: chenyong
 * @description:
 * @create: 2019-09-25 21:05
 **/
@Slf4j
public class HttpUtils {

	public static String doGet(String url) {
		return doGet(url, null, null);
	}

	public static String doGet(String url, Map<String, String> header, Map<String, Object> data) {
		if (data == null)
			data = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		if (ArrayUtils.isNotEmpty(data)) {
			data.forEach((k, v) -> {
				if (k != null && v != null) {
					sb.append(k).append("=").append(v).append("&");
				}
			});
		}

		if (sb.length() > 0) {
			if (!url.contains("?"))
				url += "?";

			url += sb.substring(0, sb.length() - 1);
		}

		log.info("HttpUtils.doGet: url={},data={}", url, data);

		HttpRequest httpRequest = new HttpRequest(url, HttpRequest.METHOD_GET);
		httpRequest.contentType(HttpRequest.CONTENT_TYPE_FORM);

		if (ArrayUtils.isNotEmpty(header)) {
			for (Map.Entry<String, String> e : header.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if (ArrayUtils.isEmpty(k) || ArrayUtils.isEmpty(v))
					continue;

				httpRequest.header(k, v);
			}
		}

		return httpRequest.body();
	}

	public static String doPost(String url, Map<String, String> header, Map<String, Object> data) {
		if (data == null)
			data = new HashMap<>();

		HttpRequest httpRequest = new HttpRequest(url, HttpRequest.METHOD_POST);
		httpRequest.contentType(HttpRequest.CONTENT_TYPE_FORM);

		if (ArrayUtils.isNotEmpty(header)) {
			for (Map.Entry<String, String> e : header.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if (ArrayUtils.isEmpty(k) || ArrayUtils.isEmpty(v))
					continue;

				httpRequest.header(k, v);
			}
		}

		log.info("HttpUtils.doGet, url={}, data={}", url, data);

		httpRequest.form(data);
		return httpRequest.body();
	}

	public static String postJson(String url, Map<String, String> header, Map<String, Object> data) {
		if (data == null)
			data = new HashMap<>();

		HttpRequest httpRequest = new HttpRequest(url, HttpRequest.METHOD_POST);
		httpRequest.contentType(HttpRequest.CONTENT_TYPE_JSON, "UTF-8");

		if (ArrayUtils.isNotEmpty(header)) {
			for (Map.Entry<String, String> e : header.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if (ArrayUtils.isEmpty(k) || ArrayUtils.isEmpty(v))
					continue;

				httpRequest.header(k, v);
			}
		}

		String body = JSON.toJSONString(data);
		log.info("HttpUtils.postJson,url={}, data={}", url, body);

		httpRequest.send(body);
		return httpRequest.body();
	}
}
