/*
 * Copyright (C) 2018 Zhejiang lishiots Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.lishiots.com.
 * Developer Web Site: http://open.lishiots.com.
 */

package com.xiaominfo.oss.sdk.client;


/***
 *
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/05/30 15:12
 */
public class FileBytesRequest {

    private String originalName;

    /***
     * 文件的base64文件字符码
     * File uploadFile=new File(...)
     * String file=Base64.encodeBase64String(org.apache.commons.io.FileUtils.FileUtils.readFileToByteArray(uploadFile));
     */
    private String file;

    private String mediaType;
    /**
     * 模块的名称
     */
    private String module;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
