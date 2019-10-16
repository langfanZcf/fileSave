/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaominfo.oss.common.annotation.NotLogin;
import com.xiaominfo.oss.common.pojo.Pagination;
import com.xiaominfo.oss.common.pojo.RestfulMessage;
import com.xiaominfo.oss.domain.FileBinaryRequest;
import com.xiaominfo.oss.domain.FileBinaryResponse;
import com.xiaominfo.oss.exception.AssemblerException;
import com.xiaominfo.oss.exception.ErrorCable;
import com.xiaominfo.oss.exception.ErrorConstant;
import com.xiaominfo.oss.module.entity.OSSMaterialInfoResult;
import com.xiaominfo.oss.module.model.OSSAppInfo;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.module.model.OSSInformation;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import com.xiaominfo.oss.service.*;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.net.URLCodec;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/05/30 10:47
 */

@RestController
@RequestMapping("/oss/material")
public class MaterialApplication extends RootApis {

    private Log log = LogFactory.get();

    @Autowired
    MaterialService materialService;

    @Autowired
    OSSMaterialInfoService ossMaterialInfoService;

    @Autowired
    OSSDeveloperService ossDeveloperService;

    @Autowired
    OSSAppInfoService ossAppInfoService;

    @Autowired
    OSSInformationService ossInformationService;

    @Autowired
    ExcelService excelService;

    @GetMapping("/queryByPage")
    public Pagination<OSSMaterialInfoResult> queryByPage(OSSMaterialInfo ossMaterialInfo,
                                                         @RequestParam(value = "page", defaultValue = "1") Integer current_page,
                                                         @RequestParam(value = "rows", defaultValue = "10") Integer page_size) {
        return ossMaterialInfoService.queryByPage(ossMaterialInfo, current_page, page_size);
    }

    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            String fileName = System.currentTimeMillis() + "-oss-server.xls";
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            Workbook workbook = excelService.exportMaterials();
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            log.error(e);

        }
    }

    @PostMapping(value = "/createDir")
    public RestfulMessage createDirectory(@RequestParam(value = "dir", required = false) String dir,
                                          @RequestParam(value = "pro") String pro) {
        RestfulMessage restfulMessage = new RestfulMessage();
        try {
            String root = ossInformationService.queryOne().getRoot();
            StringBuffer dirStr = new StringBuffer();
            dirStr.append(root).append(dir);
            validateProjectName(pro);
            dirStr.append("/").append(pro);
            File file = new File(dirStr.toString());
            createDirectoryQuietly(file);
            file.setExecutable(false);
            file.setWritable(false);
            file.setReadOnly();
            successResultCode(restfulMessage);
        } catch (Exception e) {
            restfulMessage = wrapperException(e);
        }
        return restfulMessage;
    }


    /***
     * byte 字节码上传文件
     * 文件夹名称：只能是数组+字母+下划线组成
     * 传输文件格式：
     * {
     *     "project":"",
     *     "appid":"",
     *     "appsecret":"",
     *     "module":"",
     *     "files":[
     *     {
     *         "original_name":"test.png",
     *         "file":"F://test.png",
     *         "media_type":"png"
     *     }
     *     ]
     * }
     * @param entity
     * @return
     */
    @NotLogin
    @PostMapping(value = "/uploadByBinary", produces = "application/json;charset=UTF-8")
    public RestfulMessage upload(HttpEntity<String> entity) {
        RestfulMessage restfulMessage = new RestfulMessage();
        try {
            log.info("/uploadByBinary...");
            String bodyStr = entity.getBody();
            String decodeBodyStr = new String(URLCodec.decodeUrl(bodyStr.getBytes("UTF-8")), "UTF-8");
            //获取project
            JSONObject reqJson = JSONObject.parseObject(decodeBodyStr);

            if (reqJson == null || reqJson.isEmpty() || ("null".equals(reqJson.toString())) || (reqJson.toString() == null)) {
                throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "not any data"));
            }
            //获取appid "project"
            String moduleStr = "";
            String appId = reqJson.getString("appid");
            String appSecret = reqJson.getString("appsecret");
            String project = reqJson.getString("project");
            JSONArray files = reqJson.getJSONArray("files");

            assertArgumentNotEmpty(appId, "appid can't be empty");
            assertArgumentNotEmpty(appSecret, "appsecret can't be empty");
            assertArgumentNotEmpty(project, "project name can't be empty");
            assertArgumentNotEmpty(files, "files can't be empty");

            if (!StringUtil.isNullOrEmpty(reqJson.getString("module"))) {
                moduleStr = reqJson.getString("module");
            }

            OSSDeveloper ossDeveloper = ossDeveloperService.queryByAppid(appId, appSecret);
            assertArgumentNotEmpty(ossDeveloper, "appid or appsecret is invalid");
            String projectFilePathName = project;
            validateProjectName(projectFilePathName);
            //判断文件夹code是否相等
            List<OSSAppInfo> ossAppInfos = ossAppInfoService.queryByDevIds(ossDeveloper.getId());
            if (ossAppInfos == null || ossAppInfos.size() == 0) {
                throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
            }
            OSSAppInfo ossApp = null;
            boolean flag = false;
            for (OSSAppInfo ossAppInfo : ossAppInfos) {
                if (StrUtil.equalsIgnoreCase(ossAppInfo.getCode(), projectFilePathName)) {
                    flag = true;
                    ossApp = ossAppInfo;
                    break;
                }
            }
            if (!flag) {
                throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
            }
            OSSInformation ossInformation = ossInformationService.queryOne();
            String root = ossInformation.getRoot();
            //验证文件夹规则,不能包含特殊字符
            File file = new File(root);
            createDirectoryQuietly(file);
            StringBuffer path = new StringBuffer();
            path.append(file.getAbsolutePath());
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
            createDirectoryQuietly(projectFile);

            List<FileBinaryRequest> materials = JSONObject.parseArray(files.toJSONString(), FileBinaryRequest.class);
            for (FileBinaryRequest fileBinaryRequest : materials) {
                assertArgumentNotEmpty(fileBinaryRequest.getMediaType(), "media_type is requried ...");
                assertArgumentNotEmpty(fileBinaryRequest.getFile(), "Not found file bytes, but it is requried ...");
                //fileBinaryRequest.setFileBytes(material.getFileBytes().replaceAll(" ", "+"));
                fileBinaryRequest.setFile(fileBinaryRequest.getFile().replaceAll(" ", "+"));
            }
            List<FileBinaryResponse> fileBinaryResponseList = materialService.saveAndStore(ossInformation, ossDeveloper, ossApp, projectFile, materials);
            restfulMessage.setData(fileBinaryResponseList);
            successResultCode(restfulMessage);
        } catch (Exception e) {
            restfulMessage = wrapperException(e);
        }
        return restfulMessage;
    }


    /***
     * 表单提交,上传
     * @param project
     * @param files
     * @return
     * @Deprecated since v1.1 not used,use upload method
     */
    @Deprecated
    @NotLogin
    @PostMapping("{project}/uploadMaterial")
    public RestfulMessage uploadMaterial(@PathVariable(value = "project") String project,
                                         @RequestParam(value = "module", required = false) String module,
                                         @RequestParam(value = "appid", required = false) String appid,
                                         @RequestParam(value = "appsecret", required = false) String appsecret, @RequestParam(value = "file") MultipartFile[] files) {
        RestfulMessage restfulMessage = new RestfulMessage();
        try {
            restfulMessage = uploadFileByForm(project, appid, appsecret, module, files);
        } catch (Exception e) {
            restfulMessage = wrapperException(e);
        }
        return restfulMessage;
    }


    /***
     * 固定表单上传接口url
     * @return
     */
    @NotLogin
    @PostMapping("/uploadMaterialNonProUrl")
    public RestfulMessage uploadMaterialNonProUrl(@RequestParam(value = "project", required = false) String project,
                                                  @RequestParam(value = "module", required = false) String module,
                                                  @RequestParam(value = "appid", required = false) String appid,
                                                  @RequestParam(value = "appsecret", required = false) String appsecret, @RequestParam(value = "file") MultipartFile[] files) {
        RestfulMessage restfulMessage = new RestfulMessage();
        try {
            restfulMessage = uploadFileByForm(project, appid, appsecret, module, files);
        } catch (Exception e) {
            restfulMessage = wrapperException(e);
        }
        return restfulMessage;
    }


    private RestfulMessage uploadFileByForm(String project, String appid, String appsecret, String module, MultipartFile[] files) throws IOException {
        RestfulMessage restfulMessage = new RestfulMessage();
        assertArgumentNotEmpty(project, "project can't be empty!!!");
        assertArgumentNotEmpty(appid, "appid can't be empty ");
        assertArgumentNotEmpty(appsecret, "appsecret can't be empty ");
        OSSDeveloper ossDeveloper = ossDeveloperService.queryByAppid(appid, appsecret);
        assertArgumentNotEmpty(ossDeveloper, "appid or appsecret is invalid");
        //判断文件夹code是否相等
        List<OSSAppInfo> ossAppInfos = ossAppInfoService.queryByDevIds(ossDeveloper.getId());
        if (ossAppInfos == null || ossAppInfos.size() == 0) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
        }
        boolean flag = false;
        OSSAppInfo ossApp = null;
        for (OSSAppInfo ossAppInfo : ossAppInfos) {
            if (StrUtil.equalsIgnoreCase(ossAppInfo.getCode(), project)) {
                flag = true;
                ossApp = ossAppInfo;
                break;
            }
        }
        if (!flag) {
            throw new AssemblerException(new ErrorCable(ErrorConstant.REQUEST_PARAMS_NOT_VALID, "You do not have permission to upload files"));
        }
        validateProjectName(project);
        OSSInformation ossInformation = ossInformationService.queryOne();
        String root = ossInformation.getRoot();
        //验证文件夹规则,不能包含特殊字符
        File file = new File(root);
        createDirectoryQuietly(file);
        StringBuffer path = new StringBuffer();
        path.append(file.getAbsolutePath());
        path.append(File.separator);
        path.append(project);
        if (StrUtil.isNotEmpty(module)) {
            if (!module.startsWith("/")) {
                path.append("/");
            }
            path.append(module);
        }
        log.info("path:{}", path);
        File projectFile = new File(path.toString());
            /*if (!projectFile.exists()){
                throw new AssemblerException(new ErrorCable(ErrorConstant.AUTHENTICATION_FAILED,"You do not have operating authority for this directory "+project+", or the directory was not created"));
            }*/
        createDirectoryQuietly(projectFile);
        List<FileBinaryResponse> fileBinaryResponseList = materialService.saveAndStore(ossInformation, ossDeveloper, ossApp, projectFile, files);
        restfulMessage.setData(fileBinaryResponseList);
        successResultCode(restfulMessage);
        return restfulMessage;
    }


    @PostMapping("/uploadBySys")
    public RestfulMessage uploadSys(@RequestParam(value = "dir", required = false) String dir, @RequestParam(value = "file") MultipartFile[] files) {
        RestfulMessage restfulMessage = new RestfulMessage();
        try {
            OSSInformation ossInformation = ossInformationService.queryOne();
            String root = ossInformation.getRoot();
            //验证文件夹规则,不能包含特殊字符
            File file = new File(root);
            createDirectoryQuietly(file);
            StringBuffer path = new StringBuffer();
            path.append(file.getAbsolutePath());
            if (StrUtil.isNotEmpty(dir)) {
                if (!dir.startsWith("/")) {
                    path.append(File.separator);
                }
                path.append(dir);
            }
            log.info("path:{}", path);
            File projectFile = new File(path.toString());
            createDirectoryQuietly(projectFile);
            List<FileBinaryResponse> fileBinaryResponseList = materialService.saveAndStoreBySys(ossInformation, projectFile, files);
            restfulMessage.setData(fileBinaryResponseList);
            successResultCode(restfulMessage);
        } catch (Exception e) {
            restfulMessage = wrapperException(e);
        }
        return restfulMessage;
    }
}
