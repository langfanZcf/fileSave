/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/***
 * excel操作工具类
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/22 9:10
 */
public class ExcelUtils {


    /***
     * 创建Excel-Workbook对象
     * @param size
     * @return
     */
    public static Workbook createWorkBook(Integer size){
        Workbook workbook=null;
        if (size==null||size==0){
            workbook=new HSSFWorkbook();
        }else if(size < 100000){
            workbook=new XSSFWorkbook();
        }else{
            workbook=new SXSSFWorkbook();
        }
        return workbook;
    }

}
