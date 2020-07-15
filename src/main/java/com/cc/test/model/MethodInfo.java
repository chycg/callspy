package com.cc.test.model;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.cc.test.util.ApiUtils;

/**
 * @author: chenyong
 *          <p>
 * @createtime：2019年10月30日
 */
public class MethodInfo implements Serializable {

    private static final long serialVersionUID = 2220719378758333065L;

	/**
	 * 别名，无重载时，等同于方法名，有重载时，会加编号
	 */
	private String name;

    private Method method;

    private String url;

    private int methodType;

    // 自定义json data
    private Object sample;

    private boolean jsonType;

	public MethodInfo(Method method, String name) {
		this.name = name;
        this.method = method;
        checkMethod();
    }

    private void checkMethod() {
        GetMapping gm = method.getAnnotation(GetMapping.class);
        if (gm != null) {
            this.methodType = 1;
            this.url = gm.value()[0];
        }

        PostMapping pm = method.getAnnotation(PostMapping.class);
        if (pm != null) {
            this.methodType = 2;
            this.url = pm.value()[0];
        }

        RequestMapping rm = method.getAnnotation(RequestMapping.class);
        if (rm != null) {
            if (rm.method().length == 0)
                this.methodType = 3;

            int v = 0;
            for (RequestMethod e : rm.method()) {
                if (e == RequestMethod.GET)
                    v += 1;

                if (e == RequestMethod.POST)
                    v += 2;
            }

            this.methodType = v;
            this.url = rm.value()[0];
        }

        /**
         * 假定post json方式，仅一个json入参；对象+普通入参组合型暂时不考虑
         */
        if (supportPost() && method.getParameterCount() == 1) {
            Class<?> clz = method.getParameterTypes()[0];
			this.jsonType = ApiUtils.isObject(clz) || method.getParameters()[0].getAnnotation(RequestBody.class) != null;
        }

        String prefix = "/";
        Class<?> clz = method.getDeclaringClass();
        if (clz.isAnnotationPresent(RequestMapping.class)) {
            rm = clz.getAnnotation(RequestMapping.class);
            if (rm.value().length > 0) {
                prefix = rm.value()[0];
                if (!prefix.startsWith("/"))
                    prefix = "/" + prefix;
            }
        }

        if (!url.startsWith("/"))
            url = "/" + url;

        url = (prefix + url).replace("//", "/");

        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
    }

    // public List<Object> getMethodArgs(JSONObject json) {
    // List<Object> args = new ArrayList<>();
    //
    // for (Parameter e : method.getParameters()) {
    // Object arg = json.get(e.getName());
    // Class<?> type = e.getType();
    //
    // Object value = null;
    // if (arg != null) {
    // if (type == String.class) {
    // value = arg.toString();
    // } else if (type.isEnum() && !arg.toString().matches("\\d+")) {
    // value = Context.getEnumValue(type, arg.toString());
    // } else {
    // // value = JSON.parseObject(arg.toString(), type);
    // value = new Gson().fromJson(arg.toString(), type);
    // }
    // }
    //
    // args.add(value);
    // }
    //
    // return args;
    // }

    public Method getMethod() {
        return method;
    }

	public String getName() {
		return name;
	}

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public String getUrl() {
        return url;
    }

    public boolean isJsonType() {
        return jsonType;
    }

    public boolean supportGet() {
        return methodType == 1 || methodType == 3;
    }

    public boolean supportPost() {
        return methodType == 2 || methodType == 3;
    }

    public void clearSample() {
        this.sample = null;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }
}
