package com.example.service;

import com.example.*;
import com.example.entity.TAgent;
import com.example.entity.CanalInstance;
import com.example.entity.CanalServer;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "configuration" path)
 */
@Path("config")
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private VelocityEngine ve;

    public ConfigurationService() {
    }

    @POST
    @Path("canal/server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationResult postCanalServer(CanalServer config) {
        logger.debug("CanalServer: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanalServer(config);
        return new ConfigurationResult("POST", "success");
    }

    @POST
    @Path("canal/instance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationResult postCanalInstance(CanalInstance config) {
        logger.debug("CanalInstance: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanalInstance(config);
        return new ConfigurationResult("POST", "success");
    }

    @POST
    @Path("tagent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationResult postAgent(TAgent config) {
        logger.debug("TAgent: {}", config.toString());

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateTAgent(config);
        return new ConfigurationResult("POST", "success");
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
