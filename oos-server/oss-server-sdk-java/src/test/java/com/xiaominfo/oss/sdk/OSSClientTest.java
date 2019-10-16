package com.xiaominfo.oss.sdk;

import com.alibaba.fastjson.JSON;
import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;
import org.junit.Test;

import java.io.File;

public class OSSClientTest {

    @Test
    public void uploadFile() {
        //客户端上传
        String url = "http://192.168.56.1:18000";
        OSSClientProperty ossClientProperty = new OSSClientProperty(url, "test");
        ossClientProperty.setAppid("osss2su1a");
        ossClientProperty.setAppsecret("40y40qyy");
        OSSClient ossClient = new OSSClient(ossClientProperty, "BASE64");
        //File uploadFile=new File("C:\\Users\\xiaoymin\\Desktop\\aa.jpg");
        //File uploadFile = new File("D:\\source\\oss-server\\static\\wechat.jpg");
        File uploadFile = new File("F:\\证件照.jpg");

        OSSClientMessage<FileBytesResponse> ossClientMessage = ossClient.uploadFile(uploadFile);

        System.out.println("response,result={}"+JSON.toJSONString(ossClientMessage));
    }
    @Test
    public void nettyUploadFile() {
        //客户端上传
        String url = "192.168.56.1:18001";
        OSSClientProperty ossClientProperty = new OSSClientProperty(url, "ditiexiangmu");
        ossClientProperty.setAppid("osss2su1a");
        ossClientProperty.setAppsecret("40y40qyy");
        OSSClient ossClient = new OSSClient(ossClientProperty);
        //File uploadFile = new File("D:\\source\\oss-server\\static\\wechat.jpg");
        File uploadFile = new File("F:\\证件照.jpg");

        OSSClientMessage<FileBytesResponse> ossClientMessage = ossClient.uploadFile(uploadFile);

        System.out.println("===========================================");
        System.out.println(JSON.toJSONString(ossClientMessage));
    }
}
