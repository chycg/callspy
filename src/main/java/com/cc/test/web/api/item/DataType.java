package com.cc.test.web.api.item;

/**
 * 数据类型
 */
public enum DataType {

    PLAIN(1),

    PLAIN_ARRAY(2),

    OBJECT(3),

    OBJECT_ARRAY(4),

    ;

    private int code;

    DataType(int code) {
        this.code = code;
    }

    public boolean isPlain(){
        return this == PLAIN;
    }

    public boolean isPlainArray(){
        return this == PLAIN_ARRAY;
    }

    public boolean isObject(){
        return this == OBJECT;
    }

    public boolean isObjectArray(){
        return this == OBJECT_ARRAY;
    }

}
