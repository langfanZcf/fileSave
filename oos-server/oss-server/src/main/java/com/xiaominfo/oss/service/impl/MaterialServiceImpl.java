/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.common.collect.Lists;
import com.xiaominfo.oss.api.RootApis;
import com.xiaominfo.oss.common.pojo.ThreadLocalHolder;
import com.xiaominfo.oss.domain.FileBinaryRequest;
import com.xiaominfo.oss.domain.FileBinaryResponse;
import com.xiaominfo.oss.exception.AssemblerException;
import com.xiaominfo.oss.exception.ErrorCable;
import com.xiaominfo.oss.exception.ErrorConstant;
import com.xiaominfo.oss.module.model.OSSAppInfo;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.module.model.OSSInformation;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.service.*;
import com.xiaominfo.oss.sync.Sync;
import com.xiaominfo.oss.utils.CommonUtils;
import com.xiaominfo.oss.utils.IdGen;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/05/30 11:00
 */
@Service
public class MaterialServiceImpl extends RootApis implements MaterialService {

    @Autowired
    OSSMaterialInfoService ossMaterialInfoService;

    @Autowired
    OSSMaterialInfoSyncService ossMaterialInfoSyncService;

    @Autowired
    OSSDeveloperService ossDeveloperService;

    @Autowired
    OSSAppInfoService ossAppInfoService;

    @Autowired
    OSSInformationService ossInformationService;

    @Value(value = "${material.excludeFileTypes}")
    String excludeFileTypes;

    private Log log = LogFactory.get();


    @Override
    public List<FileBinaryResponse> saveAndStore(OSSInformation ossInformation, OSSDeveloper ossDeveloper, OSSAppInfo ossApp, File projectDirectory, List<FileBinaryRequest> fileBinaryRequests) throws IOException {
        List<String> excludeFileTypeLists = Lists.newArrayList(StrUtil.split(excludeFileTypes, ","));
        String ip = CommonUtils.getIpAddr(ThreadLocalHolder.getRequest());
        //存ossMaterialInfo对象
        List<OSSMaterialInfo> ossMaterialInfos = Lists.newArrayList();
        String root = ossInformation.getRoot();
        File rootFile = new File(root);
        int start = rootFile.getAbsolutePath().length();
        List<FileBinaryResponse> fileBinaryResponses = Lists.newArrayList();
        String currentTimeString = DateTime.now().toString("yyyyMMddHHmmss");
        //按月目录
        final String monthPath = currentTimeString.substring(0, 6);
        //按日目录,如W020151111
        final String dayPath = currentTimeString.substring(6, 8);
        final String fileDirectory = projectDirectory.getAbsolutePath() + "/" + monthPath + "/" + dayPath;
        log.info("directory:{}", fileDirectory);
        File saveFileDirectory = new File(fileDirectory);
        createDirectoryQuietly(saveFileDirectory);
        BigDecimal totalUseSpaceByDev = new BigDecimal(ossDeveloper.getUseSpace());
        BigDecimal appTotalUseSpace = new BigDecimal(ossApp.getUseSpace());
        for (FileBinaryRequest fileBinaryRequest : fileBinaryRequests) {
            if (excludeFileTypeLists.contains(fileBinaryRequest.getMediaType())) {
                throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "Your uploaded file may harm the server"));
            }
            String originalName = fileBinaryRequest.getOriginalName();
            byte[] bs = Base64.decodeBase64(fileBinaryRequest.getFile());
            //文件id
            String uuid = IdGen.getUUID();
            //生成uuid文件名称
            String fileName = uuid + "." + fileBinaryRequest.getMediaType();
            //静态资源存储路径
            StringBuffer storePathBuffer = new StringBuffer();
            String pre = projectDirectory.getAbsolutePath().substring(start);
            pre = com.xiaominfo.oss.utils.FileUtils.transforSysSpec(pre);
            if (!pre.startsWith("/")) {
                storePathBuffer.append("/");
            }
            storePathBuffer.append(pre).append("/")
                    .append(monthPath).append("/").append(dayPath).append("/").append(fileName);
            //判断最后一位是否是/
            String invokePath = ossInformation.getInvokingRoot();
            if (ossInformation.getInvokingRoot().endsWith("/")) {
                invokePath = ossInformation.getInvokingRoot().substring(0, invokePath.lastIndexOf("/"));
            }
            String url = invokePath + storePathBuffer.toString();
            File targetFile = new File(saveFileDirectory.getAbsolutePath() + "/" + fileName);
            //输出文件
            FileUtils.writeByteArrayToFile(targetFile, bs);
            //不可执行,防止恶意脚本攻击system
            com.xiaominfo.oss.utils.FileUtils.fileUnModified(targetFile);
            FileBinaryResponse fileBinaryResponse = new FileBinaryResponse(uuid, url, storePathBuffer.toString());
            //赋值其他属性
            fileBinaryResponse.setByteLength(targetFile.length());
            fileBinaryResponse.setByteToStr(com.xiaominfo.oss.utils.FileUtils.byteToString(targetFile.length()));
            fileBinaryResponse.setOriginalName(originalName);
            fileBinaryResponse.setObjType(fileBinaryRequest.getMediaType());
            fileBinaryResponses.add(fileBinaryResponse);
            //添加ossMaterial对象
            //追加
            totalUseSpaceByDev = totalUseSpaceByDev.add(new BigDecimal(targetFile.length()));
            //项目
            appTotalUseSpace = appTotalUseSpace.add(new BigDecimal(targetFile.length()));
            ossMaterialInfos.add(createTargetMaterial(ip, originalName, fileBinaryRequest.getMediaType(), ossApp, targetFile, fileBinaryResponse));
            //集群间同步
            Sync.syncSalve(root, targetFile, fileBinaryResponse.getId());
            //更新数据库
            ossMaterialInfoSyncService.createSync(fileBinaryResponse.getId(), "Master");
        }
        ossMaterialInfoService.insertBatch(ossMaterialInfos);
        //更新ossdev
        ossDeveloperService.updateById(new OSSDeveloper(ossDeveloper.getId(), totalUseSpaceByDev.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(totalUseSpaceByDev.longValue())));
        //更新项目占用空间
        ossAppInfoService.updateById(new OSSAppInfo(ossApp.getId(), appTotalUseSpace.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(appTotalUseSpace.longValue())));
        return fileBinaryResponses;
    }

    @Override
    public FileBinaryResponse saveAndStore(OSSInformation ossInformation, OSSDeveloper ossDeveloper, OSSAppInfo ossApp, NettyFileRequest ef, File file) throws IOException {
        //存ossMaterialInfo对象
        List<OSSMaterialInfo> ossMaterialInfos = Lists.newArrayList();
        String root = ossInformation.getRoot();
        File rootFile = new File(root);
        int start = rootFile.getAbsolutePath().length();
        List<FileBinaryResponse> fileBinaryResponses = Lists.newArrayList();

        BigDecimal totalUseSpaceByDev = new BigDecimal(ossDeveloper.getUseSpace());
        BigDecimal appTotalUseSpace = new BigDecimal(ossApp.getUseSpace());


        String originalName = ef.getOriginalName();

        //文件id
        String uuid = ef.getUuid();
        //静态资源存储路径
        String pre = ef.getProjectPath().substring(start);
        pre = com.xiaominfo.oss.utils.FileUtils.transforSysSpec(pre);
        //判断最后一位是否是/
        String invokePath = ossInformation.getInvokingRoot();
        if (ossInformation.getInvokingRoot().endsWith("/")) {
            invokePath = ossInformation.getInvokingRoot().substring(0, invokePath.lastIndexOf("/"));
        }
        String url = invokePath + pre;
        //输出文件
        File targetFile = new File(ef.getProjectPath());
        //不可执行,防止恶意脚本攻击system
        FileBinaryResponse fileBinaryResponse = new FileBinaryResponse(uuid, url, pre);
        //赋值其他属性
        fileBinaryResponse.setByteLength(targetFile.length());
        fileBinaryResponse.setByteToStr(com.xiaominfo.oss.utils.FileUtils.byteToString(targetFile.length()));
        fileBinaryResponse.setOriginalName(originalName);
        fileBinaryResponse.setObjType(ef.getMediaType());
        fileBinaryResponses.add(fileBinaryResponse);
        //添加ossMaterial对象
        //追加
        totalUseSpaceByDev = totalUseSpaceByDev.add(new BigDecimal(targetFile.length()));
        //项目
        appTotalUseSpace = appTotalUseSpace.add(new BigDecimal(targetFile.length()));
        ossMaterialInfos.add(createTargetMaterial("", originalName, ef.getMediaType(), ossApp, targetFile, fileBinaryResponse));
        //集群间同步
        log.info("开启同步 start");
        Sync.syncSalve(root, targetFile, fileBinaryResponse.getId());
        log.info("开启同步 end");
        //更新数据库
        ossMaterialInfoSyncService.createSync(fileBinaryResponse.getId(), "Master");
        ossMaterialInfoService.insertBatch(ossMaterialInfos);
        //更新ossdev
        ossDeveloperService.updateById(new OSSDeveloper(ossDeveloper.getId(), totalUseSpaceByDev.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(totalUseSpaceByDev.longValue())));
        //更新项目占用空间
        ossAppInfoService.updateById(new OSSAppInfo(ossApp.getId(), appTotalUseSpace.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(appTotalUseSpace.longValue())));
        return fileBinaryResponse;
    }

    @Override
    public List<FileBinaryResponse> saveAndStore(OSSInformation ossInformation, OSSDeveloper ossDeveloper, OSSAppInfo ossApp, File projectDirectory, MultipartFile[] multipartFiles) throws IOException {
        List<String> excludeFileTypeLists = Lists.newArrayList(StrUtil.split(excludeFileTypes, ","));
        String ip = CommonUtils.getIpAddr(ThreadLocalHolder.getRequest());
        //存ossMaterialInfo对象
        List<OSSMaterialInfo> ossMaterialInfos = Lists.newArrayList();
        String root = ossInformation.getRoot();
        File rootFile = new File(root);
        int start = rootFile.getAbsolutePath().length();
        List<FileBinaryResponse> fileBinaryResponses = Lists.newArrayList();
        String currentTimeString = DateTime.now().toString("yyyyMMddHHmmss");
        //按月目录
        final String monthPath = currentTimeString.substring(0, 6);
        //按日目录,如W020151111
        final String dayPath = currentTimeString.substring(6, 8);
        final String fileDirectory = projectDirectory.getAbsolutePath() + "/" + monthPath + "/" + dayPath;
        log.info("directory:{}", fileDirectory);
        File saveFileDirectory = new File(fileDirectory);
        createDirectoryQuietly(saveFileDirectory);
        BigDecimal totalUseSpaceByDev = new BigDecimal(ossDeveloper.getUseSpace());
        BigDecimal appTotalUseSpace = new BigDecimal(ossApp.getUseSpace());
        for (MultipartFile multpFile : multipartFiles) {
            String originalName = multpFile.getOriginalFilename();
            //文件id
            String uuid = IdGen.getUUID();
            String fileName = uuid;
            String mediaType = "";
            if (originalName.lastIndexOf(".") > 0) {
                mediaType = multpFile.getOriginalFilename().substring(multpFile.getOriginalFilename().lastIndexOf(".") + 1);
                fileName = fileName + "." + mediaType;
            }
            if (excludeFileTypeLists.contains(mediaType)) {
                throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "Your uploaded file may harm the server "));
            }
            //静态资源存储路径
            StringBuffer storePathBuffer = new StringBuffer();
            String pre = projectDirectory.getAbsolutePath().substring(start);
            pre = com.xiaominfo.oss.utils.FileUtils.transforSysSpec(pre);
            if (!pre.startsWith("/")) {
                storePathBuffer.append("/");
            }
            storePathBuffer.append(pre).append("/")
                    .append(monthPath).append("/").append(dayPath).append("/").append(fileName);
            //判断最后一位是否是/
            String invokePath = ossInformation.getInvokingRoot();
            if (ossInformation.getInvokingRoot().endsWith("/")) {
                invokePath = ossInformation.getInvokingRoot().substring(0, invokePath.lastIndexOf("/"));
            }
            String url = invokePath + storePathBuffer.toString();
            File targetFile = new File(saveFileDirectory.getAbsolutePath() + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(targetFile);
            InputStream ins = multpFile.getInputStream();
            byte[] byts = new byte[1024 * 1024];
            int len = -1;
            while ((len = ins.read(byts)) != -1) {
                fos.write(byts, 0, len);
            }
            //关闭输入输出流
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(ins);
            //不可执行,防止恶意脚本攻击system
            com.xiaominfo.oss.utils.FileUtils.fileUnModified(targetFile);
            FileBinaryResponse fileBinaryResponse = new FileBinaryResponse(uuid, url, storePathBuffer.toString());
            //赋值其他属性
            fileBinaryResponse.setByteLength(targetFile.length());
            fileBinaryResponse.setByteToStr(com.xiaominfo.oss.utils.FileUtils.byteToString(targetFile.length()));
            fileBinaryResponse.setOriginalName(originalName);
            fileBinaryResponse.setObjType(mediaType);
            fileBinaryResponses.add(fileBinaryResponse);
            //添加ossMaterial对象
            //追加
            totalUseSpaceByDev = totalUseSpaceByDev.add(new BigDecimal(targetFile.length()));
            //项目
            appTotalUseSpace = appTotalUseSpace.add(new BigDecimal(targetFile.length()));
            ossMaterialInfos.add(createTargetMaterial(ip, originalName, mediaType, ossApp, targetFile, fileBinaryResponse));
        }
        ossMaterialInfoService.insertBatch(ossMaterialInfos);
        //更新ossdev
        ossDeveloperService.updateById(new OSSDeveloper(ossDeveloper.getId(), totalUseSpaceByDev.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(totalUseSpaceByDev.longValue())));
        //更新项目占用空间
        ossAppInfoService.updateById(new OSSAppInfo(ossApp.getId(), appTotalUseSpace.longValue(), com.xiaominfo.oss.utils.FileUtils.byteToString(appTotalUseSpace.longValue())));
        return fileBinaryResponses;
    }


    private OSSMaterialInfo createTargetMaterial(String ip, String originalName, String mediaType, OSSAppInfo ossAppInfo, File targetFile, FileBinaryResponse fileBinaryResponse) {
        //添加ossMaterial对象
        OSSMaterialInfo ossMaterialInfo = new OSSMaterialInfo();
        ossMaterialInfo.setOriginalName(originalName);
        ossMaterialInfo.setFromIp(ip);
        ossMaterialInfo.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        ossMaterialInfo.setLastModifiedTime(ossMaterialInfo.getCreateTime());
        ossMaterialInfo.setStorePath(fileBinaryResponse.getStore());
        ossMaterialInfo.setUrl(fileBinaryResponse.getUrl());
        ossMaterialInfo.setId(fileBinaryResponse.getId());
        ossMaterialInfo.setLen(new BigDecimal(targetFile.length()).intValue());
        ossMaterialInfo.setByteStr(com.xiaominfo.oss.utils.FileUtils.byteToString(ossMaterialInfo.getLen()));
        ossMaterialInfo.setUserId(ossAppInfo.getDevId());
        ossMaterialInfo.setType(mediaType);
        ossMaterialInfo.setAppId(ossAppInfo.getId());
        return ossMaterialInfo;
    }

    @Override
    public List<FileBinaryResponse> saveAndStoreBySys(OSSInformation ossInformation, File projectDirectory, MultipartFile[] multipartFiles) throws IOException {
        String root = ossInformation.getRoot();
        File rootFile = new File(root);
        int start = rootFile.getAbsolutePath().length();
        List<FileBinaryResponse> fileBinaryResponses = Lists.newArrayList();
        //按日目录,如W020151111
        final String fileDirectory = projectDirectory.getAbsolutePath();
        log.info("directory:{}", fileDirectory);
        File saveFileDirectory = new File(fileDirectory);
        createDirectoryQuietly(saveFileDirectory);
        for (MultipartFile multpFile : multipartFiles) {
            //文件id
            String uuid = IdGen.getUUID();
            String fileName = uuid;
            String mediaType = "";
            if (multpFile.getOriginalFilename().lastIndexOf(".") > 0) {
                mediaType = multpFile.getOriginalFilename().substring(multpFile.getOriginalFilename().lastIndexOf(".") + 1);
                fileName = fileName + "." + mediaType;
            }
            //静态资源存储路径
            StringBuffer storePathBuffer = new StringBuffer();
            String pre = projectDirectory.getAbsolutePath().substring(start);
            pre = com.xiaominfo.oss.utils.FileUtils.transforSysSpec(pre);
            if (!pre.startsWith("/")) {
                storePathBuffer.append("/");
            }
            //判断最后一位是否是/
            String invokePath = ossInformation.getInvokingRoot();
            if (ossInformation.getInvokingRoot().endsWith("/")) {
                invokePath = ossInformation.getInvokingRoot().substring(0, invokePath.lastIndexOf("/"));
            }
            String url = invokePath + pre;
            File targetFile = new File(saveFileDirectory.getAbsolutePath() + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(targetFile);
            InputStream ins = multpFile.getInputStream();
            byte[] byts = new byte[1024 * 1024];
            int len = -1;
            while ((len = ins.read(byts)) != -1) {
                fos.write(byts, 0, len);
            }
            //关闭输入输出流
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(ins);
            //不可执行,防止恶意脚本攻击system
            targetFile.setExecutable(false);
            FileBinaryResponse fileBinaryResponse = new FileBinaryResponse(uuid, url, storePathBuffer.toString());
            fileBinaryResponses.add(fileBinaryResponse);
        }
        return fileBinaryResponses;
    }
}
