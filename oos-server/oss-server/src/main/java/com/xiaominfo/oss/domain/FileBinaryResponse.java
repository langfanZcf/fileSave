/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.domain;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/05/30 15:32
 */
public class FileBinaryResponse {
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

    /***
     * 文件类型
     */
    private String objType;
    /***
     * 文件原始名称
     */
    private String originalName;

    /***
     * 文件长度
     */
    private long byteLength;
    /***
     * 字符串显示
     */
    private String byteToStr;

    public FileBinaryResponse(String id, String url, String store) {
        this.id = id;
        this.url = url;
        this.store = store;
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


    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public long getByteLength() {
        return byteLength;
    }

    public void setByteLength(long byteLength) {
        this.byteLength = byteLength;
    }

    public String getByteToStr() {
        return byteToStr;
    }

    public void setByteToStr(String byteToStr) {
        this.byteToStr = byteToStr;
    }
}
