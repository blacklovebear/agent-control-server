package com.citic;

import static com.citic.AppConstants.CANAL_PSWD_ENCRYPT;
import static com.citic.AppConstants.CLASSPATH_URL_PREFIX;
import static com.citic.AppConstants.DEFAULT_CANAL_PASSWD_ENCRYPT;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type App conf.
 */
public class AppConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConf.class);
    private static Properties configProp = new Properties();

    static {
        LOGGER.info("## load app configurations");
        String conf = System.getProperty("server.conf", "classpath:conf/application.properties");
        if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
            conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
            try (InputStream confInput = AppMain.class.getClassLoader().getResourceAsStream(conf)) {
                configProp.load(confInput);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        } else {
            try (InputStream confInput = new FileInputStream(conf)) {
                configProp.load(confInput);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private AppConf() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets config.
     *
     * @param key the key
     * @return the config
     */
    public static String getConfig(String key) {
        return configProp.getProperty(key);
    }

    /**
     * Is canal password encrypt boolean.
     *
     * @return the boolean
     */
    public static boolean isCanalPasswordEncrypt() {
        boolean isEncrypt = DEFAULT_CANAL_PASSWD_ENCRYPT;
        String test = getConfig(CANAL_PSWD_ENCRYPT);
        if (test != null) {
            isEncrypt = BooleanUtils.toBoolean(test);
        }
        return isEncrypt;
    }
}
