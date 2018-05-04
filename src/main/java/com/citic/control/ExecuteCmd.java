package com.citic.control;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.citic.AppConstants.*;
import static com.citic.helper.Utility.exeCmd;

public enum ExecuteCmd {
    INSTANCE;

    static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCmd.class);
    final AtomicBoolean canalState = new AtomicBoolean(STATE_DEAD);
    final AtomicBoolean tAgentState = new AtomicBoolean(STATE_DEAD);

    void setCanalState(boolean state) {
        synchronized (canalState) {
            this.canalState.set(state);
        }
    }

    void setTAgentState(boolean state) {
        synchronized (tAgentState) {
            this.tAgentState.set(state);
        }
    }

    /*
    * 启动 canal server
    * */
    public int startCanal() {
        int exitCode = 0;
        synchronized (canalState) {
            if (canalState.get() == STATE_ALIVE)
                return exitCode;

            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                    AppConf.getConfig(CANAL_START_CMD));
        }
        return exitCode;
    }

    /*
    * 停止 canal server
    * */
    public int stopCanal() {
        int exitCode;
        synchronized (canalState) {
            // canal 调用自己的脚本，可重复 stop
            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                    AppConf.getConfig(CANAL_STOP_CMD));

            if (exitCode == 0) {
                canalState.set(STATE_DEAD);
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
            if (tAgentState.get() == STATE_ALIVE)
                return exitCode;

            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                    AppConf.getConfig(TAGENT_START_CMD));
        }
        return exitCode;
    }

    /*
    * 停止 TAgent
    * */
    public int stopTAgent() {
        int exitCode = 0;
        synchronized (tAgentState) {
            if (tAgentState.get() == STATE_DEAD)
                return exitCode;

            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                    AppConf.getConfig(TAGENT_STOP_CMD));

            if (exitCode == 0) {
                tAgentState.set(STATE_DEAD);
            }
        }
        return exitCode;
    }

}
