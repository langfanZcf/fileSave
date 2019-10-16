/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */
package com.xiaominfo.oss.service;

import org.apache.poi.ss.usermodel.Workbook;

/***
 * excel工具service
 * @since:cloud-ims 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/22 9:14
 */
public interface ExcelService {

    /***
     * 导出数据库存储资源
     * @return
     */
    Workbook exportMaterials();
}
