package com.citic.control;

import static com.citic.AppConstants.DATAX_HOME_DIR;
import static com.citic.AppConstants.DATAX_JOB_DIR;
import static com.citic.AppConstants.DATAX_START_CMD;

import com.citic.AppConf;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.LogOutputStream;

public class DataXJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataXJobController.class);
    private static final Map<String, Future<ProcessResult>> runningJobs = Maps.newConcurrentMap();
    private static final Map<String, List<String>> jobErrors = Maps.newConcurrentMap();

    private static Future<ProcessResult> executeDataXJob(String homeDir, String cmd, String jobId)
        throws IOException {
        return new ProcessExecutor()
            .directory(new File(homeDir))
            .command("sh", "-c", cmd)
            .redirectError(new LogOutputStream() {
                @Override
                protected void processLine(String s) {
                    appendJobError(jobId, s);
                }
            })
            .readOutput(true)
            .start().getFuture();

        // String output = future.get(60, TimeUnit.SECONDS).outputUTF8();
    }

    public static void startJob(String jobId) throws IOException {
        runningJobs.computeIfAbsent(jobId, key -> {
            String homeDir = AppConf.getConfig(DATAX_HOME_DIR);

            String cmd = AppConf.getConfig(DATAX_START_CMD) + " " + AppConf.getConfig(DATAX_JOB_DIR)
                + File.separator + key + ".json";
            try {
                return executeDataXJob(homeDir, cmd, key);
            } catch (IOException e) {
                appendJobError(jobId, e.getMessage());
                return null;
            }
        });
    }

    public static void stopJob(String jobId) {
        runningJobs.computeIfPresent(jobId, (k, v) -> {
            v.cancel(true);
            return v;
        });
    }

    // TODO: 发送job执行结果给管控平台

    private static void appendJobError(String jobId, String errorInfo) {
        jobErrors.computeIfAbsent(jobId, key -> new ArrayList<>()).add("\n" + errorInfo);
    }

}
