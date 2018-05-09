package com.citic.helper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The type Log file tailer.
 */
public class LogFileTailer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileTailer.class);
    private final Path filePath;
    private final BiConsumer<String, String> logHandler;
    private int tailRunEveryNSeconds = 2000;
    private long lastKnownPosition = 0;
    private boolean shouldIRun = true;
    private File tailFile = null;

    /**
     * Instantiates a new Log file tailer.
     *
     * @param filePath the file path
     * @param myInterval the my interval
     * @param logHandler the log handler
     */
    public LogFileTailer(String filePath, int myInterval, BiConsumer<String, String> logHandler) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(filePath),
            "filePath is null or empty");
        this.filePath = Paths.get(filePath);
        tailFile = new File(filePath);
        this.tailRunEveryNSeconds = myInterval;
        this.logHandler = logHandler;
    }


    /**
     * Instantiates a new Log file tailer.
     *
     * @param filePath the file path
     * @param myInterval the my interval
     * @param logHandler the log handler
     */
    public LogFileTailer(File filePath, int myInterval, BiConsumer<String, String> logHandler) {
        Preconditions.checkNotNull(filePath, "filePath is null");

        this.filePath = filePath.toPath();
        tailFile = filePath;
        this.tailRunEveryNSeconds = myInterval;
        this.logHandler = logHandler;
    }

    /**
     * The entry point of application.
     *
     * @param argv the input arguments
     */
    public static void main(String[] argv) {

        ExecutorService tailExecutor = Executors.newFixedThreadPool(4);

        // Replace username with your real value
        // For windows provide different path like: c:\\temp\\tail.log
        String filePath = "logs/test.log";
        LogFileTailer logFileTailer = new LogFileTailer(filePath, 2000, (message, logPath) -> {
            if (message.contains("ERROR")) {
                System.out.println(message);
            }
        });

        // Start running log file tailer on tail.log file
        tailExecutor.execute(logFileTailer);

    }

    private void printLine(String message) {
        logHandler.accept(message, this.filePath.toString());
    }

    /**
     * Stop running.
     */
    public void stopRunning() {
        shouldIRun = false;
    }

    /**
     * run.
     */
    public void run() {
        LOGGER.debug("tail -f {}", this.filePath.toString());
        // 确保log文件存在
        Utility.createParentDirs(this.filePath.toString());
        if (!Files.exists(this.filePath)) {
            String cmd = "touch " + this.filePath.getFileName();
            Path parent = this.filePath.getParent();
            if (parent != null) {
                Utility.exeCmd(parent.toString(), cmd);
            }
        }
        // 已有的文件内容不做解析
        lastKnownPosition = tailFile.length();
        try {
            while (shouldIRun && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(tailRunEveryNSeconds);
                long fileLength = tailFile.length();
                if (fileLength > lastKnownPosition) {
                    // Reading and writing file
                    RandomAccessFile readWriteFileAccess = new RandomAccessFile(tailFile, "r");
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
                    RandomAccessFile readWriteFileAccess = new RandomAccessFile(tailFile, "r");
                    readWriteFileAccess.seek(lastKnownPosition);
                    String tailLine = null;
                    while ((tailLine = readWriteFileAccess.readLine()) != null) {
                        this.printLine(tailLine);
                    }
                    lastKnownPosition = readWriteFileAccess.getFilePointer();
                    readWriteFileAccess.close();
                }
            }
        } catch (InterruptedException e) {
            stopRunning();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            stopRunning();
        }
    }
}
