package com.citic.control;

import com.citic.AppConf;
import com.citic.entity.CanalInstance;
import com.citic.entity.CanalServer;
import com.citic.entity.TAgent;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.citic.AppConstants.*;

import com.citic.helper.Utility;


public class GenerateConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConf.class);

    private VelocityEngine ve;
    private String templateDir = System.getProperty("template.dir", "classpath:template");

    public GenerateConf() {

        ve = new VelocityEngine();

        if (templateDir.startsWith(CLASSPATH_URL_PREFIX)) {
            templateDir = StringUtils.substringAfter(templateDir, CLASSPATH_URL_PREFIX);
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        } else {
            //设置velocity资源加载方式为file
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
            //设置velocity资源加载方式为file时的处理类
            ve.setProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        }

        ve.init();
    }

    private String getTemplatePath(String templateName) {
        String sep = System.getProperty("file.separator");
        return templateDir + sep + AppConf.getConfig(templateName);
    }


    /*
     * 根据请求的参数批量放入 VelocityContext
     * */
    private VelocityContext getVelContext(Object config) {
        VelocityContext ctx = new VelocityContext();
        Map<String, Object> beanPro = Utility.guavaBeanProperties(config);

        beanPro.forEach(ctx::put);
        return ctx;
    }

    /*
     * 根据目标和参数写配置文件
     * */
    private void writeConf(Template template, String confFilePath, VelocityContext ctx) {
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
        Utility.createParentDirs(confFilePath);
        try (PrintWriter out = new PrintWriter(confFilePath)) {
            out.print(sw.toString());
            out.flush();
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /*
     * 生成 Canal 相关的配置文件
     * */
    public void generateCanal(CanalServer config) {
        if (config == null) {
            return;
        }
        this.generateCanalServer(config);
        // 生成 instance 配置文件
        config.getInstances().forEach(this::generateCanalInstance);
        deleteUselessInstanceDirs(config.getInstances());
    }

    /*
     * 删除多余的 Instance 配置文件夹
     * */
    private void deleteUselessInstanceDirs(Set<CanalInstance> instanceSet) {
        File file = new File(getCanalConfDir());

        List<String> instanceDirs = Lists.newArrayList();
        instanceSet.forEach(instance -> instanceDirs.add(instance.getInstance()));

        String[] deleteDirs = file.list((current, name) -> {
            File thisFile = new File(current, name);
            return thisFile.isDirectory()
                && !instanceDirs.contains(name)
                && !name.equals("spring");
        });

        if (deleteDirs == null) {
            return;
        }
        try {
            for (String dir : deleteDirs) {
                String path = getCanalConfDir() + File.separator + dir;
                Utility.deleteFileOrFolder(Paths.get(path));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private String getCanalConfDir() {
        return AppConf.getConfig(CANAL_HOME_DIR) + File.separator + AppConf
            .getConfig(CANAL_CONF_DIR);
    }

    private String getCanalServerConf() {
        return getCanalConfDir() + File.separator + "canal.properties";
    }

    private String getCanalInstanceConf(String instanceName) {
        return getCanalConfDir() + File.separator
            + instanceName + File.separator + "instance.properties";
    }

    /*
     * 生成 Canal Server 的配置文件
     */
    private void generateCanalServer(CanalServer config) {
        if (config == null) {
            return;
        }
        VelocityContext vx = getVelContext(config);
        // canal server configuration
        LOGGER.info("{} path: {}", CANAL_SERVER_TEMPLATE, getTemplatePath(CANAL_SERVER_TEMPLATE));
        Template canalServer = ve.getTemplate(getTemplatePath(CANAL_SERVER_TEMPLATE), "utf-8");
        this.writeConf(canalServer, getCanalServerConf(), vx);
    }


    /*
     * 生成 Canal Instance 的配置文件
     */
    private void generateCanalInstance(CanalInstance config) {
        if (config == null) {
            return;
        }
        VelocityContext vx = getVelContext(config);
        // canal instance configuration
        Template canalInstance = ve.getTemplate(getTemplatePath(CANAL_INSTANCE_TEMPLATE), "utf-8");
        this.writeConf(canalInstance, getCanalInstanceConf(config.getInstance()), vx);
    }

    /*
     * 生成 TAgent 的配置文件
     */
    public void generateTAgent(TAgent config) {
        if (config == null) {
            return;
        }
        VelocityContext vx = getVelContext(config);
        // TAgent configuration
        Template canalServer = ve.getTemplate(getTemplatePath(TAGENT_TEMPLATE), "utf-8");
        String confPath =
            AppConf.getConfig(TAGENT_HOME_DIR) + File.separator + AppConf.getConfig(TAGENT_CONF);
        this.writeConf(canalServer, confPath, vx);
    }
}
