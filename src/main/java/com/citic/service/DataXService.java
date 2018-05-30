package com.citic.service;

import com.citic.control.DataXJobController;
import com.citic.control.GenerateConfController;
import com.citic.entity.DataXJobConfig;
import com.citic.entity.ResponseResult;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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

    @GET
    @Path("start_test")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startJob(@QueryParam("jobId") String jobId) throws IOException {
        DataXJobController.startJob(jobId);
        return new ResponseResult();
    }

    @GET
    @Path("stop")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopJob(@QueryParam("jobId") String jobId) {
        DataXJobController.stopJob(jobId);
        return new ResponseResult();
    }
}
