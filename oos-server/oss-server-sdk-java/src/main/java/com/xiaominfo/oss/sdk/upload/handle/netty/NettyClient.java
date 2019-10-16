package com.xiaominfo.oss.sdk.upload.handle.netty;

import com.xiaominfo.oss.sdk.client.FileBytesResponse;
import com.xiaominfo.oss.sdk.client.NettyFileRequest;
import com.xiaominfo.oss.sdk.common.OSSClientMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class NettyClient {
    Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private OSSClientMessage<FileBytesResponse> response;
    private final String host;
    private final int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void connect(final NettyFileRequest syncMessage) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final CountDownLatch lathc = new CountDownLatch(1);
            NettyClientInitializer handler = new NettyClientInitializer(syncMessage, lathc);
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.handler(handler);
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
            lathc.await();

            NettyMessage message = handler.getMessage();
            logger.info("message ={}", message);

            OSSClientMessage<FileBytesResponse> clientMessage = new OSSClientMessage<>();
            FileBytesResponse response = new FileBytesResponse(message.getMaterialId(), message.getUrl(), message.getFilePath());
            response.setOriginalName(syncMessage.getOriginalName());
            response.setObjType(syncMessage.getMediaType());
            response.setByteLength(syncMessage.getFile().length());

            if ("0000".equalsIgnoreCase(message.getCode())) {
                clientMessage.setData(response);
            }
            clientMessage.setCode(message.getCode());
            clientMessage.setMessage(message.getMessage());
            this.response = clientMessage;
            logger.info("FileUploadClient connect()结束");
        } finally {
            group.shutdownGracefully();
        }
    }

    public OSSClientMessage<FileBytesResponse> getResponse() {
        return this.response;
    }

    public void setResponse(OSSClientMessage<FileBytesResponse> response) {
        this.response = response;
    }
}



