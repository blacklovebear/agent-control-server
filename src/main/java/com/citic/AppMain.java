package com.citic;

import com.citic.service.ConfigurationService;
import com.citic.service.ExecutorService;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


/**
 * AppMain class.
 */
public class AppMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

    public static void main(String[] args) {
        AppConf conf = AppConf.getInstance();
        String baseUri = conf.getConfig(AppConstants.AGENT_BASE_URI);

        URI BASE_URI = URI.create(baseUri);

        ResourceConfig resourceConfig = new ResourceConfig(
                ConfigqurationService.class,
                ExecutorService.class
                );
        final Channel server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

        LOGGER.info("Jersey App on Netty Server starting: {}", BASE_URI.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                server.close();
            }
        }));

        // 启动对进程的监控
//        ProcessMonitor monitor = new ProcessMonitor();
//        monitor.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

