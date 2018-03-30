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
        // TODO 对所有属性缺失情况,以及格式进行校验
        LOGGER.debug("UnionConfig: {}", unionConfig.toString());

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
        // TODO 对所有属性缺失情况,以及格式进行校验
        LOGGER.debug("UnitConfig: {}", unitConfig.toString());

        UnionConfig unionConfig =  AppGlobal.getUnionConfig();
        if (unionConfig == null) {
            return new ResponseResult(ResponseResult.ERROR, "Not post UnionConfig info");
        }

        unionConfig.addUnit(unitConfig);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanal(unionConfig.getCanalServer());
        generateConf.generateTAgent(unionConfig.getTAgent());

        return new ResponseResult();
    }
}
