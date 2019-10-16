package com.xiaominfo.oss.sdk.upload.handle.netty;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class NettyMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private File file;// 文件
    private String filePath;// 文件路径
    private String materialId;//对象的id
    private String file_md5;// 文件名
    private int starPos;// 开始位置
    private byte[] bytes;// 文件字节数组
    private int endPos;// 结尾位置
    private long length;// 文件总长度
    private String url;

    private String code;//通讯的code
    private String message;//通讯的message

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFile_md5() {
        return file_md5;
    }

    public void setFile_md5(String file_md5) {
        this.file_md5 = file_md5;
    }

    public int getStarPos() {
        return starPos;
    }

    public void setStarPos(int starPos) {
        this.starPos = starPos;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "NettyMessage{" +
                "file=" + file +
                ", filePath='" + filePath + '\'' +
                ", materialId='" + materialId + '\'' +
                ", file_md5='" + file_md5 + '\'' +
                ", starPos=" + starPos +
                ", bytes=" + Arrays.toString(bytes) +
                ", endPos=" + endPos +
                ", url='" + url + '\'' +
                '}';
    }
}

