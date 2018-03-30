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
    @Path("union")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult configUnion(UnionConfig unionConfig) {
        LOGGER.debug("UnionConfig: {}", unionConfig.toString());

        try {
            unionConfig.checkProperties();
        } catch (Exception e) {
            return new ResponseResult(ResponseResult.ERROR, e.getMessage());
        }

        AppGlobal.setUnionConfig(unionConfig);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanal(unionConfig.getCanalServer());
        generateConf.generateTAgent(unionConfig.getTAgent());

        return new ResponseResult();
    }

    @POST
    @Path("union/unit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult addUnionConfigUnit(UnionConfig.Unit unitConfig) {
        LOGGER.debug("UnitConfig: {}", unitConfig.toString());

        UnionConfig unionConfig =  AppGlobal.getUnionConfig();
        if (unionConfig == null) {
            return new ResponseResult(ResponseResult.ERROR, "Not post UnionConfig info");
        }

        try {
            unitConfig.checkProperties();
        } catch (Exception e) {
            return new ResponseResult(ResponseResult.ERROR, e.getMessage());
        }

        unionConfig.addOrReplaceUnit(unitConfig);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanal(unionConfig.getCanalServer());
        generateConf.generateTAgent(unionConfig.getTAgent());
        return new ResponseResult();

    }
}
