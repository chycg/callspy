package com.cc.test.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * File: ArrayUtils
 *
 * @author chenyong
 * @desc
 * @date 2015年9月11日 上午11:27:43
 */
public class ArrayUtils {

    private static ThreadLocal<SimpleDateFormat[]> threadLocal = new ThreadLocal<SimpleDateFormat[]>() {

        @Override
        protected synchronized SimpleDateFormat[] initialValue() {
            SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat shortFormat = new SimpleDateFormat("yyyy-MM-dd");
            TimeZone timeZone = TimeZone.getTimeZone("GMT+08:00");
            longFormat.setTimeZone(timeZone);
            shortFormat.setTimeZone(timeZone);
            return new SimpleDateFormat[]{longFormat, shortFormat};
        }
    };

    private static SimpleDateFormat getLongFormat() {
        return threadLocal.get()[0];
    }

    private static SimpleDateFormat getShortFormat() {
        return threadLocal.get()[1];
    }

    /**
     * 以逗号分离出元素，添加到集合中返回；不包含空白
     */
    public static Set<String> splitStr(String str) {
        Set<String> set = new HashSet<>();
        if (isNotEmpty(str))
            for (String s : str.split(",")) {
                if (s.trim().isEmpty())
                    continue;
                set.add(s);
            }

        return set;
    }

    /**
     * 字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.trim().length() > 0;
    }

    /**
     * 集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && map.size() > 0;
    }

    /**
     * 判断数组是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 不为空
     */
    public static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }

    public static int max(int[] c) {
        int max = Integer.MIN_VALUE;
        for (int e : c)
            max = Math.max(max, e);

        return max;
    }

    /**
     * 直接相连，中间没有任何分隔符
     */
    public static String joinString(Object... objs) {
        StringBuilder sb = new StringBuilder(objs.length * 8);
        for (Object o : objs)
            sb.append(o);

        return sb.toString();
    }

    /**
     * 将对象集串接起来，以指定分隔符分隔
     */
    public static String makeString(String splitChar, Object... objs) {
        if (isEmpty(objs))
            return "";

        StringBuilder sb = new StringBuilder(objs.length * 6);
        if (isNotEmpty(objs) && objs[0] != null && objs[0].getClass().isArray()) {
            for (int i = 0; i < Array.getLength(objs[0]); i++)
                sb.append(Array.get(objs[0], i)).append(splitChar);
        } else {
            for (Object o : objs)
                sb.append(o).append(splitChar);
        }

        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * 数组转换为英文逗号隔开的字符串
     */
    public static String toString(Object[] array) {
        return makeString(",", array);
    }

    /**
     * 数组转换为英文逗号隔开的字符串
     */
    public static String toString(int[] array) {
        return makeString(",", array);
    }

    /**
     * 数组转换为英文逗号隔开的字符串
     */
    public static String toString(long[] array) {
        return makeString(",", array);
    }

    /**
     * 逗号隔开各个元素
     */
    public static String toString(Collection<?> c) {
        return makeString(",", c);
    }

    /**
     * [A,B,C],[a,b,c] --> A=a,B=b,C=c
     */
    public static String toString(String[] fields, Object[] values) {
        if (ArrayUtils.isEmpty(fields) || ArrayUtils.isEmpty(values) || fields.length != values.length)
            return null;

        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < fields.length; i++)
            sb.append(fields[i]).append("=").append(values[i]).append(",");

        sb.replace(sb.length() - 1, sb.length(), ")");

        return sb.toString();
    }

    /**
     * 用于覆盖Object.toString()，规则：ClassName: value0,value1
     */
    public static String toString(Object o, Object... values) {
        if (o == null)
            return null;

        return o.getClass().getSimpleName() + ":" + toString(values);
    }

    /**
     * 对象解析成Log字符串对象，方便阅读，一般用于集合、数组、长字符串对象的toString处理
     * <p>
     * showSize: 集合、数组对象，最大显示数量，范围[5,20]
     * <p>
     * maxLen:字符串对象，最大显示字符数，范围[256,1024]
     */
    public static String toLogString(Object o, int showSize, int maxLen) {
        if (o == null)
            return "null";

        if (showSize < 5)
            showSize = 5;

        if (showSize > 20)
            showSize = 20;

        if (maxLen < 256)
            maxLen = 256;

        if (maxLen > 1024)
            maxLen = 1024;

        if (o instanceof HttpServletRequest) {
            return "request";
        } else if (o instanceof HttpServletResponse)
            return "response";
        else if (o instanceof HttpSession)
            return "session";

        if (o instanceof Collection) {
            Collection<?> c = (Collection<?>) o;
            if (c.size() > showSize) {
                int i = 0;
                StringBuilder sb = new StringBuilder(256);
                sb.append("[");
                for (Object e : c) {
                    sb.append(e);
                    if (i == showSize - 1) {
                        sb.append("... <" + c.size() + ">]");
                        break;
                    }
                    sb.append(",");
                    i++;
                }
                return sb.toString();
            }

            return o.toString(); // 集合默认的toString
        } else if (o.getClass().isArray()) {
            if (Array.getLength(o) > showSize) {
                StringBuilder sb = new StringBuilder(256);
                sb.append("[");
                for (int i = 0; i < showSize; i++) {
                    sb.append(Array.get(o, i));
                    if (i == showSize - 1) {
                        sb.append("... <" + Array.getLength(o) + ">]");
                        break;
                    }
                    sb.append(",");
                }
                return sb.toString();
            }

            return Arrays.toString((Object[]) o);
        } else if (o instanceof Class<?>) {
            return ((Class<?>) o).getSimpleName() + ".class";
        } else if (o instanceof Date) {
            return ArrayUtils.getLongDate((Date) o);
        }

        String str = o.toString();
        if (str != null && str.length() > maxLen)
            return str.substring(0, Math.min(str.length(), maxLen)) + "... (" + str.length() + ")";

        return str;
    }

    public static String makeString(String splitChar, Collection<?> c) {
        if (isEmpty(c))
            return "";

        StringBuilder sb = new StringBuilder(c.size() * 8);
        for (Object o : c)
            sb.append(o).append(splitChar);

        if (sb.length() > 0)
            sb.delete(sb.lastIndexOf(splitChar), sb.length());

        return sb.toString();
    }

    /**
     * 集合中是否包含目标对象
     */
    public static <T> boolean isContain(T target, Collection<T> c) {
        if (target == null || isEmpty(c))
            return false;

        return c.contains(target);
    }

    /**
     * 数组是否包含目标对象
     */
    public static boolean isContain(Object target, Object... array) {
        if (target == null || isEmpty(array))
            return false;

        if (array[0] != null && array[0].getClass().isArray()) {
            for (int i = 0, k = Array.getLength(array[0]); i < k; i++) { // 原始类型数组特殊处理
                if (target.equals(Array.get(array[0], i)))
                    return true;
            }
            return false;
        } else if (array[0] != null && array[0] instanceof Collection) {
            return ((Collection<?>) array[0]).contains(target);
        }

        return indexOf(array, target, 0) != -1;
    }

    /**
     * 结果是两个对象是否相同(==)或相等(eqauls)
     */
    public static boolean isEquals(Object o1, Object o2) {
        return o1 == o2 ? true : o1 == null || o2 == null ? false : o1.equals(o2);
        // return o1 == null && o2 == null ? true : o1 != null ? o1.equals(o2) :
        // false;
    }

    /**
     * 对象是否既不相同也不相等
     */
    public static boolean isNotEquals(Object o1, Object o2) {
        return !isEquals(o1, o2);
    }

    public static int hashCode(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }

    public static int hashCode(Object... array) {
        if (array == null)
            return 0;

        int result = 1;
        if (array[0] != null && array[0].getClass().isArray()) {
            for (int i = 0, k = Array.getLength(array[0]); i < k; i++) { // 原始类型数组特殊处理
                Object element = Array.get(array, i);
                result = 31 * result + (element == null ? 0 : element.hashCode());
            }
            return result;
        }

        for (Object element : array)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }

    private static int indexOf(Object array[], Object objectToFind, int startIndex) {
        if (array == null)
            return -1;

        if (startIndex < 0)
            startIndex = 0;

        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++)
                if (array[i] == null)
                    return i;
        } else {
            for (int i = startIndex; i < array.length; i++)
                if (objectToFind.equals(array[i]))
                    return i;
        }

        return -1;
    }

    /**
     * 返回当前日期完整字符串，格式为: yyyy-MM-dd hh:mm:ss
     */
    public static String getLongCurrentDate() {
        return getLongFormat().format(new Date());
    }

    /**
     * 给定日期(Date)，返回格式为: yyyy-MM-dd hh:mm:ss的字符串
     */
    public static String getLongDate(Date date) {
        if (null == date)
            return "-";

        return getLongFormat().format(date);
    }

    /**
     * 给定日期(long:ms)，返回格式为: yyyy-MM-dd hh:mm:ss的字符串
     */
    public static String getLongDate(long value) {
        if (value <= 0)
            return "-";

        return getLongFormat().format(new Date(value));
    }

    /**
     * 当前日期格式化: yyyy-MM-dd
     */
    public static String getShortCurrentDate() {
        return getShortFormat().format(new Date());
    }

    /**
     * 指定日期格式化: yyyy-MM-dd
     */
    public static String getShortDate(Date date) {
        if (null == date)
            return "-";

        return getShortFormat().format(date);
    }

    /**
     * 给定日期(long:ms)，返回当前日期简写字符串，格式为: yyyy-MM-dd
     */
    public static String getShortDate(long value) {
        if (value <= 0)
            return "-";

        return getShortFormat().format(new Date(value));
    }

    public static int getMonthDays(LocalDateTime ldt) {
        if (ldt == null)
            ldt = LocalDateTime.now();

        return ldt.toLocalDate().lengthOfMonth();
    }

    public static LocalDateTime getLocalDateTime(long time) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(new Date(time).toInstant(), zone);
        return localDateTime;
    }

    public static long getLongTime(LocalDateTime ldt) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = ldt.atZone(zoneId).toInstant();
        long time = instant.toEpochMilli();
        return time;
    }

    /**
     * 自定义格式
     */
    public static String getFormatDate(long value, String format) {
        return new SimpleDateFormat(format).format(new Date(value));
    }

    public static long parseShortDate(String str) {
        return parseDate(str, getShortFormat());
    }

    public static long parseDate(String str, SimpleDateFormat format) {
        if (isEmpty(str))
            return 0L;

        Date date = format.parse(str, new ParsePosition(0));
        return date != null ? date.getTime() : 0L;
    }

    /**
     * 打印堆栈信息
     */
    public static String getStackTrace(Throwable e) {
        if (e == null)
            return "Exception == null!";

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw, true)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e2) {
            return e.getMessage();
        }
    }

    /**
     * 获取对象的字段值，如获取失败抛出异常
     */
    public static Object getFieldValue(Object bo, String fieldName) {
        if (bo == null || isEmpty(fieldName))
            throw new IllegalArgumentException("empty arg existed.");

        try {
            return makeGetMethod(bo.getClass(), fieldName).invoke(bo);
        } catch (Exception e) {
            Field field = getField(bo.getClass(), fieldName);
            return field == null ? null : getFieldValue(bo, field);
        }
    }

    /**
     * 获取指定字段的值
     */
    public static Object getFieldValue(Object bo, Field field) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(bo);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void setFieldValue(Object bo, String fieldName, Object value) {
        Method method = makeSetMethod(bo.getClass(), fieldName);
        if (method == null) {
            Field field = getField(bo.getClass(), fieldName);
            if (field == null)
                return;

            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                field.set(bo, value);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } finally {
                field.setAccessible(accessible);
            }
        } else {
            try {
                method.invoke(bo, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getMethodValue(Object o, String methodName, Object... args) {
        try {
            Method method = o.getClass().getMethod(methodName);
            return method.invoke(o, args);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 返回get方法
     */
    public static Method makeGetMethod(Class<?> clz, String fieldName) {
        String Name = capitalize(fieldName);
        Method method = null;
        try {
            try {
                method = clz.getMethod("get" + Name);
            } catch (NoSuchMethodException ex) {
                method = clz.getMethod("is" + Name);
            }
        } catch (Exception ex) {
            throw new RuntimeException("无合法的getter方法");
        }
        return method;
    }

    /**
     *
     */
    public static Method makeSetMethod(Class<?> clz, String fieldName) {
        Method getter = makeGetMethod(clz, fieldName);
        String Name = capitalize(fieldName);
        Method method = null;
        try {
            method = clz.getMethod("set" + Name, getter.getReturnType());
        } catch (NoSuchMethodException ex) {
        }
        return method;
    }

    /**
     * 字符串转换方法，如果有is前缀，则去掉
     */
    private static String capitalize(String fieldName) {
        if (isEmpty(fieldName))
            throw new IllegalArgumentException("The field name is empty...");

        if (fieldName.toLowerCase().startsWith("is"))
            fieldName = fieldName.substring(2);

        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 自当前类中寻找直至Object
     */
    public static Field getField(Class<?> clz, String fieldName) {
        Field field = null;
        while (clz != Object.class)
            try {
                field = clz.getDeclaredField(fieldName);
                return field;
            } catch (Exception ex) {
                clz = clz.getSuperclass();
            }
        return null;
    }

    public static List<Integer> splitToInt(String s) {
        if (isEmpty(s))
            return new ArrayList<>(0);

        String[] strs = s.split(",");
        List<Integer> list = new ArrayList<>(strs.length);
        Pattern p = Pattern.compile("[0-9]+");
        for (String str : strs)
            if (p.matcher(str).matches())
                list.add(Integer.parseInt(str.trim()));

        return list;
    }

    public static List<Long> splitToLong(String s) {
        if (isEmpty(s))
            return new ArrayList<>(0);

        String[] strs = s.split(",");
        List<Long> list = new ArrayList<>(strs.length);
        Pattern p = Pattern.compile("[0-9]+");
        for (String str : strs) {
            if (p.matcher(str).matches())
                list.add(Long.parseLong(str.trim()));
        }

        return list;
    }

    public static List<String> splitToString(String str){
        List<String> list = new ArrayList<>();
        if (isNotEmpty(str))
            for (String s : str.split(",")) {
                if (s.trim().isEmpty())
                    continue;

                list.add(s);
            }

        return list;
    }

    public static boolean forceDelete(File file) {
        if (!file.exists())
            return true;

        // 目录时先清空目录
        if (file.isDirectory())
            cleanDirectory(file);

        if (!file.delete()) // 考虑直接返回false
            throw new RuntimeException("Unable to delete file: " + file);

        return true;
    }

    private static void cleanDirectory(File directory) {
        File[] files = directory.listFiles();
        if (isEmpty(files))
            return;

        for (File file : files)
            forceDelete(file);
    }

    /**
     * 安静关闭流
     */
    public static void closeQuietly(InputStream in) {
        if (in == null)
            return;

        try {
            in.close();
        } catch (Exception e) {
        }
    }

    public static void closeQuietly(Reader reader) {
        if (reader == null)
            return;

        try {
            reader.close();
        } catch (Exception e) {
        }
    }

    public static void closeQuietly(OutputStream out) {
        if (out == null)
            return;

        try {
            out.close();
        } catch (Exception e) {
        }
    }

    public static List<Integer> getBitMap(int value) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int v = 1 << i;
            if ((value & v) > 0)
                list.add(v);
        }
        return list;
    }

    /**
     * xxId -> xx_id
     */
    public static String getColumnName(String name) {
        char[] chars = name.toCharArray();
        StringBuffer sb = new StringBuffer();

        boolean lastLower = true;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (i == 0) {
                sb.append(c);
                continue;
            }

            if (lastLower && Character.isAlphabetic(c) && Character.isUpperCase(c)) {
                sb.append("_");
                lastLower = false;
            } else {
                lastLower = true;
            }

            sb.append(c);
        }

        return sb.toString().toLowerCase();
    }

    /**
     * 通过表格列名生成字段名，需要遵循Java编码规范
     */
    public static String getFieldName(String name) {
        char[] chars = name.toLowerCase().toCharArray();
        StringBuffer sb = new StringBuffer();

        boolean hasSplit = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (i == 0 || i == chars.length - 1) {
                sb.append(c);
                continue;
            }

            if (c == '_') {
                hasSplit = true;
                continue;
            }

            if (hasSplit) {
                sb.append(Character.toUpperCase(c));
                hasSplit = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}