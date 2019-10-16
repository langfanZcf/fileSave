package com.xiaominfo.oss.sdk.upload.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaominfo.oss.sdk.OSSClientProperty;
import com.xiaominfo.oss.sdk.client.FileBytesRequest;
import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;
import com.xiaominfo.oss.sdk.upload.IUpload;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Base64UploadHandle implements IUpload {
    public OSSClientProperty ossClientProperty;

    public Base64UploadHandle(OSSClientProperty ossClientProperty) {
        this.ossClientProperty = ossClientProperty;
    }

    /***
     * 字节字符串形式上传文件
     * @param file
     * @return
     */

    @Override
    public OSSClientMessage<FileBytesResponse> upload(File file) {
        OSSClientMessage<FileBytesResponse> ossClientMessage = new OSSClientMessage<>();
        CloseableHttpResponse closeableHttpResponse = null;
        CloseableHttpClient httpClient = null;
        try {
            //获取文件原始名称
            String originalName = file.getName();
            String mediaType = "unkown";
            int idx = originalName.lastIndexOf(".");
            if (idx > 0) {
                mediaType = originalName.substring(idx + 1);
            }
            String filebyteString = Base64.encodeBase64String(FileUtils.readFileToByteArray(file));
            FileBytesRequest fileBytesRequest = new FileBytesRequest();
            fileBytesRequest.setFile(filebyteString);
            fileBytesRequest.setMediaType(mediaType);
            fileBytesRequest.setOriginalName(originalName);


            HttpPost request = new HttpPost(ossClientProperty.getRemote());
            addDefaultHeader(request);
            httpClient = HttpClients.createDefault();
            List<FileBytesRequest> fileBytesRequests = new ArrayList<>();
            fileBytesRequests.add(fileBytesRequest);
            addRequestParam(request, fileBytesRequests, ossClientProperty.getProject());
            closeableHttpResponse = httpClient.execute(request);
            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String content = EntityUtils.toString(closeableHttpResponse.getEntity(), "UTF-8");
                if (content != null && !"".equals(content)) {
                    JSONObject jsonObject = JSONObject.parseObject(content);
                    String code = jsonObject.get("code").toString();
                    String message = jsonObject.get("message").toString();
                    String data = jsonObject.get("data") + "";
                    ossClientMessage.setCode(code);
                    ossClientMessage.setMessage(message);

                    List<FileBytesResponse> fileBytesResponse = JSONArray.parseArray(data, FileBytesResponse.class);

                    if (fileBytesResponse != null && fileBytesResponse.size() > 0) {
                        ossClientMessage.setData(fileBytesResponse.get(0));
                    }
                }
            }
        } catch (Exception e) {
            handleServerExceptionMessage(ossClientMessage, e);
        }
        return ossClientMessage;
    }

    /***
     * 设置默认header
     * @param request
     */
    private void addDefaultHeader(HttpUriRequest request) {
        request.addHeader("Content-Encoding", "gzip");
        request.addHeader("Content-type", "application/json");
    }

    private void handleServerExceptionMessage(OSSClientMessage ossClientMessage, Exception e) {
        ossClientMessage.setCode("8500");
        ossClientMessage.setMessage(e.getMessage());
    }

    private void addRequestParam(HttpPost request, List<FileBytesRequest> fileBytesRequests, String project) {
        JSONObject param = createRequestParams(fileBytesRequests, ossClientProperty.getProject());
        request.setEntity(new StringEntity(param.toString(), "UTF-8"));
    }

    /**
     * 创建请求参数
     *
     * @param fileBytesRequests
     * @param project
     * @return
     */
    private JSONObject createRequestParams(List<FileBytesRequest> fileBytesRequests, String project) {
        JSONObject param = new JSONObject();
        param.put("project", project);
        param.put("appid", ossClientProperty.getAppid());
        param.put("appsecret", ossClientProperty.getAppsecret());
        param.put("files", createFileArrs(fileBytesRequests));
        return param;
    }


    /***
     * 创建数组
     * @param fileBytesRequests
     * @return
     */
    private JSONArray createFileArrs(List<FileBytesRequest> fileBytesRequests) {
        JSONArray jsonArray = new JSONArray();
        for (FileBytesRequest fileBytesRequest : fileBytesRequests) {
            JSONObject fileObj = new JSONObject();
            fileObj.put("media_type", fileBytesRequest.getMediaType());
            fileObj.put("file", fileBytesRequest.getFile());
            fileObj.put("original_name", fileBytesRequest.getOriginalName());
            jsonArray.add(fileObj);
        }
        return jsonArray;
    }

}
