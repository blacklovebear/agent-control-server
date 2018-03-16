package com.citic;

import com.citic.control.GenerateConf;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConf {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConf.class);
    private static ApplicationConf singleton = new ApplicationConf();
    private static Properties configProp =  new Properties();

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    static {
//        PropertyConfigurator.configure("config/log4j.properties");
        LOGGER.info("## load app configurations");
        String conf = System.getProperty("server.conf", "classpath:conf/application.properties");

        try {
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                configProp.load(Main.class.getClassLoader().getResourceAsStream(conf));
            } else {
                configProp.load(new FileInputStream(conf));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static ApplicationConf getInstance() {
        return singleton;
    }

    public String getConfig(String key) {
        return configProp.getProperty(key);
    }

    private ApplicationConf() { }
}
