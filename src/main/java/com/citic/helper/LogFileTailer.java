package com.citic.helper;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;


public class LogFileTailer implements Runnable {
    private int tailRunEveryNSeconds = 2000;
    private long lastKnownPosition = 0;
    private boolean shouldIRun = true;
    private File tailFile = null;
    private final Path filePath;


    private final Consumer<String> logHandler;


    public LogFileTailer(String filePath, int myInterval, Consumer<String> logHandler) {
        this.filePath = Paths.get(filePath);
        tailFile = new File(filePath);
        this.tailRunEveryNSeconds = myInterval;
        this.logHandler = logHandler;
    }

    private void printLine(String message) {
        logHandler.accept(message);
    }

    public void stopRunning() {
        shouldIRun = false;
    }

    public void run() {
        // 确保log文件存在
        Utility.createParentDirs(this.filePath.toString());
        if (!Files.exists(this.filePath)) {
            String cmd = "touch " + this.filePath.getFileName();
            Utility.exeCmd(this.filePath.getParent().toString(), cmd);
        }

        try {
            while (shouldIRun) {
                Thread.sleep(tailRunEveryNSeconds);
                long fileLength = tailFile.length();
                if (fileLength > lastKnownPosition) {

                    // Reading and writing file
                    RandomAccessFile readWriteFileAccess = new RandomAccessFile(tailFile, "rw");
                    readWriteFileAccess.seek(lastKnownPosition);
                    String tailLine = null;
                    while ((tailLine = readWriteFileAccess.readLine()) != null) {
                        this.printLine(tailLine);
                    }
                    lastKnownPosition = readWriteFileAccess.getFilePointer();
                    readWriteFileAccess.close();
                } else if (fileLength < lastKnownPosition) {
                    // rotate file
                    lastKnownPosition = 0;
                    // Reading and writing file
                    RandomAccessFile readWriteFileAccess = new RandomAccessFile(tailFile, "rw");
                    readWriteFileAccess.seek(lastKnownPosition);
                    String tailLine = null;
                    while ((tailLine = readWriteFileAccess.readLine()) != null) {
                        this.printLine(tailLine);
                    }
                    lastKnownPosition = readWriteFileAccess.getFilePointer();
                    readWriteFileAccess.close();
                }
            }
        } catch (Exception e) {
            stopRunning();
        }
    }

    public static void main(String argv[]) {

        ExecutorService tailExecutor = Executors.newFixedThreadPool(4);

        // Replace username with your real value
        // For windows provide different path like: c:\\temp\\tail.log
        String filePath = "/Users/macbook/git_repo/agent-control-server/logs/test.log";
        LogFileTailer tail_tailF = new LogFileTailer(filePath, 2000, message -> {
            if (message.contains("ERROR"))
                System.out.println(message);
        });

        // Start running log file tailer on tail.log file
        tailExecutor.execute(tail_tailF);

    }
}
