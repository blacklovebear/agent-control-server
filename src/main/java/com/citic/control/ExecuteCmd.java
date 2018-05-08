package com.citic.control;

import static com.citic.AppConstants.CANAL_HOME_DIR;
import static com.citic.AppConstants.CANAL_START_CMD;
import static com.citic.AppConstants.CANAL_STOP_CMD;
import static com.citic.AppConstants.STATE_ALIVE;
import static com.citic.AppConstants.STATE_DEAD;
import static com.citic.AppConstants.TAGENT_HOME_DIR;
import static com.citic.AppConstants.TAGENT_START_CMD;
import static com.citic.AppConstants.TAGENT_STOP_CMD;
import static com.citic.helper.Utility.exeCmd;

import com.citic.AppConf;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The enum Execute cmd.
 */
public enum ExecuteCmd {
    /**
     * Instance execute cmd.
     */
    INSTANCE;

    /**
     * The Logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCmd.class);
    /**
     * The Canal state.
     */
    final AtomicBoolean canalState = new AtomicBoolean(STATE_DEAD);
    /**
     * The T agent state.
     */
    final AtomicBoolean tagentState = new AtomicBoolean(STATE_DEAD);

    /**
     * Sets canal state.
     *
     * @param state the state
     */
    void setCanalState(boolean state) {
        synchronized (canalState) {
            this.canalState.set(state);
        }
    }

    /**
     * Sets t agent state.
     *
     * @param state the state
     */
    void setTAgentState(boolean state) {
        synchronized (tagentState) {
            this.tagentState.set(state);
        }
    }

    /**
     * Start canal int.
     *
     * @return the int
     */
    public int startCanal() {
        int exitCode = 0;
        synchronized (canalState) {
            if (canalState.get() == STATE_ALIVE) {
                return exitCode;
            }

            exeCmd(AppConf.getConfig(CANAL_HOME_DIR), AppConf.getConfig(CANAL_STOP_CMD));
            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                AppConf.getConfig(CANAL_START_CMD));
        }
        return exitCode;
    }

    /**
     * Stop canal int.
     *
     * @return the int
     */
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

    /**
     * Start t agent int.
     *
     * @return the int
     */
    public int startTAgent() {
        int exitCode = 0;
        synchronized (tagentState) {
            if (tagentState.get() == STATE_ALIVE) {
                return exitCode;
            }

            exeCmd(AppConf.getConfig(TAGENT_HOME_DIR), AppConf.getConfig(TAGENT_STOP_CMD));
            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                AppConf.getConfig(TAGENT_START_CMD), true);
        }
        return exitCode;
    }

    /**
     * Stop t agent int.
     *
     * @return the int
     */
    public int stopTAgent() {
        int exitCode = 0;
        synchronized (tagentState) {
            if (tagentState.get() == STATE_DEAD) {
                return exitCode;
            }

            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                AppConf.getConfig(TAGENT_STOP_CMD));

            if (exitCode == 0) {
                tagentState.set(STATE_DEAD);
            }
        }
        return exitCode;
    }
}
