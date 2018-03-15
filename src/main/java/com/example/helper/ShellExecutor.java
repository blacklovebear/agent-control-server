package com.example.helper;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ShellExecutor {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

    private Consumer<String> consumer = System.out::println;

    private String homeDirectory = System.getProperty("user.home");

    public ShellExecutor(Consumer<String> consumer, String homeDirectory) {
        this.consumer = consumer;
        this.homeDirectory = homeDirectory;
    }

    public void executeCmd(String... cmd) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        if (IS_WINDOWS) {
            // "cmd.exe", "/c", "dir"
            builder.command(cmd);
        } else {
            // "sh", "-c", "ls"
            builder.command(cmd);
        }
        builder.directory(new File(homeDirectory));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();

        assert exitCode == 0;
    }

    public static void main(String[] args) {
        ShellExecutor executor = new ShellExecutor(System.out::println, "D:\\Java\\jdk1.8.0_121");

        try {
            executor.executeCmd("cmd.exe", "/c", "dir");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}