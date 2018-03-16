package com.citic.control;

import com.citic.ApplicationConf;
import com.citic.Main;
import com.citic.entity.CanalInstance;
import com.citic.entity.CanalServer;
import com.citic.entity.TAgent;
import com.citic.helper.ClassHelper;
import org.apache.commons.lang.StringUtils;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConf.class);

    private static final String CANAL_SERVER_TEMPLATE = "canal.server.template";
    private static final String CANAL_SERVER_CONF = "canal.server.conf";

    private static final String CANAL_INSTANCE_TEMPLATE = "canal.instance.template";
    private static final String CANAL_INSTANCE_CONF = "canal.instance.conf";

    private static final String TAGENT_TEMPLATE = "tagent.template";
    private static final String TAGENT_CONF = "tagent.conf";

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    private ClassHelper helper;
    private VelocityEngine ve;
    private ApplicationConf appConf;
    private String templateDir = System.getProperty("template.dir", "classpath:template");

    public GenerateConf() {
        this.helper = new ClassHelper();

        ve = new VelocityEngine();

        appConf = ApplicationConf.getInstance();

        if (templateDir.startsWith(CLASSPATH_URL_PREFIX)) {
            templateDir = StringUtils.substringAfter(templateDir, CLASSPATH_URL_PREFIX);
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class",
                    ClasspathResourceLoader.class.getName());
        } else {
            //设置velocity资源加载方式为file
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
            //设置velocity资源加载方式为file时的处理类
            ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        }

        ve.init();
    }

    private String getTemplatePath(String templateName) {
        String sep = System.getProperty("file.separator");
        return templateDir + "/" + appConf.getConfig(templateName);
    }

    /*
    * 根据传入的路径生成父文件夹路径
    * */
    private void createParentDirs(String filePath) {
        Path file = Paths.get(filePath);
        Path parent = file.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                //fail to create directory
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /*
    * 根据请求的参数批量放入 VelocityContext
    * */
    private VelocityContext getVelContext(Object config) {
        VelocityContext ctx = new VelocityContext();
        String[] fieldNameList =  helper.getFiledName(config);
        // put all attributes
        for(String fieldName : fieldNameList) {
            ctx.put(fieldName, helper.getFieldValueByName(fieldName, config));
        }
        return ctx;
    }

    /*
    * 根据目标和参数写配置文件
    * */
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
            LOGGER.error(e.getMessage(), e);
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
        LOGGER.info("{} path: {}", CANAL_SERVER_TEMPLATE, getTemplatePath(CANAL_SERVER_TEMPLATE));
        Template canalServer = ve.getTemplate(getTemplatePath(CANAL_SERVER_TEMPLATE), "utf-8");
        this.writeConf(canalServer, appConf.getConfig(CANAL_SERVER_CONF), vx);
    }

    /*
    * 生成 Canal Instance 的配置文件
    */
    public void generateCanalInstance(CanalInstance config) {
        VelocityContext vx = getVelContext(config);
        // default instance name
        if (config.getInstance() == null) {
            config.setInstance("citic");
        }
        // canal instance configuration
        Template canalInstance = ve.getTemplate(getTemplatePath(CANAL_INSTANCE_TEMPLATE), "utf-8");
        String instancePath = String.format(appConf.getConfig(CANAL_INSTANCE_CONF), config.getInstance());
        this.writeConf(canalInstance, instancePath, vx);
    }

    /*
    * 生成 TAgent 的配置文件
    */
    public void generateTAgent(TAgent config) {
        VelocityContext vx = getVelContext(config);
        // TAgent configuration
        Template canalServer = ve.getTemplate(getTemplatePath(TAGENT_TEMPLATE), "utf-8");
        this.writeConf(canalServer, appConf.getConfig(TAGENT_CONF), vx);
    }
}
