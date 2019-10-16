package com.xiaominfo.oss.sdk.upload;

import com.xiaominfo.oss.sdk.OSSClientProperty;
import com.xiaominfo.oss.sdk.client.FileBytesRequest;
import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;

import java.io.File;

public interface IUpload {


    /**
     * 文件上传
     *
     * @return
     */
    OSSClientMessage<FileBytesResponse> upload(File file);


}
