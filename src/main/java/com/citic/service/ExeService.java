package com.citic.service;

import com.citic.entity.ResponseResult;
import com.citic.control.ExecuteCmd;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("executor")
public class ExeService {
    private static final ExecuteCmd EXECUTE_CMD = ExecuteCmd.INSTANCE;

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
