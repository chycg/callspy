package com.cc.test.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 引用链，判断引用环
 * 
 * @author chenyong
 *
 * @create: 2020年6月18日 下午3:37:40
 */
public class ReferenceNode implements Serializable {

	private static final long serialVersionUID = -632439517481968159L;

	/**
	 * field
	 */
	private Field field;

	/**
	 * container field, parse element type
	 */
	private Type actualType;

	/**
	 * if actualType is simple type which cannot reference other
	 */
	private boolean simpleType;

	/**
	 * parent node
	 */
	private ReferenceNode parent;

	/**
	 * child references
	 */
	private List<ReferenceNode> children = new ArrayList<>();

	public ReferenceNode(Field field) {
		this(null, field);
	}

	private ReferenceNode(ReferenceNode parent, Field field) {
		this.parent = parent;
		this.field = field;

		init();
	}

	/**
	 * field real type, return element type if field is container type
	 * 
	 * @param field
	 * @return
	 */
	private void init() {
		Class<?> type = field.getType();
		if (Collection.class.isAssignableFrom(type)) {
			Type genericType = field.getGenericType(); // 字段泛型类型
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;
				actualType = pt.getActualTypeArguments()[0];
			}
		} else {
			actualType = type;
		}

		simpleType = actualType.getTypeName().length() == 1 || ApiUtils.isSingle((Class<?>) actualType);
	}

	public ReferenceNode addChild(Field field) {
		ReferenceNode child = new ReferenceNode(this, field);
		children.add(child);

		return child;
	}

	/**
	 * check current field have ring refercen or not
	 * 
	 * @param field
	 * @return
	 */
	public boolean hasRing() {
		if (simpleType)
			return false;

		Type type = actualType;
		ReferenceNode tmp = parent;
		while (tmp != null) {
			if (tmp.actualType == type)
				return true;

			tmp = tmp.parent;
		}

		return false;
	}

	public Field getField() {
		return field;
	}

	public Type getActualType() {
		return actualType;
	}

	@Override
	public String toString() {
		return "ReferenceNode [field=" + field.getName() + ", actualType=" + actualType.getTypeName() + "]";
	}

}
