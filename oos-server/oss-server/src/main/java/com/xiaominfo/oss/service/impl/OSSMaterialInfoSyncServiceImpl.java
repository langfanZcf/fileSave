/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xiaominfo.oss.api.RootApis;
import com.xiaominfo.oss.common.SpringContextUtil;
import com.xiaominfo.oss.exception.AssemblerException;
import com.xiaominfo.oss.exception.ErrorCable;
import com.xiaominfo.oss.exception.ErrorConstant;
import com.xiaominfo.oss.module.dao.OSSMaterialInfoSyncMapper;
import com.xiaominfo.oss.module.model.OSSAppInfo;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.module.model.OSSInformation;
import com.xiaominfo.oss.module.model.OSSMaterialInfoSync;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.service.OSSAppInfoService;
import com.xiaominfo.oss.service.OSSDeveloperService;
import com.xiaominfo.oss.service.OSSInformationService;
import com.xiaominfo.oss.service.OSSMaterialInfoSyncService;
import com.xiaominfo.oss.sync.Node;
import com.xiaominfo.oss.utils.IdGen;
import io.netty.util.internal.StringUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/06/18 8:54
 */
@Service
public class OSSMaterialInfoSyncServiceImpl extends ServiceImpl<OSSMaterialInfoSyncMapper, OSSMaterialInfoSync> implements OSSMaterialInfoSyncService {

    Logger log = LoggerFactory.getLogger(OSSMaterialInfoSyncServiceImpl.class);
    @Autowired
    OSSMaterialInfoSyncMapper ossMaterialInfoSyncMapper;
    @Autowired
    OSSDeveloperService ossDeveloperService;
    @Autowired
    OSSAppInfoService ossAppInfoService;
    @Autowired
    OSSInformationService ossInformationService;

    @Override
    public OSSMaterialInfoSync createSync(String materialId, String type) {
        String localIp = "";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            log.info("未找到本机ip：", e);
        }
        int masterPort = Integer.parseInt(SpringContextUtil.getProperty("material.masterPort"));
        OSSMaterialInfoSync sync = new OSSMaterialInfoSync();
        //sync.setType("Master");
        sync.setType(type);
        sync.setNode(new Node(masterPort, localIp).toString());
        sync.setMaterialId(materialId);
        sync.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        this.insert(sync);
        return sync;
    }


    @Override
    public String buildPath(NettyFileRequest ef, OSSInformation ossInformationRef, OSSDeveloper ossDeveloperRef, OSSAppInfo ossAppRef) {
        //第一次的时候才生成路径和做检查
        if (!StringUtil.isNullOrEmpty(ef.getProjectPath())) {
            return ef.getProjectPath();
        }

        List<String> excludeFileTypeLists = Lists.newArrayList(StrUtil.split(SpringContextUtil.getProperty("material.excludeFileTypes"), ","));
        if (excludeFileTypeLists.contains(ef.getMediaType())) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "Your uploaded file may harm the server"));
        }

        RootApis apis = new RootApis();
        String moduleStr = "";
        apis.assertArgumentNotEmpty(ef.getAppid(), "appid can't be empty");
        apis.assertArgumentNotEmpty(ef.getAppsecret(), "appsecret can't be empty");
        apis.assertArgumentNotEmpty(ef.getProject(), "project name can't be empty");
        apis.assertArgumentNotEmpty(ef.getFile(), "files can't be empty");
        if (!StringUtil.isNullOrEmpty(ef.getModule())) {
            moduleStr = ef.getModule();
        }

        OSSDeveloper ossDeveloper = ossDeveloperService.queryByAppid(ef.getAppid(), ef.getAppsecret());
        apis.assertArgumentNotEmpty(ossDeveloper, "appid or appsecret is invalid");
        String projectFilePathName = ef.getProject();
        apis.validateProjectName(projectFilePathName);
        //判断文件夹code是否相等
        List<OSSAppInfo> ossAppInfos = ossAppInfoService.queryByDevIds(ossDeveloper.getId());
        if (ossAppInfos == null || ossAppInfos.size() == 0) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
        }
        OSSAppInfo ossApp = null;

        for (OSSAppInfo ossAppInfo : ossAppInfos) {
            if (StrUtil.equalsIgnoreCase(ossAppInfo.getCode(), projectFilePathName)) {
                ossApp = ossAppInfo;
                break;
            }
        }
        if (null==ossApp) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
        }

        OSSInformation ossInformation = ossInformationService.queryOne();
        String root = ossInformation.getRoot();
        //验证文件夹规则,不能包含特殊字符
        File rootFile = new File(root);
        //createDirectoryQuietly(file);
        apis.createDirectoryQuietly(rootFile);

        StringBuffer path = new StringBuffer();
        path.append(rootFile.getAbsolutePath());
        path.append(File.separator);
        path.append(projectFilePathName);
        if (StrUtil.isNotEmpty(moduleStr)) {
            if (!moduleStr.startsWith("/")) {
                path.append("/");
            }
            path.append(moduleStr);
        }
        log.info("path:{}", path);
        File projectFile = new File(path.toString());
        if (!projectFile.exists()) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.AUTHENTICATION_FAILED, "You do not have operating authority for this directory " + projectFilePathName + ", or the directory was not created"));
        }


        int start = rootFile.getAbsolutePath().length();
        String currentTimeString = DateTime.now().toString("yyyyMMddHHmmss");
        //按月目录
        final String monthPath = currentTimeString.substring(0, 6);
        //按日目录,如W020151111
        final String dayPath = currentTimeString.substring(6, 8);
        final String fileDirectory = projectFile.getAbsolutePath() + "/" + monthPath + "/" + dayPath;
        log.info("directory:{}", fileDirectory);
        File saveFileDirectory = new File(fileDirectory);
        apis.createDirectoryQuietly(saveFileDirectory);


        //文件id
        String uuid = IdGen.getUUID();
        //生成uuid文件名称
        String fileName = uuid + "." + ef.getMediaType();
        //静态资源存储路径
        StringBuffer storePathBuffer = new StringBuffer();
        String pre = projectFile.getAbsolutePath().substring(start);
        pre = com.xiaominfo.oss.utils.FileUtils.transforSysSpec(pre);
        if (!pre.startsWith("/")) {
            storePathBuffer.append("/");
        }
        storePathBuffer.append(pre).append("/").append(monthPath).append("/").append(dayPath).append("/").append(fileName);
        ef.setUuid(uuid);
        //输出文件
        String saveFilePath = saveFileDirectory.getAbsolutePath() + "/" + fileName;
        BeanUtil.copyProperties(ossInformation, ossInformationRef);
        BeanUtil.copyProperties(ossDeveloper, ossDeveloperRef);
        BeanUtil.copyProperties(ossApp, ossAppRef);
        log.info("saveFilePath = [{}]", saveFilePath);
        return saveFilePath;
    }


}
