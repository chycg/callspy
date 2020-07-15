package com.cc.test.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cc.test.model.ClassInfo;
import com.cc.test.model.ConfigInfo;
import com.cc.test.model.MethodInfo;
import com.cc.test.model.ResultInfo;
import com.cc.test.service.MainService;
import com.cc.test.service.YapiService;
import com.cc.test.util.ApiUtils;
import com.cc.test.util.ArrayUtils;
import com.cc.test.web.api.MethodBody;
import com.cc.test.web.api.MethodDocData;
import com.cc.test.web.api.item.MenuVO;

/**
 * @author: chenyong
 * @description:
 * @create: 2020-03-26 14:19
 **/
@Controller
public class MainController {

	@Autowired
	private MainService service;

	@Autowired
	private YapiService yapi;

	@GetMapping("index")
	public String index() {
		return "main.html";
	}

	@GetMapping("getConfigs")
	@ResponseBody
	public Object getConfigs() {
		ConfigInfo info = new ConfigInfo();
		info.setEffect(service.getEffect());
		info.setOftenSize(service.getOftenSize());
		info.setRpcUrl(service.getHost());
		info.setTestLog(ArrayUtils.joinString(System.getProperty("user.home"), File.separator, "db", File.separator, "test_web"));
		info.setIfaceList(service.getInterfaces());
		info.setPath(service.getPath().replace('\\', '/'));
		info.setPathList(service.getPathList());

		return ResultInfo.success(info);
	}

	@GetMapping("getAll")
	@ResponseBody
	public Object getAll() {
		List<ClassInfo> list = new ArrayList<>(service.getClassInfoList());
		ResultInfo result = ResultInfo.success(list);
		result.attach("effect", service.getEffect());

		return result;
	}

	@GetMapping("getOften")
	@ResponseBody
	public Object getOften() {
		int size = service.getOftenSize(); // 常用类数量
		List<ClassInfo> list = new ArrayList<>(service.getClassInfoList());
		list = list.stream().filter(e -> e.getAccess() > 0).sorted((a, b) -> b.getAccess() - a.getAccess()).limit(size)
				.collect(Collectors.toList());

		return ResultInfo.success(list);
	}

	@GetMapping("removeOften")
	@ResponseBody
	public Object removeOften(@RequestParam String className) {
		if (ArrayUtils.isNotEmpty(className)) {
			service.getClassInfoList().stream().filter(e -> e.getName().equals(className)).forEach(e -> e.clearAccess());
		}

		return ResultInfo.success();
	}

	@PostMapping("removePath")
	@ResponseBody
	public Object removePath(@RequestParam String path) {
		service.updatePathList(path, 2);
		return ResultInfo.success();
	}

	/**
	 * 清除样本数据，采用默认数据
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	@GetMapping("doClear")
	public String doClear(@RequestParam String className, @RequestParam String methodName) {
		service.resetSample(className, methodName);
		return "forward:getMethodArgs";
	}

	@PostMapping("updateEffect")
	@ResponseBody
	public Object updateEffect(Boolean effect) {
		service.setEffect(effect);
		return ResultInfo.success();
	}
	
	@PostMapping("updatePreMode")
	@ResponseBody
	public Object updatePreMode(Boolean preMode) {
		service.setPreMode(preMode);
		String path = service.getHost();
		return ResultInfo.success(path);
	}

	@PostMapping("updateOftenSize")
	@ResponseBody
	private Object updateOftenSize(@RequestParam Integer oftenSize) {
		service.setOftenSize(oftenSize);
		return ResultInfo.success(oftenSize);
	}

	@RequestMapping("loadIface")
	@ResponseBody
	public Object loadIface(@RequestParam String path) {
		File file = new File(path);
		if (!file.exists())
			return ResultInfo.fail("文件路径非法，请选择facade目录");

		path = service.loadIface(file.getAbsolutePath());
		if (path == null)
			return ResultInfo.fail("文件路径非法，请选择facade目录");

		Map<String, Object> map = new HashMap<>();
		map.put("path", path);
		map.put("pathList", service.getPathList());
		map.put("ifaceList", service.getInterfaces());
		map.put("host", service.getHost());

		return ResultInfo.success(map);
	}

	@PostMapping("updateRpcUrl")
	@ResponseBody
	private Object updateRpcUrl(@RequestParam String url) {
		url = url.toLowerCase().trim();
		if (url.startsWith("http://")) {
			url = url.substring("http://".length());
		}

		if (!url.contains("/") && url.lastIndexOf(':') < 0) {
			url += ":80";
		}

		service.setRpcUrl(url);

		return ResultInfo.success(url);
	}

	@PostMapping("updateUserToken")
	@ResponseBody
	private Object updateUserToken(@RequestParam String token) {
		service.setToken(token);
		return ResultInfo.success(token);
	}

	@GetMapping("getMethodArgs")
	@ResponseBody
	public Object getMethodArgs(@RequestParam String className, @RequestParam String methodName) {
		if (ArrayUtils.isEmpty(className) || ArrayUtils.isEmpty(methodName))
			return ResultInfo.success();

		MethodInfo methodInfo = service.getMethodInfo(className, methodName);
		if (methodInfo == null)
			return ResultInfo.failedLoad();

		Object model = methodInfo.getSample(); // 优先取上一个测试数据
		if (model == null) {
			model = service.getLastSample(methodInfo); // 取数据库中最后一次保存的测试样本
			if (model == null)
				model = ApiUtils.getModel(methodInfo.getMethod()); // 构造默认数据
		}

		ResultInfo result = ResultInfo.success(model);
		result.attach("urlInfo", methodInfo.getUrl());
		result.attach("method", methodInfo.supportGet() ? "GET" : "POST");
		MethodBody doc = ApiUtils.getMethodData(methodInfo.getMethod());
		result.attach("comment", doc.getComment());

		return result;
	}

	/**
	 * 成功后，更新token
	 *
	 * @param className
	 * @param methodName
	 * @param token
	 * @return
	 */
	@GetMapping("getMethodDocs")
	@ResponseBody
	public Object getMethodDocs(String className, String methodName, String token) {
		if (ArrayUtils.isEmpty(className) || ArrayUtils.isEmpty(methodName))
			return ResultInfo.success();

		MethodInfo methodInfo = service.getMethodInfo(className, methodName);
		if (methodInfo == null)
			return ResultInfo.failedLoad();

		MethodBody doc = ApiUtils.getMethodData(methodInfo.getMethod());

		ResultInfo result = ResultInfo.success();
		result.setData(doc);
		result.attach("urlInfo", methodInfo.getUrl());
		result.attach("method", methodInfo.supportGet() ? "GET" : "POST");
		result.attach("comment", doc.getComment());

		if (ArrayUtils.isNotEmpty(token)) {
			service.setApiToken(token);
		}

		token = service.getApiToken();
		if (ArrayUtils.isNotEmpty(token)) {
			List<MenuVO> menus = yapi.getCates(token);
			result.attach("menus", menus);
			result.attach("token", service.getApiToken());
			if (menus.size() > 0 && methodInfo != null) {
				Integer[] apiInfo = yapi.getApiId(service.getApiToken(), methodInfo.getUrl());
				if (apiInfo.length == 2) {
					result.attach("cateId", apiInfo[0]);
					result.attach("apiId", apiInfo[1]);
				}
			}
		}

		return result;
	}

	@GetMapping("getApis")
	@ResponseBody
	public Object getApis(@RequestParam Integer catId, String className, String methodName) {
		List<MenuVO> apis = yapi.getMenus(service.getApiToken(), catId);
		ResultInfo result = ResultInfo.success(apis);

		if (ArrayUtils.isNotEmpty(className) && ArrayUtils.isNotEmpty(methodName)) {
			MethodInfo method = service.getMethodInfo(className, methodName);
			String path = method.getUrl();

			MenuVO menu = apis.stream().filter(e -> e.getPath() != null && e.getPath().equalsIgnoreCase(path)).findAny().orElse(null);
			if (menu != null)
				result.attach("apiId", menu.getId());
		}

		return result;
	}

	@PostMapping("addCate")
	@ResponseBody
	public Object addCate(@RequestParam String name, String desc) {
		JSONObject result = yapi.addCate(service.getApiToken(), name.trim(), desc);
		if (result.getInteger("errcode") != 0) {
			String error = result.getString("errmsg");
			return ResultInfo.fail("新增分类失败: " + error);
		}

		JSONObject value = result.getJSONObject("data");
		Integer id = value.getInteger("_id");

		MenuVO menu = new MenuVO();
		menu.setId(id);
		menu.setName(name.trim());

		return ResultInfo.success(menu);
	}

	@PostMapping("addApi")
	@ResponseBody
	public Object addApi(@RequestBody MethodDocData data) {
		String className = data.getClassName();
		String methodName = data.getMethodName();
		MethodInfo methodInfo = service.getMethodInfo(className, methodName);
		if (methodInfo == null)
			return ResultInfo.failedLoad();

		Integer catId = data.getCatId();
		String name = data.getName();
		String doc = ApiUtils.trimJson(data.getDoc());
		JSONObject json = JSON.parseObject(doc);
		String path = methodInfo.getUrl();

		JSONObject result = yapi.addApi(service.getApiToken(), catId, name, methodInfo, json);
		if (result.getInteger("errcode") != 0) {
			String error = result.getString("errmsg");
			return ResultInfo.fail("新增接口失败: " + error);
		}

		JSONObject value = result.getJSONObject("data");
		Integer id = value.getInteger("_id");

		MenuVO menu = new MenuVO();
		menu.setId(id);
		menu.setName(name.trim());
		menu.setPath(path);

		return ResultInfo.success(menu);
	}

	@RequestMapping("doTest")
	@ResponseBody
	public Object doTest(@RequestParam String className, @RequestParam String methodName, @RequestParam String args) {
		MethodInfo methodInfo = service.getMethodInfo(className, methodName);
		if (methodInfo == null)
			return ResultInfo.failedLoad();

		String data = ApiUtils.trimJson(args);
		JSONObject json = JSONObject.parseObject(data);
		methodInfo.setSample(ApiUtils.sortJson(json));

		service.getClassInfoList().stream().filter(e -> e.getName().equals(className)).findAny().get().addAccess();
		try {
			long start = System.currentTimeMillis();
			String host = service.getHost();
			ResultInfo r = service.rpcCall(host, methodInfo, json);
			long time = System.currentTimeMillis() - start;
			r.setCostTime(time);

			if (r.getConnectionError() == null || !r.getConnectionError()) { // 非连接错误保存测试用例
				service.saveTestData(host, methodInfo, r);
			}

			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return ResultInfo.fail("request exception").setException(ArrayUtils.getStackTrace(e));
		}
	}

	/**
	 * 自动测试
	 * 
	 * @param type
	 *            用例类型
	 * @return
	 */
	@RequestMapping("autoTest")
	@ResponseBody
	public Object autoTest(@RequestParam("type") Integer type) {
		service.testAll(type);
		return ResultInfo.success();
	}
}
