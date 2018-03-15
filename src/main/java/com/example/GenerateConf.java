package com.example;

import com.example.entity.CanalInstance;
import com.example.entity.CanalServer;
import com.example.entity.TAgent;
import com.example.helper.ClassHelper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GenerateConf {
    private static final Logger logger = LoggerFactory.getLogger(GenerateConf.class);

    private static final String CANAL_SERVER_TEMPLATE = "canal.server.template";
    private static final String CANAL_SERVER_CONF = "canal.server.conf";

    private static final String CANAL_INSTANCE_TEMPLATE = "canal.instance.template";
    private static final String CANAL_INSTANCE_CONF = "canal.instance.conf";

    private static final String TAGENT_TEMPLATE = "tagent.template";
    private static final String TAGENT_CONF = "tagent.conf";

    private ClassHelper helper;
    private VelocityEngine ve;
    private ApplicationConf appConf;

    public GenerateConf() {
        this.helper = new ClassHelper();

        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        ve.init();

        appConf = ApplicationConf.getInstance();
    }

    private void createParentDirs(String filePath) {
        Path file = Paths.get(filePath);
        Path parent = file.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                //fail to create directory
                logger.error(e.getMessage(), e);
            }
        }
    }

    private VelocityContext getVelContext(Object config) {
        VelocityContext ctx = new VelocityContext();
        String[] fieldNameList =  helper.getFiledName(config);
        // put all attributes
        for(String fieldName : fieldNameList) {
            ctx.put(fieldName, helper.getFieldValueByName(fieldName, config));
        }
        return ctx;
    }

    private void writeConf(Template template, String confFilePath, VelocityContext ctx) {
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
        createParentDirs(confFilePath);
        PrintWriter out = null;
        try {
            out = new PrintWriter(confFilePath);
            out.print(sw.toString());
            out.flush();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /*
    * 生成 Canal Server 的配置文件
    */
    public void generateCanalServer(CanalServer config) {
        VelocityContext vx = getVelContext(config);
        // canal server configuration
        Template canalServer = ve.getTemplate(appConf.getConfig(CANAL_SERVER_TEMPLATE), "utf-8");
        this.writeConf(canalServer, appConf.getConfig(CANAL_SERVER_CONF), vx);
    }

    /*
    * 生成 Canal Instance 的配置文件
    */
    public void generateCanalInstance(CanalInstance config) {
        VelocityContext vx = getVelContext(config);
        // default instance name
        if (config.getInstance() == null) {
            config.setInstance("example");
        }
        // canal instance configuration
        Template canalInstance = ve.getTemplate(appConf.getConfig(CANAL_INSTANCE_TEMPLATE), "utf-8");
        String instancePath = String.format(appConf.getConfig(CANAL_INSTANCE_CONF), config.getInstance());
        this.writeConf(canalInstance, instancePath, vx);
    }

    /*
    * 生成 TAgent 的配置文件
    */
    public void generateTAgent(TAgent config) {
        VelocityContext vx = getVelContext(config);
        // TAgent configuration
        Template canalServer = ve.getTemplate(appConf.getConfig(TAGENT_TEMPLATE), "utf-8");
        this.writeConf(canalServer, appConf.getConfig(TAGENT_CONF), vx);
    }
}
