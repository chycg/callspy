package com.cc.test.web.api.item;

import com.cc.test.util.ApiUtils;
import com.cc.test.util.ArrayUtils;

import lombok.Data;

/**
 * 普通字段数据
 */
@Data
public class FieldData implements ApiData {

    private static final long serialVersionUID = -5985802454010991836L;

    private String type;

    private String description;

    @Override
    public DataType dataType() {
        return DataType.PLAIN;
    }

    public FieldData(Class<?> clz, String comment) {
		this.type = ApiUtils.getTypeInfo(clz);

        if (comment == null)
            comment = "";

        this.description = comment;
    }

    @Override
    public void updateDesc(ApiData data) {
        if (data == null || !sameType(data))
            return;

        FieldData fd = (FieldData) data;
        if (ArrayUtils.isNotEmpty(fd.description))
            this.description = fd.description;
    }
}