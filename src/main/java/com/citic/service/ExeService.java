package com.citic.service;

import com.citic.control.ExecuteCmdController;
import com.citic.entity.ResponseResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The type Exe service.
 */
@Path("executor")
public class ExeService {

    private static final ExecuteCmdController EXECUTE_CMD = ExecuteCmdController.INSTANCE;

    /**
     * Start canal response result.
     *
     * @return the response result
     */
    @GET
    @Path("start/canal")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startCanal() {
        int exitCode = EXECUTE_CMD.startCanal();
        if (exitCode != 0) {
            return new ResponseResult(ResponseResult.ERROR, "canal start error");
        }
        return new ResponseResult();
    }

    /**
     * Stop canal response result.
     *
     * @return the response result
     */
    @GET
    @Path("stop/canal")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopCanal() {
        int exitCode = EXECUTE_CMD.stopCanal();
        if (exitCode != 0) {
            return new ResponseResult(ResponseResult.ERROR, "canal stop error");
        }
        return new ResponseResult();
    }

    /**
     * Start t agent response result.
     *
     * @return the response result
     */
    @GET
    @Path("start/tagent")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startTAgent() {
        int exitCode = EXECUTE_CMD.startTAgent();
        if (exitCode != 0) {
            return new ResponseResult(ResponseResult.ERROR, "TAgent start error");
        }
        return new ResponseResult();
    }

    /**
     * Stop t agent response result.
     *
     * @return the response result
     */
    @GET
    @Path("stop/tagent")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopTAgent() {
        int exitCode = EXECUTE_CMD.stopTAgent();
        if (exitCode != 0) {
            return new ResponseResult(ResponseResult.ERROR, "TAgent stop error");
        }
        return new ResponseResult();
    }
}
