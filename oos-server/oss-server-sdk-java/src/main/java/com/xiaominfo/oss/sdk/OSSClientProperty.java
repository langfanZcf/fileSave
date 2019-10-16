/*
 * Copyright (C) 2018 Zhejiang lishiots Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.lishiots.com.
 * Developer Web Site: http://open.lishiots.com.
 */

package com.xiaominfo.oss.sdk;

import java.io.Serializable;

/***
 * client配置文件属性
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/05/30 19:22
 */
public class OSSClientProperty implements Serializable {
    private static final long serialVersionUID = 1L;
    /***
     * 远程oss上传地址
     */
    private String remote;
    /***
     * 项目名称
     */
    private String project;

    private String appid;

    private String appsecret;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public OSSClientProperty(String remote, String project) {
        this.remote = remote;
        this.project = project;
    }

    public OSSClientProperty(String remote) {
        this.remote = remote;
    }

    public OSSClientProperty() {
    }

    public OSSClientProperty(String remote, String project, String appid, String appsecret) {
        this.remote = remote;
        this.project = project;
        this.appid = appid;
        this.appsecret = appsecret;
    }
}
