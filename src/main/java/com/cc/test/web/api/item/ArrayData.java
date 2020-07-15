package com.cc.test.web.api.item;

import lombok.Data;

/**
 * 数组或集合类型
 */
@Data
public class ArrayData implements ApiData {

    private static final long serialVersionUID = -1930688481252579905L;

    private String type = "array";

    private ObjectData items;

    @Override
    public DataType dataType() {
        return DataType.OBJECT_ARRAY;
    }

    public void updateDesc(ApiData data) {
        if (data == null || !sameType(data))
            return;

        ArrayData ad = (ArrayData) data;
        items.updateDesc(ad.getItems());
    }
}