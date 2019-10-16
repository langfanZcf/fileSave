package com.xiaominfo.oss;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.xiaominfo.oss.common.SpringContextUtil;
import com.xiaominfo.oss.sync.Sync;
import com.xiaominfo.oss.sync.netty.MasterServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.xiaominfo.oss"})
public class OssServerApplication {

    private final static Log log = LogFactory.get();


    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(OssServerApplication.class, args);

        //Sync.startMaster();
        Environment env = application.getEnvironment();
        log.info("\n--------------------------------------------------------------------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n--------------------------------------------------------------------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));
        startMaster();
    }

    public static void startMaster() {
        ((Runnable) () -> {
            try {
                String i = SpringContextUtil.getProperty("material.masterPort");
                int masterPort = Integer.parseInt(i);
                new MasterServer().bind(masterPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).run();

    }

}
