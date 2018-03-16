package com.citic.service;

import com.citic.*;
import com.citic.entity.ResponseResult;
import com.citic.control.GenerateConf;
import com.citic.entity.TAgent;
import com.citic.entity.CanalInstance;
import com.citic.entity.CanalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "configuration" path)
 */
@Path("config")
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @POST
    @Path("canal/server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configCanalServer(CanalServer config) {
        LOGGER.debug("CanalServer: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanalServer(config);
        return new ResponseResult("POST", "success");
    }

    @POST
    @Path("canal/instance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configCanalInstance(CanalInstance config) {
        LOGGER.debug("CanalInstance: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanalInstance(config);
        return new ResponseResult("POST", "success");
    }

    @POST
    @Path("tagent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configAgent(TAgent config) {
        LOGGER.debug("TAgent: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateTAgent(config);
        return new ResponseResult("POST", "success");
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
       return "Got it!";
    }
}
