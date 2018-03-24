package com.citic.control;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.citic.AppConstants.*;

public class ExecuteCmd {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCmd.class);
    private static final ExecuteCmd single = new ExecuteCmd();

    private Boolean canalState = STATE_DEAD;
    private Boolean tAgentState = STATE_DEAD;

    private ExecuteCmd() {}

    public static ExecuteCmd getInstance() {
        return single;
    }

    public boolean getCanalState() {
        synchronized (canalState) {
            return canalState;
        }
    }

    public void setCanalState(boolean state) {
        synchronized (canalState) {
            this.canalState = state;
        }
    }

    public boolean getTAgentState() {
        synchronized (tAgentState) {
            return tAgentState;
        }
    }

    public void setTAgentState(boolean state) {
        synchronized (tAgentState) {
            this.tAgentState = state;
        }
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
        int exitCode = 0;
        synchronized (canalState) {
            if (canalState == STATE_ALIVE)
                return exitCode;

            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                    AppConf.getConfig(CANAL_START_CMD));
            if (exitCode == 0) {
                canalState = STATE_ALIVE;
            }
        }
        return exitCode;
    }

    /*
    * 停止 canal server
    * */
    public int stopCanal() {
        int exitCode = 0;
        synchronized (canalState) {
            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                    AppConf.getConfig(CANAL_STOP_CMD));
            if (exitCode == 0) {
                canalState = STATE_DEAD;
            }
        }
        return exitCode;
    }

    /*
    * 启动 TAgent
    * */
    public int startTAgent() {
        int exitCode = 0;
        synchronized (tAgentState) {
            if (tAgentState == STATE_ALIVE)
                return exitCode;

            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                    AppConf.getConfig(TAGENT_START_CMD));
            if (exitCode == 0) {
                tAgentState = STATE_ALIVE;
            }
        }
        return exitCode;
    }

    /*
    * 停止 TAgent
    * */
    public int stopTAgent() {
        int exitCode = 0;
        synchronized (tAgentState) {
            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                    AppConf.getConfig(TAGENT_STOP_CMD));
            if (exitCode == 0) {
                tAgentState = STATE_DEAD;
            }
        }
        return exitCode;
    }

}
