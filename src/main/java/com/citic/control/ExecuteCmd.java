package com.citic.control;

import com.citic.ApplicationConf;
import com.citic.helper.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteCmd {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCmd.class);

    private static final String CANAL_HOME_DIR = "canal.home.dir";
    private static final String CANAL_START_CMD = "canal.start.cmd";
    private static final String CANAL_STOP_CMD = "canal.stop.cmd";

    private static final String TAGENT_HOME_DIR = "tagent.home.dir";
    private static final String TAGENT_START_CMD = "tagent.start.cmd";
    private static final String TAGENT_STOP_CMD = "tagent.stop.cmd";

    private ApplicationConf appConf;

    public ExecuteCmd() {
        appConf = ApplicationConf.getInstance();
    }

    private int exeCmd(String homeDir, String cmd) {
        int  exitCode = 0;
        ShellExecutor executor = new ShellExecutor(homeDir);
        try {
            exitCode = executor.executeCmd(cmd);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return exitCode;
    }

    /*
    * 启动 canal server
    * */
    public int startCanal() {
        return exeCmd(appConf.getConfig(CANAL_HOME_DIR),
                appConf.getConfig(CANAL_START_CMD));
    }

    /*
    * 停止 canal server
    * */
    public int stopCanal() {
        return exeCmd(appConf.getConfig(CANAL_HOME_DIR),
                appConf.getConfig(CANAL_STOP_CMD));
    }

    /*
    * 启动 TAgent
    * */
    public int startTAgent() {
        return exeCmd(appConf.getConfig(TAGENT_HOME_DIR),
                appConf.getConfig(TAGENT_START_CMD));
    }

    /*
    * 停止 TAgent
    * */
    public int stopTAgent() {
        return exeCmd(appConf.getConfig(TAGENT_HOME_DIR),
                appConf.getConfig(TAGENT_STOP_CMD));
    }

}
