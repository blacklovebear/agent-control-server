package com.citic.service;

import com.citic.control.DataXJobController;
import com.citic.control.GenerateConfController;
import com.citic.entity.DataXJobConfig;
import com.citic.entity.ResponseResult;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Data x service.
 */
@Path("datax")
public class DataXService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataXService.class);

    /**
     * New config response result.
     *
     * @param dataXJobConfig the data x job config
     * @return the response result
     * @throws Exception the exception
     */
    @POST
    @Path("start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult newConfig(DataXJobConfig dataXJobConfig) throws Exception {
        LOGGER.debug("DataXJobConfig: {}", dataXJobConfig.toString());

        dataXJobConfig.checkProperties();
        GenerateConfController generateConf = new GenerateConfController();
        generateConf.generateDataX(dataXJobConfig);

        DataXJobController.startJob(dataXJobConfig.getJobId());
        DataXJobController
            .addJobResponseUrl(dataXJobConfig.getJobId(), dataXJobConfig.getResponseUrl());
        return new ResponseResult();
    }

    /**
     * Start job response result.
     *
     * @param jobId the job id
     * @return the response result
     * @throws IOException the io exception
     */
    @GET
    @Path("start_test")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startJob(@QueryParam("jobId") String jobId) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobId), "jobId param is null or empty ");
        DataXJobController.startJob(jobId);
        String testResponseUrl = "http://localhost:8080/datax/response_test";
        DataXJobController
            .addJobResponseUrl(jobId, testResponseUrl);

        return new ResponseResult();
    }


    @POST
    @Path("response_test")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult responseTest(final MultivaluedMap<String, String> formParams) {
        LOGGER.debug("formParams: {}", formParams.toString());
        return new ResponseResult();
    }


    /**
     * Stop job response result.
     *
     * @param jobId the job id
     * @return the response result
     */
    @GET
    @Path("stop")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopJob(@NotNull @QueryParam("jobId") String jobId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobId), "jobId param is null or empty ");
        DataXJobController.stopJob(jobId);
        return new ResponseResult();
    }
}
