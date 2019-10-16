/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.spring.boot.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/***
 * 配置文件
 * @since:oss-server-sdk-java 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/25 10:25
 */
@ConfigurationProperties(prefix = OSSServerProperties.OSS_SERVER_PREFIX)
public class OSSServerProperties {

    /***
     * SpringBoot configuration prefix for oss-server
     */
    public static final String OSS_SERVER_PREFIX="oss-server";


    private String endpoint;

    private String appid;

    private String appsecret;

    private String application;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
