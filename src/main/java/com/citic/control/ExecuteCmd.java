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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCmd.class);
    /**
     * The Canal state.
     */
    private final AtomicBoolean canalState = new AtomicBoolean(STATE_DEAD);
    /**
     * The T agent state.
     */
    private final AtomicBoolean tagentState = new AtomicBoolean(STATE_DEAD);

    private final Lock canalLock = new ReentrantLock();
    private final Lock tagentLock = new ReentrantLock();

    /**
     * Sets canal state.
     *
     * @param state the state
     */
    void setCanalState(boolean state) {
        canalLock.lock();
        try {
            this.canalState.set(state);
        } finally {
            canalLock.unlock();
        }
    }

    /**
     * Sets t agent state.
     *
     * @param state the state
     */
    void setTAgentState(boolean state) {
        tagentLock.lock();
        try {
            this.tagentState.set(state);
        } finally {
            tagentLock.unlock();
        }
    }

    /**
     * Start canal int.
     *
     * @return the int
     */
    public int startCanal() {
        int exitCode = 0;
        canalLock.lock();
        try {
            if (canalState.get() == STATE_ALIVE) {
                return exitCode;
            }

            exeCmd(AppConf.getConfig(CANAL_HOME_DIR), AppConf.getConfig(CANAL_STOP_CMD));
            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                AppConf.getConfig(CANAL_START_CMD));
        } finally {
            canalLock.unlock();
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
        canalLock.lock();
        try {
            // canal 调用自己的脚本，可重复 stop
            exitCode = exeCmd(AppConf.getConfig(CANAL_HOME_DIR),
                AppConf.getConfig(CANAL_STOP_CMD));

            if (exitCode == 0) {
                canalState.set(STATE_DEAD);
            }
        } finally {
            canalLock.unlock();
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
        tagentLock.lock();
        try {
            if (tagentState.get() == STATE_ALIVE) {
                return exitCode;
            }

            exeCmd(AppConf.getConfig(TAGENT_HOME_DIR), AppConf.getConfig(TAGENT_STOP_CMD));
            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                AppConf.getConfig(TAGENT_START_CMD), true);
        } finally {
            tagentLock.unlock();
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
        tagentLock.lock();
        try {
            if (tagentState.get() == STATE_DEAD) {
                return exitCode;
            }

            exitCode = exeCmd(AppConf.getConfig(TAGENT_HOME_DIR),
                AppConf.getConfig(TAGENT_STOP_CMD));

            if (exitCode == 0) {
                tagentState.set(STATE_DEAD);
            }
        } finally {
            tagentLock.unlock();
        }
        return exitCode;
    }
}
