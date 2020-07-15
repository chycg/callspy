package com.cc.test.web.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.cc.test.util.ArrayUtils;
import com.cc.test.web.api.item.FormVO;

import lombok.Data;

@Data
public class ApiBody implements Serializable {

    private static final long serialVersionUID = 577279845240749095L;

    private String token;

    private String id = "";

    private String title;

    private String catid;

    private String path;

    private String status = "undone";

    private String req_body_type = "json";

    private boolean req_body_is_json_schema = true;

    private final boolean res_body_is_json_schema = true;

    private final String res_body_type = "json";

    private boolean api_opened = true;

    private String method = "post";

    private List<JSONObject> req_headers = new ArrayList<>();

    /**
     * 固定值?
     */
    private List<Object> req_params = new ArrayList<>();

    /**
     * 请求表单[]
     */
    private final List<FormVO> req_body_form = new ArrayList<>();

    /**
     * request，get query
     */
    private List<FormVO> req_query = new ArrayList<>();

    /**
     * request json
     */
    private String req_body_other;

    /**
     * response json
     */
    private String res_body;

    private String desc = "";

    /**
     * @param isGet    get or post
     * @param jsonType contentType is json?
     * @param body
     */
    public void setBodyData(boolean isGet, boolean jsonType, MethodBody body) {
        this.method = isGet ? "get" : "post";
		this.req_body_type = jsonType ? "json" : "form";

        JSONObject header = new JSONObject();
        header.put("name", "Content-Type");
        header.put("value", jsonType ? "application/json" : "application/x-www-form-urlencoded");
        req_headers.add(header);

        res_body = body.getResponse() == null ? "{}" : body.getResponse().toString();

        if (jsonType) {
            req_body_other = body.getRequest() == null ? "{}" : body.getRequest().toString();
        } else if (ArrayUtils.isNotEmpty(body.getForm())) {
            if (isGet)
                req_query.addAll(body.getForm());
            else
                req_body_form.addAll(body.getForm());
        }
    }
}