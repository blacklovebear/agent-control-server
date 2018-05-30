package com.citic.control;

import static com.citic.AppConstants.DATAX_HOME_DIR;
import static com.citic.AppConstants.DATAX_JOB_DIR;
import static com.citic.AppConstants.DATAX_START_CMD;

import com.citic.AppConf;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
    private static final Map<String, String> jobResponseUrl = Maps.newConcurrentMap();

    private static Future<ProcessResult> executeDataXJob(String homeDir, String cmd, String jobId)
        throws IOException {
        return new ProcessExecutor()
            .directory(new File(homeDir))
            .command("sh", "-c", cmd)
            .destroyOnExit()
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

    public static void addJobResponseUrl(String jobId, String responseUrl) {
        jobResponseUrl.put(jobId, responseUrl);
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

    public static void checkJobStateAndSendResponse() {
        List<String> jobDoneList = Lists.newArrayList();

        runningJobs.forEach((jobId, jobFuture) -> {
            String output;
            if (!jobFuture.isDone()) {
                return;
            }
            jobDoneList.add(jobId);

            if (jobFuture.isCancelled()) {
                output = String.format("Job: %s is cancelled.", jobId);
            } else {
                try {
                    output = jobFuture.get().outputUTF8();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(e.getMessage(), e);
                    output = String.format("Job: %s run with exception: %s", jobId, e.getMessage());
                }
            }

            // TODO: 发送job执行结果给管控平台
            LOGGER.debug("Job: {}, output: {}", jobId, output);
            if (jobErrors.containsKey(jobId)) {
                LOGGER.debug("Job: {}, run error: {}", jobId, jobErrors.get(jobId).toString());
            }
        });

        jobDoneList.forEach(runningJobs::remove);
        jobDoneList.forEach(jobErrors::remove);
        jobDoneList.clear();

        // 任务还没开始执行就失败的情况
        jobErrors.forEach((jobId, errorList) -> {
            if (!runningJobs.containsKey(jobId)) {
                // TODO: 发送job执行结果给管控平台
                jobDoneList.add(jobId);
                LOGGER.debug("Job: {}, start error: {}", jobId, errorList.toString());
            }
        });

        jobDoneList.forEach(jobErrors::remove);
    }



    private static void appendJobError(String jobId, String errorInfo) {
        jobErrors.computeIfAbsent(jobId, key -> new ArrayList<>()).add("\n" + errorInfo);
    }

}
