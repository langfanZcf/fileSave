/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaominfo.oss.module.dao.OSSMaterialInfoMapper;
import com.xiaominfo.oss.module.dao.OSSStatisticDayMapper;
import com.xiaominfo.oss.module.entity.OssMaterialCount;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.module.model.OSSStatisticDay;
import com.xiaominfo.oss.service.OSSDeveloperService;
import com.xiaominfo.oss.service.OSSMaterialInfoService;
import com.xiaominfo.oss.service.OSSStatisticDayService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/23 8:59
 */
@Service
public class OSSStatisticDayServiceImpl extends ServiceImpl<OSSStatisticDayMapper,OSSStatisticDay> implements OSSStatisticDayService {

    @Autowired
    OSSStatisticDayMapper ossStatisticDayMapper;

    @Autowired
    OSSMaterialInfoMapper ossMaterialInfoMapper;

    @Autowired
    OSSMaterialInfoService ossMaterialInfoService;

    @Autowired
    OSSDeveloperService ossDeveloperService;

    @Override
    public OSSStatisticDay queryByDayAndUserId(String day, String userId) {
        Wrapper<OSSStatisticDay> wrapper=new EntityWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("cur_date",day);
        List<OSSStatisticDay> list=ossStatisticDayMapper.selectList(wrapper);
        if (list!=null&&list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public void count() {
        //计算时间,近一周的时间,不包括今天
        DateTime star=DateTime.now().minusDays(8);
        DateTime end=DateTime.now().minusDays(1);
        Wrapper<OSSStatisticDay> wrapper=new EntityWrapper<>();
        wrapper.between("cur_date",star.toString("yyyy-MM-dd"),end.toString("yyyy-MM-dd"));
        List<OSSStatisticDay> list=ossStatisticDayMapper.selectList(wrapper);
        Map params= Maps.newHashMap();
        params.put("start",star.toString("yyyy-MM-dd"));
        params.put("end",end.toString("yyyy-MM-dd"));
        List<OssMaterialCount> ossMaterialCounts=ossMaterialInfoMapper.queryCount(params);
        List<OSSDeveloper> alldevs= ossDeveloperService.queryAllDevs();
        for (OSSDeveloper dev:alldevs){
            String userId=dev.getId();
            for (int i=0;i<8;i++){
                String curDate=star.plusDays(i).toString("yyyy-MM-dd");
                //判断当天是否存在
                OSSStatisticDay ossStatisticDay=queryId(list,curDate,userId);
                if (ossStatisticDay==null){
                    //为空
                    OssMaterialCount ossMaterialCount=null;
                    for (OssMaterialCount oc:ossMaterialCounts){
                        if (StrUtil.equalsIgnoreCase(curDate,oc.getCurDate())&&StrUtil.equalsIgnoreCase(userId,oc.getUserId())){
                            ossMaterialCount=oc;
                            break;
                        }
                    }
                    if (ossMaterialCount==null){
                        //0
                        OSSStatisticDay osd=new OSSStatisticDay();
                        osd.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                        osd.setCurDate(curDate);
                        osd.setFiles(0);
                        osd.setUserId(userId);
                        osd.setUseSpaces(0L);
                       // newCounts.add(osd);
                        ossStatisticDayMapper.insert(osd);
                    }else{
                        OSSStatisticDay osd=new OSSStatisticDay();
                        osd.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                        osd.setCurDate(ossMaterialCount.getCurDate());
                        osd.setFiles(ossMaterialCount.getFiles());
                        osd.setUserId(ossMaterialCount.getUserId());
                        osd.setUseSpaces(ossMaterialCount.getSumlen());
                        ossStatisticDayMapper.insert(osd);
                    }
                }
            }
        }
    }

    @Override
    public List<Map> queryCurrentWeekStaticsDay() {
        DateTime star=DateTime.now().minusDays(8);
        List<String> list=Lists.newArrayList();
        for (int i=0;i<8;i++){
            list.add(star.plusDays(i).toString("yyyy-MM-dd"));
        }
        Map params=Maps.newHashMap();
        params.put("dates",list);
        return ossStatisticDayMapper.queryWeekCounts(params);
    }

    private OSSStatisticDay queryId(List<OSSStatisticDay> list, String day, String userId){
        OSSStatisticDay ossStatisticDay=null;
        if (list!=null&&list.size()>0){
            for (OSSStatisticDay sticday:list){
                if (StrUtil.equalsIgnoreCase(day,sticday.getCurDate())&&StrUtil.equalsIgnoreCase(userId,sticday.getUserId())){
                    ossStatisticDay=sticday;
                    break;
                }
            }
        }
        return ossStatisticDay;
    }
}
