package com.cc;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
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

	public Config(String args) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(args);) {
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
		path = properties.getProperty("filePath");
		if (path == null)
			path = "user.log";
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
