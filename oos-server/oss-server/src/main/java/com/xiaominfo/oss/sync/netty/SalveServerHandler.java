package com.xiaominfo.oss.sync.netty;

import com.xiaominfo.oss.sync.SyncMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SalveServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private SyncMessage syncMessage;
    private Logger log = LoggerFactory.getLogger(SalveServerHandler.class);

    public SalveServerHandler(SyncMessage ef) {
        if (ef.getFile().exists()) {
            if (!ef.getFile().isFile()) {
                log.info("Not a file :{}", ef.getFile());
                return;
            }
        }
        this.syncMessage = ef;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        log.info("客户端结束传递文件channelInactive()");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            randomAccessFile = new RandomAccessFile(syncMessage.getFile(),
                    "r");
            randomAccessFile.seek(syncMessage.getStarPos());
            // lastLength = (int) randomAccessFile.length() / 10;
            lastLength = 1024 * 10;
            byte[] bytes = new byte[lastLength];
            if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                syncMessage.setEndPos(byteRead);
                syncMessage.setBytes(bytes);
                ctx.writeAndFlush(syncMessage);
            } else {
            }
           // log.info("channelActive()文件已经读完 " + byteRead);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof Integer) {
            start = (Integer) msg;
            if (start != -1) {
                randomAccessFile = new RandomAccessFile(
                        syncMessage.getFile(), "r");
                randomAccessFile.seek(start);
                //log.debug("长度：" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 1024 * 2);
                if (a < lastLength) {
                    lastLength = a;
                }
                // log.debug("文件长度：" + (randomAccessFile.length()) + ",start:" + start + ",a:" + a + ",b:" + b + ",lastLength:" + lastLength);
                byte[] bytes = new byte[lastLength];
                if ((byteRead = randomAccessFile.read(bytes)) != -1
                        && (randomAccessFile.length() - start) > 0) {
                    syncMessage.setEndPos(byteRead);
                    syncMessage.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(syncMessage);
                    } catch (Exception e) {
                        log.error("写入文件异常", e);
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    log.info("文件已经读完channelRead()--------" + byteRead);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}

