/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.config;

import com.xiaominfo.oss.service.OSSStatisticDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/23 17:45
 */
@Component
public class OSSServerScheduling {

    @Autowired
    OSSStatisticDayService ossStatisticDayService;

    /***
     * 每日的凌晨1点、23统计上传文件数
     */
    @Scheduled(cron = "0 0 1,23 * * ? ")
    public void countScheduling(){
        ossStatisticDayService.count();
    }
}
