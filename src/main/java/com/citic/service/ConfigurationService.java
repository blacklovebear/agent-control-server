package com.citic.service;

import com.citic.AppGlobal;
import com.citic.AppMain;
import com.citic.control.GenerateConf;
import com.citic.entity.ResponseResult;
import com.citic.entity.UnionConfig;
import com.google.common.collect.Lists;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Configuration service.
 */
@Path("config")
public class ConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

    /**
     * Config union response result.
     *
     * @param unionConfig the union config
     * @return the response result
     * @throws Exception the exception
     */
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

        List<String> instanceList = Lists.newArrayList();
        unionConfig.getUnits().forEach(unit -> {
            instanceList.add(unit.getInstance());
        });
        AppMain.getErrorLogMonitor().start(instanceList);

        return new ResponseResult();
    }

    /**
     * Add union config unit response result.
     *
     * @param unitConfig the unit config
     * @return the response result
     * @throws Exception the exception
     */
    @POST
    @Path("union/unit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult addUnionConfigUnit(UnionConfig.Unit unitConfig) throws Exception {
        LOGGER.debug("UnitConfig: {}", unitConfig.toString());

        UnionConfig unionConfig = AppGlobal.getUnionConfig();
        if (unionConfig == null) {
            throw new Exception("Not post UnionConfig info");
        }

        unitConfig.checkProperties(unionConfig.isUseAvro());

        unionConfig.addOrReplaceUnit(unitConfig);

        GenerateConf generateConf = new GenerateConf();
        generateConf.generateCanal(unionConfig.getCanalServer());
        generateConf.generateTAgent(unionConfig.getTAgent());

        List<String> instanceList = Lists.newArrayList();
        unionConfig.getUnits().forEach(unit -> {
            instanceList.add(unit.getInstance());
        });
        AppMain.getErrorLogMonitor().start(instanceList);

        return new ResponseResult();

    }
}
