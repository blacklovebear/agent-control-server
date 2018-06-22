package com.citic.entity;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 统一异常处理类
 * */
@Provider
public class MyExceptionMapper implements ExceptionMapper<Throwable> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        LOGGER.error(e.toString());
        return Response.status(Response.Status.OK)
            .entity(new ResponseResult(ResponseResult.ERROR, e.toString()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
