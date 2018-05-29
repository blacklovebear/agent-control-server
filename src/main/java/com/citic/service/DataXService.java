package com.citic.service;

import com.citic.control.GenerateConf;
import com.citic.entity.DataXJobConfig;
import com.citic.entity.ResponseResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult newConfig(DataXJobConfig dataXJobConfig) throws Exception {
        LOGGER.debug("DataXJobConfig: {}", dataXJobConfig.toString());

        dataXJobConfig.checkProperties();

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateDataX(dataXJobConfig);

        return new ResponseResult();
    }
}
