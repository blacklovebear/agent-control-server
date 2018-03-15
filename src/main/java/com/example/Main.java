package com.example;

import com.example.service.ConfigurationService;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * Main class.
 *
 */
public class Main {
//    private static final URI BASE_URI = URI.create("http://localhost:8080/");
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ApplicationConf conf = ApplicationConf.getInstance();
        String baseUri = conf.getConfig("agent.base.uri");

        URI BASE_URI = URI.create(baseUri);
        try {
            ResourceConfig resourceConfig = new ResourceConfig(ConfigurationService.class);
            final Channel server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

            logger.info("Jersey App on Netty Server starting: {}", BASE_URI.toString());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.close();
                }
            }));

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }
}

