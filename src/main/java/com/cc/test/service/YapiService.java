package com.cc.test.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cc.test.model.MethodInfo;
import com.cc.test.util.ApiUtils;
import com.cc.test.util.ArrayUtils;
import com.cc.test.util.HttpUtils;
import com.cc.test.web.WebConfig;
import com.cc.test.web.api.ApiBody;
import com.cc.test.web.api.MethodBody;
import com.cc.test.web.api.item.ApiData;
import com.cc.test.web.api.item.ArrayData;
import com.cc.test.web.api.item.DataType;
import com.cc.test.web.api.item.FormVO;
import com.cc.test.web.api.item.MenuVO;
import com.cc.test.web.api.item.ObjectData;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-03-26 10:21
 **/
@Component
public class YapiService {

	@Autowired
	private MainService service;

	@Autowired
	private WebConfig config;

	private Map<String, Integer> projectMap = new HashMap<>();

	private String code = "errcode";

	public int getProjectId(String token) {
		if (ArrayUtils.isEmpty(token))
			token = service.getApiToken();

		if (ArrayUtils.isEmpty(token))
			return 0;

		if (projectMap.containsKey(token))
			return projectMap.get(token);

		String response = HttpUtils.doGet(getDomain() + "/api/project/get?token=" + token);
		JSONObject json = JSON.parseObject(response);

		if (json.getInteger(code) != 0)
			return 0;

		JSONObject data = json.getJSONObject("data");
		Integer projectId = data.getInteger("_id");
		projectMap.put(token, projectId);

		return projectId;
	}

	public List<MenuVO> getCates(String token) {
		int projectId = getProjectId(token);
		if (projectId == 0)
			return new ArrayList<>();

		String url = getDomain() + String.format("/api/interface/getCatMenu?token=%s&projectId=%d", token, projectId);

		String response = HttpUtils.doGet(url);
		JSONObject result = JSON.parseObject(response);

		JSONArray array = result.getJSONArray("data");

		List<MenuVO> list = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			MenuVO vo = new MenuVO();
			JSONObject item = array.getJSONObject(i);

			vo.setProjectId(projectId);
			vo.setId(item.getInteger("_id"));
			vo.setName(item.getString("name"));

			list.add(vo);
		}

		return list;
	}

	/**
	 * @param token
	 * @return
	 */
	public List<MenuVO> getMenus(String token, Integer catId) {
		int projectId = getProjectId(token);
		if (projectId == 0)
			return new ArrayList<>();

		String url = getDomain() + String.format("/api/interface/list_cat?token=%s&catid=%d", token, catId);

		String response = HttpUtils.doGet(url);
		JSONObject result = JSON.parseObject(response);
		JSONObject data = result.getJSONObject("data");

		JSONArray array = data.getJSONArray("list");
		List<MenuVO> list = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject item = array.getJSONObject(i);

			MenuVO menu = new MenuVO();
			menu.setName(item.getString("title"));
			menu.setId(item.getInteger("_id"));
			menu.setPath(item.getString("path"));
			menu.setProjectId(projectId);

			list.add(menu);
		}

		return list;
	}

	/**
	 * @return
	 */
	private String getDomain() {
		return config.getYapiDomain();
	}

	/**
	 * 新增分类
	 *
	 * @param token
	 * @param name
	 * @param desc
	 */
	public JSONObject addCate(String token, String name, String desc) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("desc", desc);
		map.put("token", token);
		map.put("project_id", getProjectId(token));

		String response = HttpUtils.doPost(getDomain() + "/api/interface/add_cat", null, map);
		JSONObject result = JSON.parseObject(response);

		return result;
	}

	/**
	 * 新增API
	 *
	 * @param token
	 *            token
	 * @param catId
	 *            分类id
	 * @param name
	 *            菜单名称
	 * @param methodInfo
	 *            方法信息
	 * @param json
	 *            用户补充输入
	 */
	public JSONObject addApi(String token, Integer catId, String name, MethodInfo methodInfo, JSONObject json) {
		MethodBody doc = mergeComments(methodInfo, json);

		ApiBody body = new ApiBody();
		body.setCatid(catId.toString());
		body.setToken(token);
		body.setTitle(ArrayUtils.isNotEmpty(name) ? name : doc.getComment());
		body.setPath(methodInfo.getUrl());
		body.setBodyData(methodInfo.supportGet(), methodInfo.isJsonType(), doc);

		String response = HttpUtils.postJson(getDomain() + "/api/interface/add", null, JSON.parseObject(JSON.toJSONString(body)));
		JSONObject result = JSON.parseObject(response);
		return result;
	}

	/**
	 * 注释合并
	 *
	 * @param methodInfo
	 * @param json
	 */
	private MethodBody mergeComments(MethodInfo methodInfo, JSONObject json) {
		MethodBody doc = ApiUtils.getMethodData(methodInfo.getMethod());

		boolean jsonType = methodInfo.isJsonType();
		if (jsonType) {
			ApiData request = doc.getRequest();
			JSONObject reqJson = json.getJSONObject("request");

			DataType dt = request.dataType();
			if (dt.isObject()) {
				ObjectData od = (ObjectData) request;
				ObjectData od2 = JSON.parseObject(reqJson.toJSONString(), ObjectData.class);
				od.updateDesc(od2);
			} else if (dt.isObjectArray() || dt.isPlainArray()) {
				ArrayData arrayData = (ArrayData) request;
				ArrayData jsonData = JSON.parseObject(reqJson.toJSONString(), ArrayData.class);
				arrayData.updateDesc(jsonData);
			}
		} else {
			Map<String, FormVO> map = doc.getForm().stream().collect(Collectors.toMap(e -> e.getName(), e -> e));

			JSONArray array = json.getJSONArray("form");
			for (int i = 0; i < array.size(); i++) {
				FormVO item = JSON.parseObject(array.getString(i), FormVO.class);
				if (ArrayUtils.isEmpty(item.getDesc()))
					continue;

				String name = item.getName();
				if (map.containsKey(name) && ArrayUtils.isNotEmpty(item.getDesc())) {
					map.get(name).setDesc(item.getDesc());
				}
			}
		}
		// ObjectData response = doc.getResponse();
		// JSONObject respJson = json.getJSONObject("response");

		return doc;
	}

	public void saveApi(Integer id, Integer catId, ApiBody body) {
		body.setToken(service.getApiToken());

		if (id != null)
			body.setId(id.toString());

		if (catId != null)
			body.setCatid(catId.toString());

		String response = HttpUtils.postJson(getDomain() + "/api/interface/save", null, JSON.parseObject(JSON.toJSONString(body)));
		System.out.println(response);
	}

	/**
	 * 根据路径匹配对应的id
	 *
	 * @param token
	 * @param requestUrl
	 * @return cateId，apiId
	 */
	public Integer[] getApiId(String token, String requestUrl) {
		Map<String, Object> map = new HashMap<>();
		map.put("page", 1);
		map.put("limit", 1000);
		map.put("token", token);
		map.put("project_id", getProjectId(token));

		String response = HttpUtils.doGet(getDomain() + "/api/interface/list", null, map);
		JSONObject result = JSON.parseObject(response);
		JSONObject data = result.getJSONObject("data");
		JSONArray array = data.getJSONArray("list");

		for (int i = 0; i < array.size(); i++) {
			JSONObject item = array.getJSONObject(i);
			if (item.getString("path").equalsIgnoreCase(requestUrl)) {
				return Arrays.asList(item.getInteger("catid"), item.getInteger("_id")).toArray(new Integer[0]);
			}
		}

		return new Integer[0];
	}
}
