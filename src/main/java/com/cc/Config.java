package com.cc;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Config {

	private Set<String> includes;
	private Set<String> excludes;

	private Set<String> excludeClass;
	private Set<String> excludeMethod;

	private boolean showEntry;
	private boolean showGetter;

	private Set<String> imports;

	/**
	 * 显示方法定义而非参数值，默认false
	 */
	private boolean showMethodInfo;

	/**
	 * 显示入参类型而非toString
	 */
	private boolean showParamType;

	/**
	 * 相同方法出现次数
	 */
	private int maxCount;

	/**
	 * 最大深度
	 */
	private int maxDepth;

	private boolean consoleLog;

	private String indent;

	private String path;

	/**
	 * 重复出现的方法与次数
	 */
	private Map<String, AtomicInteger> countMap = new HashMap<>();

	/**
	 * 循环调用方法记录，循环方法不再记录日志
	 */
	private Set<String> loopMethods = new HashSet<>();

	private WatchService watcher;

	public Config(String args) {
		File file = new File(args);
		init(file);
		initListener(file);
	}

	/**
	 * @param file
	 */
	private void init(File file) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(file)) {
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}

		includes = Utils.splitString(properties.getProperty("include"));
		excludes = Utils.splitString(properties.getProperty("exclude"));

		Set<String> packages = excludes.stream().filter(e -> !e.contains(".")).collect(Collectors.toSet());
		excludes.removeAll(packages);
		for (String e : includes) {
			for (String p : packages) {
				excludes.add(e + "." + p);
			}
		}

		excludeClass = Utils.splitString(properties.getProperty("excludeClass"));
		excludeMethod = Utils.splitString(properties.getProperty("excludeMethod"));

		String value = properties.getProperty("showEntry"); // 是否显示方法进入
		showEntry = Boolean.valueOf(value);

		value = properties.getProperty("showGetter");
		showGetter = Boolean.valueOf(value);

		value = properties.getProperty("maxCount", "50");
		maxCount = Integer.parseInt(value);

		value = properties.getProperty("maxDepth", "50");
		maxDepth = Integer.parseInt(value);

		value = properties.getProperty("consoleLog"); // 是否输出控制台日志
		consoleLog = value == null ? true : Boolean.valueOf(value);

		value = properties.getProperty("showMethodInfo");
		showMethodInfo = Boolean.valueOf(value);

		value = properties.getProperty("showParamType");
		showParamType = Boolean.valueOf(value);

		imports = Utils.splitString(properties.getProperty("imports"));

		indent = properties.getProperty("indent");
		path = properties.getProperty("filePath", "user.log");
	}

	private void initListener(File file) {
		try {
			watcher = FileSystems.getDefault().newWatchService();
			Path path = file.toPath();
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

			while (true) {
				WatchKey key = watcher.take();
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						init(file);
						break;
					}
				}

				if (!key.reset()) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean needTrace(Method method) {
		String methodName = method.getName();

		if (isBasicMethod(methodName) || loopMethods.contains(methodName))
			return false;

		if (!showGetter && method.getParameterTypes().length == 0 && (methodName.startsWith("get") || methodName.startsWith("is")))
			return false;

		String name = method.getDeclaringClass().getName();
		if (getExcludes().stream().anyMatch(e -> name.startsWith(e)))
			return false;

		int index = name.lastIndexOf('.');
		String Name = null;
		if (index > 0) {
			Name = name.substring(index + 1, name.length());
		}

		if (getExcludeClass().contains(Name))
			return false;

		if (excludeMethod.contains(methodName) || excludeMethod.contains(Name + "." + methodName))
			return false;

		String key = Name + "." + methodName;
		countMap.putIfAbsent(key, new AtomicInteger());
		int count = countMap.get(key).get();
		if (count > maxCount) {
			System.out.println(key + ": count=" + count);
			return false;
		}

		return includes.stream().anyMatch(e -> name.startsWith(e));
	}

	private boolean isBasicMethod(String methodName) {
		return Utils.isContain(methodName, "toString", "hashCode", "equals", "wait", "clone");
	}

	public void addLoopMethod(String methodName) {
		loopMethods.add(methodName);
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public Set<String> getExcludes() {
		return excludes;
	}

	public Set<String> getExcludeClass() {
		return excludeClass;
	}

	public Set<String> getExcludeMethod() {
		return excludeMethod;
	}

	public boolean isShowEntry() {
		return showEntry;
	}

	public boolean isShowGetter() {
		return showGetter;
	}

	public Set<String> getImports() {
		return imports;
	}

	public boolean isShowMethodInfo() {
		return showMethodInfo;
	}

	public boolean isShowParamType() {
		return showParamType;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public boolean isConsoleLog() {
		return consoleLog;
	}

	public String getIndent() {
		return indent;
	}

	public String getPath() {
		return path;
	}
}
