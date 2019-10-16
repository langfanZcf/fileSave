/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.spring.boot.starter;

import com.xiaominfo.oss.sdk.OSSClient;
import com.xiaominfo.oss.sdk.OSSClientProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 *
 * @since:oss-server-sdk-java 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/25 10:45
 */
@Configuration
@EnableConfigurationProperties(OSSServerProperties.class)
public class OSSServerClientAutoConfiguration {

    private final OSSServerProperties ossServerProperties;


    public OSSServerClientAutoConfiguration(OSSServerProperties ossServerProperties) {
        this.ossServerProperties = ossServerProperties;
    }


    @Bean
    @ConditionalOnMissingBean
    public OSSClient ossClient(){
        OSSClient ossClient=new OSSClient(new OSSClientProperty(ossServerProperties.getEndpoint(),ossServerProperties.getApplication(),ossServerProperties.getAppid(),ossServerProperties.getAppsecret()));
        return ossClient;
    }
}
