package com.citic.helper;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Throwables.propagate;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);
    private static final String DEFAULT_IP = "127.0.0.1";


    public static int exeCmd(String homeDir, String cmd) {
        int exitCode = 0;
        ShellExecutor executor = new ShellExecutor(homeDir);
        try {
            exitCode = executor.executeCmd(cmd);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return exitCode;
    }


    /*
     * 根据传入的路径生成父文件夹路径
     * */
    public static void createParentDirs(String filePath) {
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
     * 获取本机ip
     * */
    public static String getLocalIP(String interfaceName) {
        String ip = DEFAULT_IP;
        Enumeration<?> e1 = null;
        try {
            e1 = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.error(e.getMessage(), e);
            return ip;
        }

        while (e1.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) e1.nextElement();
            if (ni.getName().equals(interfaceName)) {
                Enumeration<?> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress ia = (InetAddress) e2.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;
                    }
                    ip = ia.getHostAddress();
                }
                break;
            }
        }
        return ip;
    }

    /*
     * 通过反射,从getter方法中获取 对于的属性值
     * */
    public static Map<String, Object> guavaBeanProperties(Object bean) {
        Object NULL = new Object();
        try {
            return Maps.transformValues(
                Arrays.stream(
                    Introspector.getBeanInfo(bean.getClass(), Object.class)
                        .getPropertyDescriptors())
                    .filter(pd -> Objects.nonNull(pd.getReadMethod()))
                    .collect(ImmutableMap::<String, Object>builder,
                        (builder, pd) -> {
                            try {
                                Object result = pd.getReadMethod()
                                    .invoke(bean);
                                builder.put(pd.getName(),
                                    firstNonNull(result, NULL));
                            } catch (Exception e) {
                                throw propagate(e);
                            }
                        },
                        (left, right) -> left.putAll(right.build()))
                    .build(), v -> v == NULL ? null : v);
        } catch (IntrospectionException e) {
            throw propagate(e);
        }
    }

    public static void isUrlsAddressListValid(String serverUrls, String fieldName)
        throws ServerUrlsFormatException {
        String errMessage = String.format("The %s are malformed. The %s : \"%s\" .",
            fieldName, fieldName, serverUrls);

        if (StringUtils.isNotEmpty(serverUrls)) {
            for (String serverUrl : serverUrls.split(",")) {
                if (!Strings.isNullOrEmpty(serverUrl)) {
                    try {
                        isUrlAddressValid(serverUrl, fieldName);
                    } catch (Exception exception) {
                        throw new ServerUrlsFormatException(errMessage, exception);
                    }
                } else {
                    throw new ServerUrlsFormatException(errMessage);
                }
            }
        }
    }

    public static void isUrlAddressValid(String serverUrl, String fieldName) throws Exception {
        String errMessage = String.format("The %s are malformed. The %s : \"%s\" .",
            fieldName, fieldName, serverUrl);

        String[] hostAndPort = serverUrl.split(":");
        if (hostAndPort.length == 2 && !Strings.isNullOrEmpty(hostAndPort[1])) {
            try {
                Integer.parseInt(hostAndPort[1]);
            } catch (Exception e) {
                throw new Exception(errMessage);
            }
        } else {
            throw new Exception(errMessage);
        }
    }

    private static class ServerUrlsFormatException extends Exception {

        private static final long serialVersionUID = 1L;

        ServerUrlsFormatException(String msg) {
            super(msg);
        }

        ServerUrlsFormatException(String msg, Throwable throwable) {
            super(msg, throwable);
        }
    }

    /*
     * get schema String
     * */
    public static String getTableFieldSchema(List<String> schemaFieldList, String schemaName) {
        List<String> resultList = Lists.newArrayList();
        String schema = "{"
            + "\"type\":\"record\","
            + "\"name\":\"" + schemaName + "\","
            + "\"fields\":[";

        for (String fieldStr : schemaFieldList) {
            String field = "{ \"name\":\"" + fieldStr + "\", \"type\":\"string\" }";
            resultList.add(field);
        }
        schema += Joiner.on(",").join(resultList);
        schema += "]}";
        return schema;
    }


    /*
     * deleteFileOrFolder
     * */
    public static void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                e.printStackTrace(); // replace with more robust error handling
                return TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                throws IOException {
                if (e != null) {
                    return handleException(e);
                }
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    ;
}
