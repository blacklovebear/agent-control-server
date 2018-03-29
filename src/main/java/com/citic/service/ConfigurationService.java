package com.citic.service;

import com.citic.*;
import com.citic.entity.*;
import com.citic.control.GenerateConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("config")
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

    @POST
    @Path("canal/server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configCanalServer(CanalServer config) {
        LOGGER.debug("CanalServer: {}", config.toString());
        // 更新当前全局的 CanalServer
        AppGlobal.setCanalServer(config);

        GenerateConf generateConf = new GenerateConf();
        // 生成 Server 配置文件
        generateConf.generateCanalServer(config);
        // 生成 instance 配置文件
        config.getInstances().forEach(generateConf::generateCanalInstance);

        return new ResponseResult();
    }

    @POST
    @Path("canal/instance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult addCanalInstance(CanalInstance instanceConfig) {
        LOGGER.debug("CanalInstance: {}", instanceConfig.toString());
        CanalServer canalServer = AppGlobal.getCanalServer();
        if (canalServer == null) {
            return new ResponseResult(ResponseResult.ERROR, "Not post Canal Server info");
        }
        canalServer.addInstance(instanceConfig);

        GenerateConf generateConf = new GenerateConf();
        // 生成 Server 配置文件
        generateConf.generateCanalServer(canalServer);
        // 生成 instance 配置文件
        canalServer.getInstances().forEach(generateConf::generateCanalInstance);
        return new ResponseResult();
    }

    @POST
    @Path("tagent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configAgent(TAgent config) {
        LOGGER.debug("TAgent: {}", config.toString());
        // 更新当前全局的 TAgent
        AppGlobal.setTAgent(config);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateTAgent(config);
        return new ResponseResult();
    }

    @POST
    @Path("tagent/source")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult addAgentSource(TAgent.Source sourceConfig) {
        LOGGER.debug("Source: {}", sourceConfig.toString());
        TAgent tAgent =  AppGlobal.getTAgent();
        if (tAgent == null) {
            return new ResponseResult(ResponseResult.ERROR, "Not post TAgent info");
        }
        tAgent.addSource(sourceConfig);
        GenerateConf generateConf = new GenerateConf();
        generateConf.generateTAgent(tAgent);
        return new ResponseResult();
    }

    @POST
    @Path("all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configAll(AllConfig allConfig) {
        LOGGER.debug("AllConfig: {}", allConfig.toString());

        GenerateConf generateConf = new GenerateConf();

        generateConf.generateCanalServer(allConfig.getCanalServer());
        generateConf.generateCanalInstance(allConfig.getCanalInstance());
        generateConf.generateTAgent(allConfig.getTagent());

        return new ResponseResult();
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
