package com.xiaominfo.oss.sync.netty;

import com.xiaominfo.oss.sync.SyncMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SalveServer {
    public static final int FILE_PORT = 18001;
    private static Logger logger = LoggerFactory.getLogger(SalveServer.class);

    public void connect(int port, String host,
                        final SyncMessage syncMessage) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            ch.pipeline().addLast(new SalveServerHandler(syncMessage));
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
            logger.info("file = [{}] 同步完成", syncMessage.getFile().getName());
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = FILE_PORT;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            SyncMessage uploadFile = new SyncMessage();
            File file = new File("D:\\迅雷下载\\idman630build7.exe");// d:/source.rar,D:/2014work/apache-maven-3.5.0-bin.tar.gz
            String fileMd5 = file.getName();// 文件名
            uploadFile.setFile(file);
            uploadFile.setFile_md5(fileMd5);
            uploadFile.setFilePath("\\sync\\123\\");
            uploadFile.setStarPos(0);// 文件开始位置
            new SalveServer().connect(port, "127.0.0.1", uploadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}



