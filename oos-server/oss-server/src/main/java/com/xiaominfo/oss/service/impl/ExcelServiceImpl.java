/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.xiaominfo.oss.common.pojo.Pagination;
import com.xiaominfo.oss.module.entity.OSSMaterialInfoResult;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import com.xiaominfo.oss.service.ExcelService;
import com.xiaominfo.oss.service.OSSMaterialInfoService;
import com.xiaominfo.oss.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/22 9:19
 */
@Service
public class ExcelServiceImpl implements ExcelService {

    private Log log= LogFactory.get();

    @Autowired
    OSSMaterialInfoService ossMaterialInfoService;

    @Override
    public Workbook exportMaterials() {
        //
        Integer count=ossMaterialInfoService.count();
        Workbook workbook= ExcelUtils.createWorkBook(count);
        Sheet sheet=workbook.createSheet("oss-server存储对象表");
        int rownum=0;
        //创建header
        Row row=sheet.createRow(rownum);
        row.createCell(0).setCellValue("开发者");
        row.createCell(1).setCellValue("应用");
        row.createCell(2).setCellValue("原始名");
        row.createCell(3).setCellValue("文件大小");
        row.createCell(4).setCellValue("文件byte大小");
        row.createCell(5).setCellValue("来源ip");
        row.createCell(6).setCellValue("类型");
        row.createCell(7).setCellValue("创建时间");
        row.createCell(8).setCellValue("url");
        row.createCell(9).setCellValue("存储路径");
        row.createCell(10).setCellValue("id");
        row.createCell(11).setCellValue("最后修改时间");
        //批量分页查询
        Integer pageSize=3000;
        Integer currentPage=1;
        boolean flag=true;
        do {
            Pagination<OSSMaterialInfoResult> pagination=ossMaterialInfoService.queryByPage(new OSSMaterialInfo(),currentPage,pageSize);
            log.info("oss-material-size:{}",pagination.getCount());
            List<OSSMaterialInfoResult> ossMaterialInfoResults=pagination.getData();
            if (ossMaterialInfoResults==null||ossMaterialInfoResults.size()<pageSize){
                flag=false;
            }
            if (ossMaterialInfoResults!=null&&ossMaterialInfoResults.size()>0){
                for (OSSMaterialInfoResult osr:ossMaterialInfoResults){
                    rownum++;
                    row=sheet.createRow(rownum);
                    row.createCell(0).setCellValue(osr.getName());
                    row.createCell(1).setCellValue(osr.getAppname());
                    row.createCell(2).setCellValue(osr.getOriginalName());
                    row.createCell(3).setCellValue(osr.getByteStr());
                    row.createCell(4).setCellValue(osr.getLen());
                    row.createCell(5).setCellValue(osr.getFromIp());
                    row.createCell(6).setCellValue(osr.getType());
                    row.createCell(7).setCellValue(osr.getCreateTime());
                    row.createCell(8).setCellValue(osr.getUrl());
                    row.createCell(9).setCellValue(osr.getStorePath());
                    row.createCell(10).setCellValue(osr.getId());
                    row.createCell(11).setCellValue(osr.getLastModifiedTime());
                }
            }
        }while (flag);
        return workbook;
    }
}
