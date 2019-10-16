/*
 * Copyright (C) 2018 Zhejiang lishiots Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.lishiots.com.
 * Developer Web Site: http://open.lishiots.com.
 */

package com.xiaominfo.oss.sdk;

import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;
import com.xiaominfo.oss.sdk.upload.IUpload;
import com.xiaominfo.oss.sdk.upload.handle.Base64UploadHandle;
import com.xiaominfo.oss.sdk.upload.handle.NettyUploadHandle;

import java.io.File;

/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/05/30 17:00
 */
public class OSSClient {
    private static final String v1_upload_file_binary_api = "/oss/material/uploadByBinary";
    private OSSClientProperty ossClientProperty;
    private String mode = "NETTY";//支持base64 和 netty两种方式上传，默认netty

    public OSSClient(OSSClientProperty ossClientProperty, String mode) {
        this.ossClientProperty = ossClientProperty;
        this.mode = mode;
        //初始化上传url地址
        if (ossClientProperty != null && ossClientProperty.getRemote() != null && !"".equalsIgnoreCase(ossClientProperty.getRemote())) {
            StringBuffer endpoint = new StringBuffer();
            endpoint.append(ossClientProperty.getRemote());
            if (ossClientProperty.getRemote().endsWith("/")) {
                endpoint.append(v1_upload_file_binary_api.substring(1));
            } else {
                endpoint.append(v1_upload_file_binary_api);
            }
            if ("BASE64".equalsIgnoreCase(mode)){
                this.ossClientProperty.setRemote(endpoint.toString());
            }
        }
    }

    public OSSClient(OSSClientProperty ossClientProperty) {
        this.ossClientProperty = ossClientProperty;
        this.mode = mode;
        //初始化上传url地址
        if (ossClientProperty != null && ossClientProperty.getRemote() != null && !"".equalsIgnoreCase(ossClientProperty.getRemote())) {
            StringBuffer endpoint = new StringBuffer();
            endpoint.append(ossClientProperty.getRemote());
            if (ossClientProperty.getRemote().endsWith("/")) {
                endpoint.append(v1_upload_file_binary_api.substring(1));
            } else {
                endpoint.append(v1_upload_file_binary_api);
            }

            if ("BASE64".equalsIgnoreCase(mode)){
                this.ossClientProperty.setRemote(endpoint.toString());
            }
        }
    }

    /***
     * 文件上传
     * @param file
     * @return
     */
    public OSSClientMessage<FileBytesResponse> uploadFile(File file) {
        OSSClientMessage<FileBytesResponse> ossClientMessage = new OSSClientMessage<>();
        try {
            if (file.isDirectory()) {
                //文件不能是目录
                throw new RuntimeException("file can't be directory ");
            }
            IUpload handle = null;
            switch (mode) {
                case "BASE64":
                    handle = new Base64UploadHandle(ossClientProperty);
                    break;
                default:
                    handle = new NettyUploadHandle(ossClientProperty);
                    break;
            }
            ossClientMessage = handle.upload(file);
        } catch (Exception e) {
            ossClientMessage.setCode("8500");
            ossClientMessage.setMessage(e.getMessage());
        }
        return ossClientMessage;
    }
}
