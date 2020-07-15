package com.cc.test.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ConfigInfo implements Serializable{

    private static final long serialVersionUID = -4599843924018911898L;

    private boolean effect;

    private int oftenSize;

    private String rpcUrl;

    private String testLog;

    private Map<String,Class<?>> ifaceList;

    private String path;

    private List<String> pathList;

    
}