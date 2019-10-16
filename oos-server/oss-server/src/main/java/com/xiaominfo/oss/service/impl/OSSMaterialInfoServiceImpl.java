/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.xiaominfo.oss.common.pojo.Pagination;
import com.xiaominfo.oss.module.dao.OSSMaterialInfoMapper;
import com.xiaominfo.oss.module.entity.OSSMaterialInfoResult;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import com.xiaominfo.oss.service.OSSMaterialInfoService;
import com.xiaominfo.oss.utils.FileUtils;
import com.xiaominfo.oss.utils.PaginationUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/06/18 8:54
 */
@Service
public class OSSMaterialInfoServiceImpl extends ServiceImpl<OSSMaterialInfoMapper, OSSMaterialInfo> implements OSSMaterialInfoService {

    @Autowired
    OSSMaterialInfoMapper ossMaterialInfoMapper;

    @Override
    public String queryTotalSpaceByteStr() {
        Integer len = ossMaterialInfoMapper.selectMaterialUseSpace();
        if (len == null) {
            return "0kb";
        }
        return FileUtils.byteToString(new BigDecimal(len).longValue());
    }

    @Override
    public Integer count() {
        return ossMaterialInfoMapper.selectCount(new EntityWrapper<>());
    }

    @Override
    public Pagination<OSSMaterialInfoResult> queryByPage(OSSMaterialInfo ossMaterialInfo, Integer current_page, Integer page_size) {
        Wrapper<OSSMaterialInfo> wrapper = new EntityWrapper<>();
        Integer count = ossMaterialInfoMapper.selectCount(wrapper);
        Map params = Maps.newHashMap();
        RowBounds rowBounds = PaginationUtils.getBounds(current_page, page_size);
        //分页
        params.put("start", rowBounds.getOffset());
        params.put("page_size", page_size);
        List<OSSMaterialInfoResult> list = ossMaterialInfoMapper.queryByPage(params);
        Pagination<OSSMaterialInfoResult> pagination = PaginationUtils.transfor(list, count, current_page, page_size);
        return pagination;
    }

    @Override
    public List<OSSMaterialInfo> waitSync(String node,String thatNode) {
        return ossMaterialInfoMapper.queryWaitSync(node,thatNode);
    }
}
