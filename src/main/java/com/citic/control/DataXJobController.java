package com.citic.control;

import static com.citic.AppConstants.DATAX_HOME_DIR;
import static com.citic.AppConstants.DATAX_JOB_DIR;
import static com.citic.AppConstants.DATAX_START_CMD;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.citic.AppConf;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.LogOutputStream;

/**
 * The type Data x job controller.
 */
public class DataXJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataXJobController.class);
    private static final Map<String, Future<ProcessResult>> runningJobs = Maps.newConcurrentMap();
    private static final Map<String, List<String>> jobErrors = Maps.newConcurrentMap();
    private static final Map<String, String> jobResponseUrl = Maps.newConcurrentMap();
    private static final int EXE_SUCCESS_CODE = 2;
    private static final int EXE_ERROR_CODE = 3;

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
    }

    /**
     * Add job response url.
     *
     * @param jobId the job id
     * @param responseUrl the response url
     */
    public static void addJobResponseUrl(String jobId, String responseUrl) {
        jobResponseUrl.put(jobId, responseUrl);
    }

    /**
     * Start job.
     *
     * @param jobId the job id
     * @throws IOException the io exception
     */
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

    /**
     * Stop job.
     *
     * @param jobId the job id
     */
    public static void stopJob(String jobId) {
        runningJobs.computeIfPresent(jobId, (k, v) -> {
            v.cancel(true);
            return v;
        });
    }

    /**
     * Check job state and send response.
     */
    static void checkJobStateAndSendResponse() {
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

            String jobResponse = output;
            if (jobErrors.containsKey(jobId)) {
                jobResponse += "\n" + String.join("\n", jobErrors.get(jobId));
            }
            sendJobResultToControlPlatform(jobId, jobResponse, jobResponseUrl.get(jobId));
        });

        jobDoneList.forEach(runningJobs::remove);
        jobDoneList.forEach(jobErrors::remove);
        jobDoneList.forEach(jobResponseUrl::remove);
        jobDoneList.clear();

        // 任务还没开始执行就失败的情况
        jobErrors.forEach((jobId, errorList) -> {
            if (!runningJobs.containsKey(jobId)) {
                jobDoneList.add(jobId);

                sendJobResultToControlPlatform(jobId, String.join("\n", errorList),
                    jobResponseUrl.get(jobId));
            }
        });

        jobDoneList.forEach(jobErrors::remove);
        jobDoneList.forEach(jobResponseUrl::remove);
    }

    private static Map<String, String> grabMessageFromOutput(String jobId, String jobOutput) {
        Preconditions.checkNotNull(jobOutput);
        String endTime = null;
        int inputNum = 0;
        int outputNum = 0;
        int execCode = EXE_SUCCESS_CODE;
        String execMessage = "success";

        int startIndex;
        if ((startIndex = jobOutput.indexOf("任务结束时刻")) != -1) {
            String validateMsg = jobOutput.substring(startIndex);
            String[] msgs = validateMsg.split("[\n\r]");
            for (String msg : msgs) {
                if (msg.contains("任务结束时刻")) {
                    endTime = msg.substring(msg.indexOf(":") + 1).trim();
                } else if (msg.contains("读出记录总数")) {
                    inputNum = Integer.parseInt(msg.substring(msg.indexOf(":") + 1).trim());
                } else if (msg.contains("读书失败总数")) {
                    outputNum =
                        inputNum - Integer.parseInt(msg.substring(msg.indexOf(":") + 1).trim());
                }
            }
        } else if ((startIndex = jobOutput.indexOf("该任务最可能的错误原因是:")) != -1) {
            execMessage = jobOutput.substring(startIndex);
            execCode = EXE_ERROR_CODE;
            endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Timestamp(System.currentTimeMillis()));
        } else {
            execMessage = jobOutput;
            execCode = EXE_ERROR_CODE;
            endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Timestamp(System.currentTimeMillis()));
        }

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("jobId", jobId);
        parameters.put("endTime", endTime);
        parameters.put("inputNum", String.valueOf(inputNum));
        parameters.put("outputNum", String.valueOf(outputNum));

        parameters.put("execCode", String.valueOf(execCode));
        parameters.put("execMessage", execMessage);

        return parameters;
    }

    private static void sendJobResultToControlPlatform(String jobId, String jobOutput,
        String jobResponseUrl) {
        Map<String, String> postData = grabMessageFromOutput(jobId, jobOutput);
        sendResponsePost(jobId, jobResponseUrl, postData);
    }


    private static void sendResponsePost(String jobId, String responseUrl,
        Map<String, String> postData) {
        LOGGER.debug("job: {}, responseUrl: {} post data: {}", jobId, responseUrl, postData);

        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(responseUrl));
        Preconditions.checkNotNull(postData);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(responseUrl);
        List<NameValuePair> urlParameters = Lists.newArrayList();
        postData.forEach((k, v) -> urlParameters.add(new BasicNameValuePair(k, v)));

        post.setEntity(new UrlEncodedFormEntity(urlParameters, UTF_8));

        HttpResponse response;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            LOGGER.warn("job: {} send response to {} not success, statusCode: {}",
                jobId, responseUrl, statusCode);
        }
    }


    private static void appendJobError(String jobId, String errorInfo) {
        jobErrors.computeIfAbsent(jobId, key -> new ArrayList<>()).add("\n" + errorInfo);
    }

}
