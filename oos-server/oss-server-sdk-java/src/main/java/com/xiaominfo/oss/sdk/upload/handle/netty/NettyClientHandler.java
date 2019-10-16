package com.xiaominfo.oss.sdk.upload.handle.netty;

import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private CountDownLatch lathc;

    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private NettyFileRequest syncMessage;

    private NettyMessage message;
    private Logger log = LoggerFactory.getLogger(NettyClientHandler.class);

    public NettyClientHandler(NettyFileRequest ef, CountDownLatch lathc) {
        if (ef.getFile().exists()) {
            if (!ef.getFile().isFile()) {
                log.info("Not a file :{}", ef.getFile());
                return;
            }
        }
        this.syncMessage = ef;
        this.lathc = lathc;
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
            //log.info("channelActive()文件已经读完 " + byteRead);
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
                //log.info("长度：" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 1024 * 2);
                if (a < lastLength) {
                    lastLength = a;
                }
                //log.info("文件长度：" + (randomAccessFile.length()) + ",start:" + start + ",a:" + a + ",b:" + b + ",lastLength:" + lastLength);
                byte[] bytes = new byte[lastLength];
                // log.info("-----------------------------" + bytes.length);
                if ((byteRead = randomAccessFile.read(bytes)) != -1
                        && (randomAccessFile.length() - start) > 0) {
                    // log.info("byte 长度：" + bytes.length);
                    syncMessage.setEndPos(byteRead);
                    syncMessage.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(syncMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    log.info("文件已经读完channelRead()--------");
                    lathc.countDown();
                }
            }
        }


        if (msg instanceof NettyMessage) {
            NettyMessage message = (NettyMessage) msg;
            start = message.getStarPos();

            if (!"0000".equalsIgnoreCase(message.getCode())) {
                message.setMaterialId("");
                message.setUrl("");
                message.setFilePath("");
                ctx.close();
                log.info("上传文件出错:{}", message.getMessage());
                lathc.countDown();
                this.message = message;
            }

            if (start != -1) {
                randomAccessFile = new RandomAccessFile(
                        syncMessage.getFile(), "r");
                randomAccessFile.seek(start);
                //log.info("长度：" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 1024 * 2);
                if (a < lastLength) {
                    lastLength = a;
                }
                message.setLength(randomAccessFile.length());
                //log.info("文件长度：" + (randomAccessFile.length()) + ",start:" + start + ",a:" + a + ",b:" + b + ",lastLength:" + lastLength);
                byte[] bytes = new byte[lastLength];
                // log.info("-----------------------------" + bytes.length);
                if ((byteRead = randomAccessFile.read(bytes)) != -1
                        && (randomAccessFile.length() - start) > 0) {
                    // log.info("byte 长度：" + bytes.length);
                    syncMessage.setEndPos(byteRead);
                    syncMessage.setBytes(bytes);
                    syncMessage.setProjectPath(message.getFilePath());
                    syncMessage.setUuid(message.getMaterialId());
                    try {
                        ctx.writeAndFlush(syncMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    log.info("文件已经读完channelRead()--------" + byteRead);
                    lathc.countDown();
                    this.message = message;
                }
            } else {
                log.info("-11111111111111111111111111111111111111");
            }
        }


    }

    public void resetLatch(CountDownLatch initLathc) {
        this.lathc = initLathc;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public NettyMessage getMessage() {
        return message;
    }

    public void setMessage(NettyMessage message) {
        this.message = message;
    }
}

