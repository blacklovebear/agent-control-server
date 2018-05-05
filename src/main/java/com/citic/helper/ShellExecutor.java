package com.citic.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShellExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellExecutor.class);
    private String homeDirectory = System.getProperty("user.home");

    public ShellExecutor() {
    }

    public ShellExecutor(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    /*
     * 在机器执行命令，并用 LOGGER 记录命令执行结果信息
     * */
    public int executeCmd(String cmd) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        LOGGER.info("home dir: {}, cmd: {}", this.homeDirectory, cmd);

        if (SystemUtils.IS_OS_WINDOWS) {
            builder.command("cmd.exe", "/c", cmd);
        } else {
            builder.command("sh", "-c", cmd);
        }
        builder.directory(new File(homeDirectory));
        Process process = builder.start();

        StreamGobbler output = new StreamGobbler(process.getInputStream(), LOGGER::debug);
        StreamGobbler error = new StreamGobbler(process.getErrorStream(), LOGGER::error);

        Executors.newSingleThreadExecutor().submit(output);
        Executors.newSingleThreadExecutor().submit(error);

        return process.waitFor();
    }

    /*
     * 监控进程状态
     * */
    public String monitorProcess(String cmd, String processName) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        if (SystemUtils.IS_OS_LINUX) {
            builder.command("sh", "-c", cmd);
        } else {
            builder.command("cmd.exe", "/c", cmd);
        }
        Process process = builder.start();
        ResponseData responseData = new ResponseData(processName);

        StreamGobbler output = new StreamGobbler(process.getInputStream(), responseData::putData);
        StreamGobbler error = new StreamGobbler(process.getErrorStream(), LOGGER::error);

        Executors.newSingleThreadExecutor().submit(output);
        Executors.newSingleThreadExecutor().submit(error);

        process.waitFor();
        return responseData.getProcessState();
    }

    private static class StreamGobbler implements Runnable {

        private InputStream inputStream;
        private Consumer<String> consumer;

        StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
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