package com.cc.test.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cc.test.model.ClassInfo;
import com.cc.test.model.ConfigData;
import com.cc.test.model.Message;
import com.cc.test.model.MethodInfo;
import com.cc.test.model.ResultInfo;
import com.cc.test.model.TestData;
import com.cc.test.task.TaskManager;
import com.cc.test.util.ApiUtils;
import com.cc.test.util.ArrayUtils;
import com.cc.test.util.HttpUtils;
import com.cc.test.web.WebConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyong
 * @date 2018年11月22日
 */
@Service
@Slf4j
public class MainService {

	private final Map<String, Class<?>> interfaces = new TreeMap<>();

	private final Map<String, MethodInfo> methods = new TreeMap<>();

	private final List<ClassInfo> classInfoList = new ArrayList<>();

	private List<String> pathList = new ArrayList<>();

	private ConfigData configData;

	private final Properties p = new Properties();

	@Autowired
	private TestDataService tdService;

	@Autowired
	private ConfigService configService;

	@Autowired
	private WebSocketServer wsServer;

	@Autowired
	private WebConfig config;

	/**
	 * 临时rpc地址
	 */
	private String rpcUrl;

	@PostConstruct
	private void init() {
		ApiUtils.config = config;

		configData = configService.getConfigData();
		if (configData == null) {
			configData = new ConfigData();
			configData.setEffect(0);
			configData.setOftenSize(5);
		}

		loadProperties();

		loadIface(getPath());
	}

	private void loadProperties() {
		File file = new File(System.getProperty("user.home"), "test.properties");
		if (!file.exists())
			return;

		try (InputStream in = new FileInputStream(file)) {
			p.load(in);

			String value = p.getProperty("pathList");
			pathList = ArrayUtils.splitToString(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveProperties() {
		File file = new File(System.getProperty("user.home"), "test.properties");
		try (FileWriter fw = new FileWriter(file)) {
			p.store(fw, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String loadIface(String path) {
		if (!path.contains("facade")) {
			File parent = new File(path);
			if (!parent.exists())
				return null;

			for (File file : parent.listFiles()) {
				if (file.isDirectory() && file.getName().toLowerCase().endsWith("facade")) {
					path = file.getAbsolutePath();
					break;
				}
			}
		}

		if (!path.contains("facade"))
			return null;

		path = updatePathList(path, 1);

		if (ArrayUtils.isNotEquals(path, configData.getFacadePath())) { // 当服务变更时，清除临时path
			rpcUrl = null;
		}

		classInfoList.clear();
		interfaces.clear();
		methods.clear();

		readCommonClass(new File(path));

		readClass(new File(path + "/target/classes"));

		classInfoList.sort(Comparator.comparing(ClassInfo::getName));

		configData.setFacadePath(path);
		configService.updateById(configData);
		ApiUtils.sourcePath = path + "/src/main/java/";

		return path;
	}

	/**
	 * 是否启用预发访问模式，默认测试模式
	 */
	public void setPreMode(Boolean preMode) {
		p.setProperty("preMode", String.valueOf(preMode));
		saveProperties();
	}

	public boolean isPreMode() {
		return Boolean.valueOf(p.getProperty("preMode", "false"));
	}

	/**
	 * @param path
	 * @param type
	 *            1=add, 2=delete
	 * @return
	 */
	public String updatePathList(String path, int type) {
		if (path.endsWith("/") || path.endsWith("\\"))
			path = path.substring(0, path.length() - 1);

		path = path.replaceAll("\\\\", "/");

		if (type == 1 && !pathList.contains(path)) {
			pathList.add(path);
			Collections.sort(pathList);

			p.setProperty("pathList", ArrayUtils.toString(pathList));
			saveProperties();
		} else if (type == 2 && pathList.contains(path)) {
			pathList.remove(path);
			saveProperties();
		}

		rpcUrl = null;

		return path;
	}

	public Object getLastSample(MethodInfo methodInfo) {
		QueryWrapper<TestData> c = new QueryWrapper<>();
		c.eq("class_name", methodInfo.getMethod().getDeclaringClass().getSimpleName());
		c.eq("method_name", methodInfo.getMethod().getName());
		c.eq("status", 1);
		c.orderByDesc("id");

		TestData data = tdService.getOne(c);
		return data == null || ArrayUtils.isEmpty(data.getArgs()) ? null : JSON.parseObject(data.getArgs());
	}

	private void readClass(File parent) {
		String filePath = parent.getPath();
		int clazzPathLen = filePath.length() + 1;

		if (parent.exists() && parent.isDirectory()) {
			try {
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				method.invoke(classLoader, parent.toURI().toURL());
			} catch (Exception e) {
				e.printStackTrace();
			}

			Stack<File> stack = new Stack<>();
			stack.push(parent);

			while (!stack.isEmpty()) {
				File path = stack.pop();
				File[] classFiles = path
						.listFiles(f -> f.getPath().contains("com") && (f.isDirectory() || f.getName().endsWith("Facade.class")));
				for (File subFile : classFiles) {
					if (subFile.isDirectory()) {
						stack.push(subFile);
						continue;
					}

					String className = subFile.getAbsolutePath();
					if (!className.endsWith(".class"))
						continue;

					className = className.substring(clazzPathLen, className.length() - 6);
					className = className.replace(File.separatorChar, '.');

					try {
						Class<?> clz = Class.forName(className);
						interfaces.put(clz.getSimpleName(), clz);
						ClassInfo node = getMethods(clz);
						classInfoList.add(node);
					} catch (Exception e) {
						System.err.println("error class: " + className);
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 仅加载枚举/pojo
	 * <p>
	 * common class load
	 *
	 * @param facade
	 */
	private void readCommonClass(File facade) {
		File project = facade.getParentFile();
		String name = facade.getName();
		File common = new File(project, name.substring(0, name.indexOf("-")) + "-common");
		if (!common.exists())
			return;

		common = new File(common, "/target/classes");
		try {
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			method.invoke(classLoader, common.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String filePath = common.getPath();
		int clazzPathLen = filePath.length() + 1;

		Stack<File> stack = new Stack<>();
		stack.push(common);

		while (!stack.isEmpty()) {
			File path = stack.pop();
			File[] classFiles = path.listFiles(f -> f.getPath().contains("com") && !f.getPath().contains("META-INF"));
			for (File subFile : classFiles) {
				if (subFile.isDirectory()) {
					stack.push(subFile);
					continue;
				}

				String className = subFile.getAbsolutePath();
				if (!className.endsWith(".class") || className.contains("util"))
					continue;

				className = className.substring(clazzPathLen, className.length() - 6);
				className = className.replace(File.separatorChar, '.');

				try {
					Class.forName(className);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private ClassInfo getMethods(Class<?> clz) {
		ClassInfo info = new ClassInfo(clz.getSimpleName());
		for (Method m : clz.getMethods()) {
			if (m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(GetMapping.class)
					|| m.isAnnotationPresent(RequestMapping.class)) {

				String name = makeMethodName(clz, m);
				methods.putIfAbsent(clz.getName() + "." + name, new MethodInfo(m, name));
				info.addChild(name);
			}
		}

		return info;
	}

	/**
	 * 避免方法重载问题
	 *
	 * @param clz
	 * @param m
	 * @return
	 */
	private String makeMethodName(Class<?> clz, Method m) {
		String clzName = clz.getName() + ".";
		String name = m.getName();
		int i = 1;
		while (methods.containsKey(clzName + name)) {
			name = m.getName() + "-" + i++;
		}

		return name;
	}

	public ResultInfo rpcCall(String host, MethodInfo methodInfo, JSONObject args) {
		String url = host;
		if (!url.startsWith("http")) {
			url = "http://" + host;
		}

		if (!url.endsWith("/"))
			url += "/";

		String methodUrl = methodInfo.getUrl();
		if (methodUrl.startsWith("/"))
			methodUrl = methodUrl.substring(1);

		url = url + methodUrl;
		Map<String, String> header = new HashMap<>();
		if (ArrayUtils.isEmpty(args.getString("userId")) && ArrayUtils.isNotEmpty(configData.getToken())) {
			header.put("token", configData.getToken());
		}

		try {
			String response = null;
			if (methodInfo.supportGet()) {
				response = HttpUtils.doGet(url, header, args);
			} else if (methodInfo.supportPost()) {
				if (methodInfo.isJsonType()) {
					String key = args.keySet().iterator().next();
					response = HttpUtils.postJson(url, header, args.getJSONObject(key));
				} else {
					response = HttpUtils.doPost(url, header, args);
				}
			}

			if (response != null && response.trim().startsWith("{")) {
				Object result = JSON.parseObject(response, methodInfo.getReturnType());
				return ResultInfo.makeResult(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResultInfo.fail("RPC ERROR").setConnectionError(true).setException(ArrayUtils.getStackTrace(e));
		}

		return ResultInfo.fail("RPC ERROR: 接口服务异常");
	}

	/**
	 * 取出测试数据，逐一再跑，跳过预发环境数据
	 */
	public void testAll(Integer status) {
		String host = getHost();
		TaskManager.submit(() -> {
			List<TestData> list = tdService.listTestData(getProject(), getHost(), status);

			for (TestData e : list) {
				Message message = new Message(e);
				try {
					MethodInfo methodInfo = getMethodInfo(e.getClassName(), e.getMethodName());
					if (methodInfo == null) { // 方法不存在时，例如删除，跳过处理
						log.info("method = {} not exists", e.getClassName() + "." + e.getMethodName());
						continue;
					}

					JSONObject args = JSON.parseObject(e.getArgs());
					ResultInfo result = rpcCall(host, methodInfo, args);

					if (result.isSuccess()) {
						tdService.addOkCount(e.getId());
					} else {
						tdService.addErrorCount(e.getId());
					}

					message.setStatus(result.isSuccess());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				wsServer.sendMessage(message.getInfo(status == 1));
			}

			wsServer.sendMessage("----- Finished to test all, count = " + list.size() + ".");
		});
	}

	public MethodInfo getMethodInfo(String className, String methodName) {
		Class<?> clz = interfaces.get(className);
		if (clz == null)
			return null;

		String accessKey = clz.getName() + "." + methodName;
		return methods.get(accessKey);
	}

	public void resetSample(String className, String methodName) {
		MethodInfo method = getMethodInfo(className, methodName);
		if (method != null)
			method.setSample(ApiUtils.getModel(method.getMethod()));
	}

	public Map<String, Class<?>> getInterfaces() {
		return new HashMap<>(interfaces);
	}

	public List<ClassInfo> getClassInfoList() {
		return new ArrayList<>(classInfoList);
	}

	private String getProject() {
		String path = configData.getFacadePath();
		int index = path.lastIndexOf(File.separator);
		if (index < 0 && !"/".equals(File.separator)) {
			index = path.lastIndexOf("/");
		}

		int endIndex = path.lastIndexOf("-");
		if (endIndex < 0)
			endIndex = path.length();

		String project = index > 0 ? path.substring(index + 1, endIndex) : "unknown";
		return project;
	}

	/**
	 * @return
	 */
	public String getHost() {
		if (isPreMode())
			return config.getPreUrl() + getProject().replace("-", "");

		if (ArrayUtils.isNotEmpty(rpcUrl))
			return rpcUrl;

		TestData td = tdService.getLatestTestData(getProject());
		if (td != null && ArrayUtils.isNotEmpty(td.getHost())) {
			return td.getHost();
		}

		return rpcUrl;
	}

	/**
	 * 记录测试数据
	 *
	 * @param methodInfo
	 * @param result
	 */
	public void saveTestData(String host, MethodInfo methodInfo, ResultInfo result) {
		TestData td = new TestData();
		td.setProject(getProject());
		td.setHost(host);
		td.setClassName(methodInfo.getMethod().getDeclaringClass().getSimpleName());
		td.setMethodName(methodInfo.getName());
		td.setArgs(JSON.toJSONString(methodInfo.getSample()));
		td.setResult(JSON.toJSONString(result));
		td.setDuration(result.getCostTime());
		td.setStatus(result.isSuccess() ? 1 : 0);
		td.setUrl(methodInfo.getUrl());

		tdService.addTestData(td);
	}

	///////////////////// configs
	public void saveConfigs() {
		configService.updateConfig(configData);
	}

	public boolean getEffect() {
		return Objects.equals(configData.getEffect(), 1);
	}

	public Integer getOftenSize() {
		Integer size = configData.getOftenSize();
		return size == null ? 5 : size;
	}

	public String getPath() {
		return configData.getFacadePath();
	}

	public List<String> getPathList() {
		return pathList;
	}

	public String getApiToken() {
		return configData.getApiToken();
	}

	public void setEffect(Boolean effect) {
		configData.setEffect(Objects.equals(effect, true) ? 1 : 0);
		saveConfigs();
	}

	public void setOftenSize(Integer oftenSize) {
		if (oftenSize == null || oftenSize < 1)
			return;

		configData.setOftenSize(oftenSize);
		saveConfigs();
	}

	public void setRpcUrl(String url) {
		if (ArrayUtils.isEmpty(url))
			return;

		this.rpcUrl = url;
	}

	public void setToken(String token) {
		configData.setToken(token);
		saveConfigs();
	}

	public void setApiToken(String token) {
		configData.setApiToken(token);
		saveConfigs();
	}
}
