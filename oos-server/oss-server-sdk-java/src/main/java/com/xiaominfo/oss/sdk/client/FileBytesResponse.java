/*
 * Copyright (C) 2018 Zhejiang lishiots Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.lishiots.com.
 * Developer Web Site: http://open.lishiots.com.
 */

package com.xiaominfo.oss.sdk.client;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/05/30 15:32
 */
public class FileBytesResponse {
    /***
     * id
     */
    private String id;
    /***
     * 在线url地址
     */
    private String url;
    /**
     * 存储路径名称
     */
    private String store;

    private String originalName;

    private String objType;

    private Long byteLength;

    private String byteToStr;

    public FileBytesResponse(String id, String url, String store) {
        this.id = id;
        this.url = url;
        this.store = store;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public Long getByteLength() {
        return byteLength;
    }

    public void setByteLength(Long byteLength) {
        this.byteLength = byteLength;
    }

    public String getByteToStr() {
        return byteToStr;
    }

    public void setByteToStr(String byteToStr) {
        this.byteToStr = byteToStr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}
