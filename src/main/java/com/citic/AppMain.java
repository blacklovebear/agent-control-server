package com.citic;

import static com.citic.AppConstants.KAFKA_USE_AVRO;

import com.citic.control.DataXJobMonitor;
import com.citic.control.ErrorLogMonitor;
import com.citic.control.ProcessMonitor;
import com.citic.entity.MyExceptionMapper;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.service.ConfigurationService;
import com.citic.service.DataXService;
import com.citic.service.ExeService;
import io.netty.channel.Channel;
import java.net.URI;
import org.apache.commons.lang.BooleanUtils;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AppMain class.
 */
public class AppMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);
    private static Channel server;
    private static SimpleKafkaProducer<Object, Object> producer;
    private static ProcessMonitor processMonitor;
    private static ErrorLogMonitor errorLogMonitor;
    private static DataXJobMonitor dataXJobMonitor;


    /**
     * Gets error log monitor.
     *
     * @return the error log monitor
     */
    public static ErrorLogMonitor getErrorLogMonitor() {
        return errorLogMonitor;
    }

    /**
     * Start.
     */
    public static void start() {
        String baseUri = AppConf.getConfig(AppConstants.AGENT_BASE_URI);
        boolean useAvro = BooleanUtils.toBoolean(AppConf.getConfig(KAFKA_USE_AVRO));

        URI uri = URI.create(baseUri);

        ResourceConfig resourceConfig = new ResourceConfig(
            ConfigurationService.class,
            ExeService.class,
            MyExceptionMapper.class,
            DataXService.class
        );

        server = NettyHttpContainerProvider.createHttp2Server(uri, resourceConfig, null);

        LOGGER.info("Jersey App on Netty Server starting: {}", uri);

        producer = new SimpleKafkaProducer<>(false, useAvro);
        // 启动对进程的监控
        processMonitor = new ProcessMonitor(producer, useAvro);
        processMonitor.start();

        errorLogMonitor = new ErrorLogMonitor(producer, useAvro);
        errorLogMonitor.start();

        dataXJobMonitor = new DataXJobMonitor();
        dataXJobMonitor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(AppMain::stop));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stop.
     */
    public static void stop() {
        processMonitor.stop();
        errorLogMonitor.stop();
        dataXJobMonitor.stop();
        producer.close();
        server.close();
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        start();
    }
}

