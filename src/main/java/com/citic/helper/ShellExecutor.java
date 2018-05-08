package com.citic.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;


/**
 * The type Shell executor.
 */
public class ShellExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellExecutor.class);
    private String homeDirectory = System.getProperty("user.home");

    /**
     * Instantiates a new Shell executor.
     */
    public ShellExecutor() {
    }

    /**
     * Instantiates a new Shell executor.
     *
     * @param homeDirectory the home directory
     */
    public ShellExecutor(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    /**
     * 在机器执行命令，并用 LOGGER 记录命令执行结果信息.
     *
     * @param cmd the cmd
     * @return the int
     * @throws Exception the exception
     */
    public int executeCmd(String cmd) throws Exception {
        ProcessExecutor processExecutor = new ProcessExecutor();

        if (SystemUtils.IS_OS_WINDOWS) {
            processExecutor.command("cmd.exe", "/c", cmd);
        } else {
            processExecutor.command("sh", "-c", cmd);
        }

        return processExecutor.directory(new File(homeDirectory))
            .redirectError(Slf4jStream.of(getClass()).asError())
            .redirectOutput(Slf4jStream.of(getClass()).asInfo())
            .execute()
            .getExitValue();
    }

    /**
     * 监控进程状态.
     *
     * @param cmd the cmd
     * @param processName the process name
     * @return the string
     * @throws Exception the exception
     */
    public String monitorProcess(String cmd, String processName) throws Exception {
        ProcessExecutor processExecutor = new ProcessExecutor();
        ResponseData responseData = new ResponseData(processName);

        if (SystemUtils.IS_OS_WINDOWS) {
            processExecutor.command("cmd.exe", "/c", cmd);
        } else {
            processExecutor.command("sh", "-c", cmd);
        }

        processExecutor.directory(new File(homeDirectory))
            .redirectError(Slf4jStream.of(getClass()).asError())
            .redirectOutput(new LogOutputStream() {
                @Override
                protected void processLine(String line) {
                    responseData.putData(line);
                }
            })
            .execute();
        String output = responseData.getProcessState();
        LOGGER.info("monitor process output:{}", output);
        return output;
    }

    /*
     * 获取命令执行结果后的数据
     * */
    private static class ResponseData {

        private List<String> data = new ArrayList<>();
        private String processName;

        private ResponseData(String processName) {
            this.processName = processName;
        }

        private void putData(String line) {
            data.add(line);
        }

        private String getProcessState() {
            String state;
            if (data.isEmpty()) {
                state = String.format("%s dead", processName);
            } else if (data.size() == 1) {
                state = String.format("%s running pid:%s", processName, data.toString());
            } else {
                state = String.format("%s unNormal pid:%s", processName, data.toString());
            }
            return state;
        }
    }

}