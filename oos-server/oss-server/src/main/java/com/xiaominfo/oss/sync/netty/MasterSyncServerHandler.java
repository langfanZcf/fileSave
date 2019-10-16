package com.xiaominfo.oss.sync.netty;

import com.xiaominfo.oss.common.SpringContextUtil;
import com.xiaominfo.oss.domain.FileBinaryResponse;
import com.xiaominfo.oss.exception.AssemblerException;
import com.xiaominfo.oss.exception.ErrorConstant;
import com.xiaominfo.oss.module.model.OSSAppInfo;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.module.model.OSSInformation;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.sdk.upload.handle.netty.NettyMessage;
import com.xiaominfo.oss.service.MaterialService;
import com.xiaominfo.oss.service.OSSInformationService;
import com.xiaominfo.oss.service.OSSMaterialInfoSyncService;
import com.xiaominfo.oss.sync.SyncMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

public class MasterSyncServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "F:";
    private Logger log = LoggerFactory.getLogger(SalveServerHandler.class);
    private OSSMaterialInfoSyncService ossMaterialInfoSyncService = SpringContextUtil.getBean(OSSMaterialInfoSyncService.class);
    private OSSInformationService ossInformationService = SpringContextUtil.getBean(OSSInformationService.class);
    private MaterialService materialService = SpringContextUtil.getBean(MaterialService.class);


    private OSSInformation ossInformation = new OSSInformation();
    private OSSDeveloper ossDeveloper = new OSSDeveloper();
    private OSSAppInfo ossApp = new OSSAppInfo();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("服务端1：channelActive()");
        file_dir = ossInformationService.queryOne().getRoot();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("服务端1：channelInactive()");
        ctx.flush();
        ctx.close();

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof SyncMessage) {
            readSyncMessage(ctx, (SyncMessage) msg);
        } else if (msg instanceof NettyFileRequest) {
            uploadByBinary(ctx, (NettyFileRequest) msg);

        }
    }

    public void uploadByBinary(ChannelHandlerContext ctx, NettyFileRequest ef) throws Exception {
        try {
            String path = ossMaterialInfoSyncService.buildPath(ef, ossInformation, ossDeveloper, ossApp);
            ef.setProjectPath(path);
            ef.setCode("0000");
            //保存文件
            readNettyFile(ctx, ef);
        } catch (AssemblerException e) {
            log.error("生成路径失败:", e);
            writeErrorLogAndFlush(ctx, ErrorConstant.INTERNAL_SERVER_ERROR + "", e.getMessage());
        } catch (Exception e) {
            log.error("生成路径失败:", e);
            writeErrorLogAndFlush(ctx, ErrorConstant.INTERNAL_SERVER_ERROR + "", "系统异常，请联系管理员");
        }
    }

    private void writeErrorLogAndFlush(ChannelHandlerContext ctx, String code, String message) throws Exception {
        NettyMessage nettyMessage = new NettyMessage();
        nettyMessage.setCode(code);
        nettyMessage.setMessage(message);
        ctx.writeAndFlush(nettyMessage);
        channelInactive(ctx);
    }

    public void readNettyFile(ChannelHandlerContext ctx, NettyFileRequest ef) throws Exception {
        byte[] bytes = ef.getBytes();
        byteRead = ef.getEndPos();

        File file = new File(ef.getProjectPath());
        createDirectoryQuietly(file);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(start);
        randomAccessFile.write(bytes);
        start = start + byteRead;
        //log.debug("path:" + ef.getProjectPath() + "," + byteRead);
        if (byteRead > 0) {
            NettyMessage message = new NettyMessage();
            message.setStarPos(start);
            message.setMaterialId(ef.getUuid());
            message.setFilePath(ef.getProjectPath());
            message.setCode("0000");

            if (byteRead != 1024 * 10) {
                //最后一次上传，保存数据
                FileBinaryResponse response = materialService.saveAndStore(ossInformation, ossDeveloper, ossApp, ef, file);
                message.setMaterialId(response.getId());
                message.setUrl(response.getUrl());
                message.setFilePath(response.getStore());
            }
            ctx.writeAndFlush(message);
            randomAccessFile.close();

            if (byteRead != 1024 * 10) {
                channelInactive(ctx);
            }
        } else {
            ctx.close();
        }
    }

    public void readSyncMessage(ChannelHandlerContext ctx, SyncMessage ef) throws Exception {
        byte[] bytes = ef.getBytes();
        byteRead = ef.getEndPos();
        String md5 = ef.getFile_md5();//文件名
        String path = file_dir + File.separator + ef.getFilePath() + ef.getFile().getName();
        File file = new File(path);
        createDirectoryQuietly(file);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(start);
        randomAccessFile.write(bytes);
        start = start + byteRead;
       // log.debug("path:" + path + "," + byteRead);
        if (byteRead > 0) {
            ctx.writeAndFlush(start);
            randomAccessFile.close();
            if (byteRead != 1024 * 10) {
                //更新数据库
                ossMaterialInfoSyncService.createSync(ef.getMaterialId(), "Salve");
                channelInactive(ctx);
            }
        } else {
            //ctx.flush();
            ctx.close();
        }
        log.info("SyncMessage:{}" + ef);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        log.info("FileUploadServerHandler--exceptionCaught()");
    }

    /***
     * 创建文件夹
     * @param files
     */
    protected void createDirectoryQuietly(File files) {
        File file = new File(files.getParent());
        try {
            if (file != null) {
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new RuntimeException(file.getName() + " is invalid,can't be create directory");
                    }
                }
            }
        } finally {
            if (file != null) {
                file.setWritable(false);
                file.setExecutable(false);
                file.setReadOnly();
            }

        }

    }
}

