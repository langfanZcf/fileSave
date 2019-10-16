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
        String url = "http://127.0.0.1:18000";
        OSSClientProperty ossClientProperty = new OSSClientProperty(url, "province_IIII");
        ossClientProperty.setAppid("zh");
        ossClientProperty.setAppsecret("123123");
        OSSClient ossClient = new OSSClient(ossClientProperty, "BASE64");
        //File uploadFile=new File("C:\\Users\\xiaoymin\\Desktop\\aa.jpg");
        //File uploadFile = new File("D:\\source\\oss-server\\static\\wechat.jpg");
        File uploadFile = new File("F:\\得仕\\页面\\得仕宝前台页面改动效果图-20150430\\效果图 得仕宝产品列表页-修改后.png");

        OSSClientMessage<FileBytesResponse> ossClientMessage = ossClient.uploadFile(uploadFile);

        System.out.println(JSON.toJSONString(ossClientMessage));
    }

    @Test
    public void nettyUploadFile() {
        //客户端上传
        String url = "127.0.0.1:18001";
        OSSClientProperty ossClientProperty = new OSSClientProperty(url, "province_IIII");
        ossClientProperty.setAppid("zh");
        ossClientProperty.setAppsecret("123123");
        OSSClient ossClient = new OSSClient(ossClientProperty);
        //File uploadFile = new File("D:\\source\\oss-server\\static\\wechat.jpg");
        File uploadFile = new File("F:\\得仕\\页面\\得仕宝前台页面改动效果图-20150430\\效果图 得仕宝产品列表页-修改后.png");

        OSSClientMessage<FileBytesResponse> ossClientMessage = ossClient.uploadFile(uploadFile);

        System.out.println("===========================================");
        System.out.println(JSON.toJSONString(ossClientMessage));
    }
}
