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
    public ResponseResult configUnion(UnionConfig unionConfig) throws Exception {
        LOGGER.debug("UnionConfig: {}", unionConfig.toString());

        unionConfig.checkProperties();
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
    public ResponseResult addUnionConfigUnit(UnionConfig.Unit unitConfig) throws Exception {
        LOGGER.debug("UnitConfig: {}", unitConfig.toString());

        UnionConfig unionConfig =  AppGlobal.getUnionConfig();
        if (unionConfig == null) {
            throw new Exception("Not post UnionConfig info");
        }

        unitConfig.checkProperties();

        unionConfig.addOrReplaceUnit(unitConfig);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanal(unionConfig.getCanalServer());
        generateConf.generateTAgent(unionConfig.getTAgent());
        return new ResponseResult();

    }
}
