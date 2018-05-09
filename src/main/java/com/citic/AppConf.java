package com.citic;

import static com.citic.AppConstants.CLASSPATH_URL_PREFIX;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConf.class);
    private static Properties configProp = new Properties();

    static {
        // PropertyConfigurator.configure("config/log4j.properties");
        LOGGER.info("## load app configurations");
        String conf = System.getProperty("server.conf", "classpath:conf/application.properties");
        InputStream confInput = null;
        try {
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                confInput = AppMain.class.getClassLoader().getResourceAsStream(conf);
                configProp.load(confInput);
            } else {
                confInput = new FileInputStream(conf);
                configProp.load(confInput);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (confInput != null) {
                try {
                    confInput.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public static String getConfig(String key) {
        return configProp.getProperty(key);
    }
}
