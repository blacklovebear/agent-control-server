package com.citic.service;

import com.citic.entity.ResponseResult;
import com.citic.control.ExecuteCmd;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("executor")
public class ExeService {
    private static final ExecuteCmd EXECUTE_CMD = ExecuteCmd.getInstance();

    @GET
    @Path("start/canal")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startCanal() {
        int exitCode = EXECUTE_CMD.startCanal();
        String message = "success";
        if (exitCode != 0) {
            message = "error";
        }
        return new ResponseResult("GET", message);
    }

    @GET
    @Path("stop/canal")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopCanal() {
        int exitCode = EXECUTE_CMD.stopCanal();
        String message = "success";
        if (exitCode != 0) {
            message = "error";
        }
        return new ResponseResult("GET", message);
    }

    @GET
    @Path("start/tagent")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startTAgent() {
        int exitCode = EXECUTE_CMD.startTAgent();
        String message = "success";
        if (exitCode != 0) {
            message = "error";
        }
        return new ResponseResult("GET", message);
    }

    @GET
    @Path("stop/tagent")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult stopTAgent() {
        int exitCode = EXECUTE_CMD.stopTAgent();
        String message = "success";
        if (exitCode != 0) {
            message = "error";
        }
        return new ResponseResult("GET", message);
    }
}
