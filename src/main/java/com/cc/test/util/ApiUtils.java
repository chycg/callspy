package com.cc.test.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.cc.test.web.WebConfig;
import com.cc.test.web.api.MethodBody;
import com.cc.test.web.api.item.ArrayData;
import com.cc.test.web.api.item.FieldData;
import com.cc.test.web.api.item.FormVO;
import com.cc.test.web.api.item.ObjectData;

public class ApiUtils {

	public static String sourcePath;

	public static WebConfig config;

	private static Map<Class<?>, TypeDeclaration> typeMap = new HashMap<>();

	/**
	 * 入参默认数据
	 *
	 * @param method
	 * @return
	 */
	public static Object getModel(Method method) {
		Map<String, Object> model = new TreeMap<>();
		for (Parameter p : method.getParameters()) {
			String name = p.getName();

			RequestParam rp = p.getAnnotation(RequestParam.class);
			if (rp != null) {
				String alias = rp.value();
				if (ArrayUtils.isEmpty(alias)) {
					alias = rp.name();
				}

				if (ArrayUtils.isNotEmpty(alias) && !alias.equals(name))
					name = alias;
			}

			Class<?> clz = p.getType(); // 参数类型
			Type actualType = ApiUtils.getActualType(p);

			Object value = null;
			if (!clz.isEnum()) {
				value = getDefaultValue(clz, actualType, null); // 参数类型，泛型类型
			}

			if (value instanceof JSONObject) {
				JSONObject json = (JSONObject) value;
				value = new TreeMap<>(json);
			}

			model.put(name, value);
		}

		return model;
	}

	public static Object getDefaultValue(Class<?> clz, Type type, ReferenceNode node) {
		return getDefaultValue(clz, type, node, true);
	}

	/**
	 * 返回默认值
	 *
	 * @param clz
	 *            本次需要返回的对象类型
	 * 
	 * @param type
	 *            泛型类型
	 * 
	 * @param node
	 *            引用链
	 * 
	 * @param fillMode
	 *            填充模式，true时填充对象数据， 循环引用时为false，下层不再填充
	 * @return
	 */
	public static Object getDefaultValue(Class<?> clz, Type type, ReferenceNode node, boolean fillMode) {
		Object value = null;

		if (List.class.isAssignableFrom(clz)) {
			value = new ArrayList<>();
			if (fillMode)
				initCollection((List<Object>) value, type, node);
		} else if (Set.class.isAssignableFrom(clz)) {
			value = new HashSet<>();
			if (fillMode)
				initCollection((Set<Object>) value, type, node);
		} else if (clz.isArray()) {
			Class<?> componentType = clz.getComponentType();
			List<Object> list = new ArrayList<>();

			if (fillMode)
				initCollection(list, componentType, node);

			value = list.toArray();
		} else if (clz == String.class) {
			value = "";
		} else if (clz == int.class || clz == Integer.class || clz == short.class || clz == byte.class) {
			value = 0;
		} else if (clz == long.class || clz == Long.class) {
			value = 0L;
		} else if (clz == float.class || clz == Float.class) {
			value = 0f;
		} else if (clz == double.class || clz == Double.class) {
			value = 0.0;
		} else if (clz == boolean.class || clz == Boolean.class) {
			value = false;
		} else if (clz == Date.class) {
			value = new Date();
		} else if (clz == LocalDate.class) {
			value = LocalDate.now();
		} else if (clz == LocalDateTime.class) {
			value = LocalDateTime.now();
		} else if (clz == BigDecimal.class) {
			value = new BigDecimal(0);
		} else if (clz == BigInteger.class) {
			value = new BigInteger("0");
		} else if (clz == JSONObject.class) {
			value = new JSONObject();
		} else if (Map.class.isAssignableFrom(clz)) {
			value = new HashMap<>();
		} else { // 认为是普通对象
			value = ApiUtils.makeObject(clz);
			if (fillMode)
				setDefaultValue(value, node); // 设置对象默认值
		}

		return value;
	}

	public static void initCollection(Collection<Object> c, Type actualType, ReferenceNode node) {
		if (actualType == null) // 未知类型不填充集合
			return;

		String name = actualType.getTypeName();
		if (config.isPrefixed(name)) {
			Object o = ApiUtils.makeObject(name);
			setDefaultValue(o, node);
			c.add(o);
		} else if (actualType instanceof Class) {
			c.add(getDefaultValue((Class<?>) actualType, null, node));
		}
	}

	/**
	 * 为某个对象设置默认值
	 * 
	 * 基础类型不处理
	 *
	 * @param o
	 * @param node
	 */
	public static void setDefaultValue(Object o, ReferenceNode node) {
		if (ApiUtils.isSingle(o.getClass()))
			return;

		Class<?> tmp = o.getClass();
		if (!config.isPrefixed(tmp.getName()))
			return;

		boolean mapObject = o instanceof Map;
		while (tmp != null && tmp != Object.class) {
			Field[] fields = tmp.getDeclaredFields();
			for (Field f : fields) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isPublic(mod))
					continue;

				Class<?> type = f.getType(); // 字段类型
				Type actualType = ApiUtils.getActualType(f);

				Class<?> target = type;
				if (actualType != null) {
					if (actualType.getTypeName().length() == 1)
						target = Object.class;
					else
						target = (Class<?>) actualType;
				}

				boolean hasRing = false;
				ReferenceNode child = null;
				if (ApiUtils.isObject(target)) { // 属性为对象类型时，检测循环依赖
					child = node == null ? new ReferenceNode(f) : node.addChild(f);

					hasRing = child.hasRing(); // 引用链向上查找有引用环存在，不再赋值
				}

				Object value = getDefaultValue(type, actualType, child, !hasRing);
				if (mapObject)
					((Map<String, Object>) o).put(f.getName(), value);
				else
					ApiUtils.setValue(o, f, value);
			}

			tmp = tmp.getSuperclass();
		}
	}

	/**
	 * method body, req and res
	 *
	 * @param method
	 * @return
	 */
	public static MethodBody getMethodData(Method method) {
		MethodBody body = new MethodBody();
		if (method == null)
			return body;

		Class<?> clz = method.getDeclaringClass();
		TypeDeclaration type = getTypeDeclaration(clz); // source type

		if (type != null) {
			for (MethodDeclaration fd : type.getMethods()) {
				String name = fd.getName().getIdentifier();
				if (name.equals(method.getName())) {
					readMethodComment(method, fd, body);
					break;
				}
			}
		}

		return body;
	}

	private static void readMethodComment(Method method, MethodDeclaration md, MethodBody body) {
		Javadoc doc = md.getJavadoc();
		Map<String, String> map = new HashMap<>(); // code comment
		if (doc != null) {
			List<?> tags = doc.tags();
			for (Object tag : tags) {
				TagElement te = (TagElement) tag;
				List<?> fragments = te.fragments();

				String tagName = te.getTagName();
				if (tagName == null) {
					body.setComment(fragments.get(0).toString().trim());
					continue;
				}

				if (TagElement.TAG_PARAM.equalsIgnoreCase(tagName) && fragments.size() > 0) {
					String name = fragments.get(0).toString();
					map.put(name, fragments.size() > 1 ? fragments.get(1).toString() : name);
				}
			}
		}

		for (Parameter p : method.getParameters()) {
			Class<?> type = p.getType();
			if (ArrayUtils.isContain(type, HttpServletRequest.class, HttpServletResponse.class, MultipartFile.class))
				continue;

			String desc = map.getOrDefault(p.getName(), p.getName()).trim();
			if (type.isArray()) { // 数组，原始类型或对象类型，认为是json提交
				Class<?> componentType = type.getComponentType(); // 元素类型
				ArrayData array = new ArrayData();
				ObjectData data = makeBodyData(componentType, desc, new HashSet<>());
				array.setItems(data);

				body.setRequest(array);
			} else if (ApiUtils.isCollection(type)) {
				Class<?> componentType = Object.class;
				Type pType = p.getParameterizedType();
				if (pType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) pType;
					componentType = (Class<?>) pt.getActualTypeArguments()[0];
				}

				ArrayData array = new ArrayData();
				ObjectData data = makeBodyData(componentType, desc, new HashSet<>());
				array.setItems(data);

				body.setRequest(array);
			} else {
				if (ApiUtils.isSingle(type)) { // 非组合型数据，认为是表单提交
					int required = 1;
					RequestParam rp = p.getAnnotation(RequestParam.class);
					if (rp == null || !rp.required()) // 无注解或非必需时
						required = 0;

					FormVO data = new FormVO();
					data.setDesc(desc);
					data.setRequired(required);
					data.setName(p.getName());
					data.setExample(getDefaultValue(type, null, null));

					body.addFormData(data);
				} else if (ApiUtils.isObject(type)) { // 组合型数据，一般就一个对象
					// if (p.getAnnotation(RequestBody.class) == null) // 对象类型必须带注解
					// continue;

					ObjectData data = makeBodyData(type, desc, new HashSet<>());
					body.setRequest(data);
				}
			}
		}

		Class<?> clz = method.getReturnType();
		if (Result.class.isAssignableFrom(clz) || BaseResult.class.isAssignableFrom(clz)) {
			Type returnType = method.getGenericReturnType();
			Type type0;
			if (returnType instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
				Type[] types = type.getActualTypeArguments();
				type0 = types.length > 0 ? types[0] : Object.class;

				if (type.getRawType() == PageResult.class) { // 将泛型转换为List<T>
					type0 = new ParameterizedTypeImpl(new Type[] { type0 }, null, List.class);
				}
			} else {
				type0 = returnType;
			}

			ObjectData data = makeResponse(clz, type0);
			body.setResponse(data);
		}
	}

	private static TypeDeclaration getTypeDeclaration(Class<?> clz) {
		if (!config.isPrefixed(clz.getName()))
			return null;

		if (!typeMap.containsKey(clz)) {
			String filePath = sourcePath + clz.getName().replace('.', '/') + ".java";
			File file = new File(filePath);
			if (!file.exists())
				return null;

			byte[] input;
			try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {
				input = new byte[br.available()];
				br.read(input);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			ASTParser astParser = ASTParser.newParser(AST.JLS13);
			astParser.setSource(new String(input).toCharArray());
			astParser.setKind(ASTParser.K_COMPILATION_UNIT);

			CompilationUnit result = (CompilationUnit) astParser.createAST(null);
			TypeDeclaration type = (TypeDeclaration) result.types().get(0);

			typeMap.put(clz, type);
		}

		return typeMap.get(clz);
	}

	/**
	 * 返回体
	 *
	 * @param resultType
	 * @param genericType`
	 */
	public static ObjectData makeResponse(Class<?> resultType, Type genericType) {
		ObjectData data = new ObjectData();
		data.setTitle("empty object");
		data.setProperties(new LinkedHashMap<>());
		data.setRequired(new ArrayList<>());

		String comment = "";

		Class<?> tmp = resultType;

		while (tmp != Object.class) {
			for (Field field : tmp.getDeclaredFields()) {
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod))
					continue;

				String name = field.getName();
				if (ArrayUtils.isContain(name, "offset"))
					continue;

				if (name.equalsIgnoreCase("success")) {
					comment = "是否成功";
				} else if (name.equalsIgnoreCase("code")) {
					comment = "错误码";
				} else if (name.equalsIgnoreCase("msg") || name.equalsIgnoreCase("message")) {
					comment = "错误提示";
				} else if (name.equalsIgnoreCase("totalRecords")) {
					comment = "总页数";
				} else if (name.equalsIgnoreCase("totalPages")) {
					comment = "总行数";
				} else if (name.equalsIgnoreCase("pageIndex")) {
					comment = "当前页";
				} else if (name.equalsIgnoreCase("pageSize")) {
					comment = "每页行数";
				} else if (name.equalsIgnoreCase("data") || name.equalsIgnoreCase("resultObject")) {
					comment = "data";
				}

				addProperty(data, field, comment, genericType, new HashSet<>());
			}

			tmp = tmp.getSuperclass();
		}

		return data;
	}

	/**
	 * 新增属性
	 *
	 * @param data
	 * @param field
	 * @param comment
	 * @param type
	 */
	public static void addProperty(ObjectData data, Field field, String comment, Type type, Set<Class<?>> set) {
		int mod = field.getModifiers();
		if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
			return;
		}

		String name = field.getName();
		Class<?> clz = field.getType();

		if (ApiUtils.isSingle(clz)) { // 普通的非组合数据类型，int,string,boolean
			FieldData fieldData = new FieldData(clz, comment);
			data.getProperties().put(name, fieldData);
			data.getRequired().add(name);
		} else if (ApiUtils.isObject(clz)) { // 复合结构
			if (clz == Object.class && type instanceof Class || clz != Object.class) { // data字段时，取实际类型遍历数据结构
				if (clz == Object.class)
					clz = (Class<?>) type;

				ObjectData jsonData = makeBodyData(clz, comment, set);
				data.getProperties().put(name, jsonData);

				set.remove(clz);
			} else if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				Type type0 = pt.getActualTypeArguments()[0];
				Type rawType = pt.getRawType();

				Class<?> elementClass = forName(type0.getTypeName());

				if (ApiUtils.isCollection((Class<?>) rawType)) {
					ArrayData arrayData = new ArrayData();
					arrayData.setItems(makeBodyData(elementClass, comment, set));
					data.getProperties().put(name, arrayData);
				} else if (rawType == PageQueryResult.class) { // PageQueryResult特殊处理
					ObjectData jsonData = makeBodyData(PageQueryResult.class, comment, set);
					ArrayData arrayData = new ArrayData();
					arrayData.setItems(makeBodyData(elementClass, "data", set));
					jsonData.getProperties().put("data", arrayData);

					data.getProperties().put(name, jsonData);
				}
			}
		} else if (ApiUtils.isCollection(clz) || clz.isArray()) { // collection
			Class<?> elementClass = (Class<?>) type;
			if (elementClass == null) {
				Type fieldType = field.getGenericType();
				if (fieldType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) fieldType;
					Type actualType = pt.getActualTypeArguments()[0];
					String typeName = actualType.getTypeName();

					if (typeName.length() == 1) { // 泛型
						elementClass = (Class<?>) field.getDeclaringClass().getGenericSuperclass();
					} else {
						elementClass = forName(typeName);
					}
				} else {
					elementClass = clz.getComponentType(); // array element
				}
			}

			ArrayData arrayData = new ArrayData();
			arrayData.setItems(makeBodyData(elementClass, comment, set));
			data.getProperties().put(name, arrayData);
		}
	}

	/**
	 * 此处只处理单个对象结构，不包含集合或数组类型
	 *
	 * @param clz
	 * @param description
	 */
	public static ObjectData makeBodyData(Class<?> clz, String description, Set<Class<?>> set) {
		ObjectData data = new ObjectData();
		data.setDescription(description);
		if (ApiUtils.isObject(clz)) {
			data.setType("object");
			data.setProperties(new LinkedHashMap<>());
			data.setRequired(new ArrayList<>());

			if (!set.contains(clz)) {
				set.add(clz);
				for (Field field : ApiUtils.getFields(clz)) {
					if (Modifier.isStatic(field.getModifiers()))
						continue;

					String comment = getFieldComment(clz, field.getName());
					addProperty(data, field, comment, null, set);
				}
			}

			set.remove(clz);
		} else { // primitive type
			data.setType(ApiUtils.getTypeInfo(clz));
		}

		return data;
	}

	/**
	 * 读取指定字段的注释
	 *
	 * @param clz
	 * @param fieldName
	 * @return
	 */
	private static String getFieldComment(Class<?> clz, String fieldName) {
		TypeDeclaration type = getTypeDeclaration(clz);
		if (type == null)
			return fieldName;

		for (FieldDeclaration fd : type.getFields()) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) fd.fragments().get(0);
			String name = vdf.getName().getIdentifier();
			if (name.equals(fieldName)) {
				String comment = ApiUtils.readFieldComment(fd);
				if (ArrayUtils.isNotEmpty(comment))
					return comment;

				break;
			}
		}

		return fieldName;
	}

	public static String getTypeInfo(Class<?> clz) {
		if (ArrayUtils.isContain(clz, int.class, Integer.class, long.class, Long.class) || clz.isEnum()) // 枚举认为是数字
			return "integer";

		if (ArrayUtils.isContain(clz, double.class, Double.class, float.class, Float.class))
			return "number";

		if (ArrayUtils.isContain(clz, String.class, Date.class, LocalDate.class, LocalDateTime.class))
			return "string";

		if (clz == boolean.class || clz == Boolean.class)
			return "boolean";

		if (Collection.class.isAssignableFrom(clz) || clz.isArray())
			return "array";

		return "object";
	}

	public static boolean isCollection(Class<?> clz) {
		return Collection.class.isAssignableFrom(clz);
	}

	public static boolean isSingle(Class<?> clz) {
		return clz.isPrimitive() || clz.isEnum() || ArrayUtils.isContain(clz, Integer.class, Long.class, Double.class, Float.class,
				Boolean.class, String.class, Date.class, BigDecimal.class, BigInteger.class);
	}

	public static boolean isObject(Class<?> clz) {
		return !isSingle(clz) && !isCollection(clz) && !clz.isArray();
	}

	public static Object makeObject(String name) {
		return makeObject(forName(name));
	}

	private static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Object makeObject(Class<?> type) {
		if (type.isInterface())
			return new HashMap<>();

		try {
			return type.newInstance();
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	/**
	 * 不覆盖默认值
	 * 
	 * @param o
	 * @param field
	 * @param value
	 */
	public static void setValue(Object o, Field field, Object value) {
		try {
			field.setAccessible(true);

			Object oldValue = field.get(o);
			if (oldValue == null)
				field.set(o, value);

			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数据排序
	 *
	 * @param json
	 * @return
	 */
	public static Map<String, Object> sortJson(Map<String, Object> json) {
		if (json == null)
			return null;

		for (Entry<String, Object> entry : json.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof Map) {
				Map<String, Object> result = sortJson((Map<String, Object>) v);
				entry.setValue(result);
			} else if (v instanceof List) {
				List<Object> list = (List<Object>) v;
				for (int i = 0; i < list.size(); i++) {
					Object value = list.get(i);
					if (value instanceof Map) {
						Map<String, Object> item = sortJson((Map<String, Object>) value);
						list.set(i, item);
					} else {
						list.set(i, value);
					}
				}
			}
		}

		Map<String, Object> map = new TreeMap<>((a, b) -> {
			if (a.equalsIgnoreCase("id"))
				return -1;

			if (b.equalsIgnoreCase("id"))
				return 1;

			return a.compareTo(b);
		});

		map.putAll(json);
		return map;
	}

	public static Type getActualType(Parameter p) {
		Type type = p.getParameterizedType(); // 参数泛型类型
		Type actualType = null;
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			actualType = pt.getActualTypeArguments()[0];
		}

		return actualType;
	}

	/**
	 * 取泛型
	 * 
	 * @param f
	 * @return
	 */
	public static Type getActualType(Field f) {
		Class<?> type = f.getType(); // 字段类型
		Type actualType = null;
		if (Collection.class.isAssignableFrom(type)) {
			Type genericType = f.getGenericType(); // 字段泛型类型
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;
				actualType = pt.getActualTypeArguments()[0];
			}
		}

		return actualType;
	}

	/**
	 * get fields with parents class fields
	 *
	 * @param clz
	 * @return
	 */
	public static List<Field> getFields(Class<?> clz) {
		List<Field> list = new LinkedList<>();

		Class<?> tmp = clz;
		while (tmp != null && tmp != Object.class) {
			for (Field field : tmp.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;

				list.add(0, field);
			}

			tmp = tmp.getSuperclass();
		}

		return list;
	}

	public static String readFieldComment(FieldDeclaration fd) {
		Javadoc doc = fd.getJavadoc();
		if (doc == null)
			return null;

		List<?> tags = doc.tags();
		if (tags.isEmpty())
			return null;

		TagElement te = (TagElement) tags.get(0);
		List<?> list = te.fragments();
		if (list != null) {
			String comment = list.toString();
			return comment.substring(1, comment.length() - 1);
		}

		return null;
	}

	/**
	 * 消除入参中不必要的符号
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static String trimJson(String jsonStr) {
		if (jsonStr == null)
			return null;

		return jsonStr.replaceAll("\\{-+", "\\{").replaceAll("-+\\{", "\\{").replaceAll(",-+\"", ",\"");
	}

	// public Object getEnumValue(Class<?> clz, String name) {
	// try {
	// Method method = clz.getMethod("values");
	// Object[] values = (Object[]) method.invoke(null);
	// for (Object v : values) {
	// if (v.toString().equalsIgnoreCase(name)) {
	// return v;
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

}
