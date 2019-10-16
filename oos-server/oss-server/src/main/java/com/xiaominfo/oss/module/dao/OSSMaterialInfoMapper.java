/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.module.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.xiaominfo.oss.module.entity.OSSMaterialInfoResult;
import com.xiaominfo.oss.module.entity.OssMaterialCount;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/06/18 8:53
 */
public interface OSSMaterialInfoMapper extends BaseMapper<OSSMaterialInfo> {

    /***
     * 查询使用space
     * @return
     */
    Integer selectMaterialUseSpace();

    /***
     * 分页查询
     * @param param
     * @return
     */
    List<OSSMaterialInfoResult> queryByPage(Map param);


    /***
     * 统计
     * @return
     */
    List<OssMaterialCount> queryCount(Map param);

    List<OSSMaterialInfo> queryWaitSync(@Param("node") String node, @Param("thatNode") String thatNode);
}
