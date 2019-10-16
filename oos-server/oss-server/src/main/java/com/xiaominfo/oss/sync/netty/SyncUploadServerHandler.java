package com.xiaominfo.oss.sync.netty;

import com.xiaominfo.oss.common.SpringContextUtil;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.service.OSSInformationService;
import com.xiaominfo.oss.service.OSSMaterialInfoSyncService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

public class SyncUploadServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "F:";
    private Logger log = LoggerFactory.getLogger(SalveServerHandler.class);
    private OSSMaterialInfoSyncService ossMaterialInfoSyncService = SpringContextUtil.getBean(OSSMaterialInfoSyncService.class);
    private OSSInformationService ossInformationService = SpringContextUtil.getBean(OSSInformationService.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("服务端2：channelActive()");
        file_dir = ossInformationService.queryOne().getRoot();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("服务端2：channelInactive()");
        ctx.flush();
        ctx.close();

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyFileRequest) {
            NettyFileRequest ef = (NettyFileRequest) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();

            String path = file_dir + File.separator + ef.getFile().getName();
            File file = new File(path);
            createDirectoryQuietly(file);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);
            start = start + byteRead;
            log.debug("path:" + path + "," + byteRead);
            if (byteRead > 0) {
                ctx.writeAndFlush(start);
                randomAccessFile.close();
                if (byteRead != 1024 * 10) {
                    channelInactive(ctx);
                }
            } else {
                ctx.close();
            }
            log.info("SyncMessage:{}" + ef);
        }
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

