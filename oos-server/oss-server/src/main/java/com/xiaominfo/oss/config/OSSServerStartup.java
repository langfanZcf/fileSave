/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.config;

import com.xiaominfo.oss.service.OSSStatisticDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/23 17:44
 */
@Component
public class OSSServerStartup implements CommandLineRunner {

    @Autowired
    OSSStatisticDayService ossStatisticDayService;

    @Override
    public void run(String... args) throws Exception {
        ossStatisticDayService.count();
    }
}
