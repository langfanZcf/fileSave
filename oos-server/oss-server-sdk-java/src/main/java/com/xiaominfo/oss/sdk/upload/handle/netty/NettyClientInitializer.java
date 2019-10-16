package com.xiaominfo.oss.sdk.upload.handle.netty;

import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.CountDownLatch;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    private CountDownLatch lathc;
    private NettyClientHandler handler;
    private NettyFileRequest syncMessage;

    public NettyClientInitializer(NettyFileRequest syncMessage, CountDownLatch lathc) {
        this.syncMessage = syncMessage;
        this.lathc = lathc;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        handler = new NettyClientHandler(syncMessage, lathc);

        ch.pipeline().addLast(new ObjectEncoder());
        ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
        //ch.pipeline().addLast(new NettyClientHandler(syncMessage, lathc));
        ch.pipeline().addLast(handler);
    }

    public NettyMessage getMessage() {
        return handler.getMessage();
    }

    //重置同步锁
    public void resetLathc(CountDownLatch initLathc) {
        handler.resetLatch(initLathc);
    }

}
