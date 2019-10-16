package com.xiaominfo.oss.sdk.upload.handle;


import com.xiaominfo.oss.sdk.OSSClientProperty;
import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;
import com.xiaominfo.oss.sdk.upload.IUpload;
import com.xiaominfo.oss.sdk.upload.handle.netty.NettyClient;

import java.io.File;

public class NettyUploadHandle implements IUpload {
    public OSSClientProperty ossClientProperty;

    public NettyUploadHandle(OSSClientProperty ossClientProperty) {
        this.ossClientProperty = ossClientProperty;
    }

    @Override
    public OSSClientMessage<FileBytesResponse> upload(File file) {
        OSSClientMessage<FileBytesResponse> ossClientMessage = new OSSClientMessage<>();

        try {
            //获取文件原始名称
            NettyFileRequest nettyFileRequest = initNettyFileRequest(file);
            String[] node = ossClientProperty.getRemote().split(":");

            //调用
            NettyClient client = new NettyClient(node[0], Integer.parseInt(node[1]));
            client.connect(nettyFileRequest);
            ossClientMessage = client.getResponse();
        } catch (Exception e) {
            ossClientMessage.setCode("8500");
            ossClientMessage.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return ossClientMessage;
    }

    private NettyFileRequest initNettyFileRequest(File file) {
        NettyFileRequest nettyFileRequest = new NettyFileRequest();
        //基础参数
        nettyFileRequest.setAppid(ossClientProperty.getAppid());
        nettyFileRequest.setAppsecret(ossClientProperty.getAppsecret());
        nettyFileRequest.setProject(ossClientProperty.getProject());
        //文件信息
        String originalName = file.getName();
        String mediaType = "unkown";
        int idx = originalName.lastIndexOf(".");
        if (idx > 0) {
            mediaType = originalName.substring(idx + 1);
        }
        nettyFileRequest.setOriginalName(originalName);
        nettyFileRequest.setMediaType(mediaType);
        nettyFileRequest.setFile(file);
        return nettyFileRequest;
    }
}
