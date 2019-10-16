/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.module.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.io.Serializable;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2018/06/23 8:53
 */
@TableName(value = "oss_statistic_day")
public class OSSStatisticDay extends Model<OSSStatisticDay> {

    @TableId(value = "id",type = IdType.UUID)
    private String id;

    @TableField(value = "create_time")
    private String createTime;

    @TableField(value = "user_id")
    private String userId;

    @TableField(value = "cur_date")
    private String curDate;

    @TableField(value = "modified_time")
    private String modifiedTime;

    @TableField(value = "use_spaces")
    private Long useSpaces;

    private Integer files;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurDate() {
        return curDate;
    }

    public void setCurDate(String curDate) {
        this.curDate = curDate;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getUseSpaces() {
        return useSpaces;
    }

    public void setUseSpaces(Long useSpaces) {
        this.useSpaces = useSpaces;
    }

    public Integer getFiles() {
        return files;
    }

    public void setFiles(Integer files) {
        this.files = files;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
