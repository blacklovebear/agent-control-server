package com.citic.service;

import com.citic.entity.ResponseResult;
import com.citic.control.ExecuteCmd;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("executor")
public class ExecutorService {
    @GET
    @Path("start/canal")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseResult startCanal() {
        ExecuteCmd exeCmd = new ExecuteCmd();
        int exitCode = exeCmd.startCanal();
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
        ExecuteCmd exeCmd = new ExecuteCmd();
        int exitCode = exeCmd.stopCanal();
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
        ExecuteCmd exeCmd = new ExecuteCmd();
        int exitCode = exeCmd.startTAgent();
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
        ExecuteCmd exeCmd = new ExecuteCmd();
        int exitCode = exeCmd.stopTAgent();
        String message = "success";
        if (exitCode != 0) {
            message = "error";
        }
        return new ResponseResult("GET", message);
    }
}
