package com.citic;

import com.citic.control.GenerateConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ApplicationConf {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConf.class);
    private static ApplicationConf singleton = new ApplicationConf();
    private static Properties configProp =  new Properties();

    static {
        try {
            configProp.load(Main.class.getClassLoader().getResourceAsStream("conf/application.properties"));
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
