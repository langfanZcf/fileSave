/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */
package com.xiaominfo.oss.module.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.xiaominfo.oss.module.model.OSSStatisticDay;

import java.util.List;
import java.util.Map;

/***
 *
 * @since:cloud-ims 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/23 8:57
 */
public interface OSSStatisticDayMapper extends BaseMapper<OSSStatisticDay> {

    List<Map> queryWeekCounts(Map params);
}
