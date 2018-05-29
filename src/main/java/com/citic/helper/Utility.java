package com.citic.helper;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Throwables.propagate;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Utility.
 */
public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);
    private static final String DEFAULT_IP = "127.0.0.1";
    
    /**
     * Exe cmd int.
     *
     * @param homeDir the home dir
     * @param cmd the cmd
     * @return the int
     */
    public static int exeCmd(String homeDir, String cmd) {
        int exitCode = 0;
        ShellExecutor executor = new ShellExecutor(homeDir);
        try {
            exitCode = executor.executeCmd(cmd, false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return exitCode;
    }


    /**
     * 根据传入的路径生成父文件夹路径.
     *
     * @param filePath the file path
     */
    public static void createParentDirs(String filePath) {
        Path file = Paths.get(filePath);
        Path parent = file.getParent();
        if (parent == null) {
            return;
        }
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                //fail to create directory
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Gets local ip.
     *
     * @param interfaceName the interface name
     * @return the local ip
     */
    public static String getLocalIp(String interfaceName) {
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

    /**
     * 通过反射,从getter方法中获取 对于的属性值.
     *
     * @param bean the bean
     * @return the map
     */
    public static Map<String, Object> guavaBeanProperties(Object bean) {
        Object nullObject = new Object();
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
                                    firstNonNull(result, nullObject));
                            } catch (Exception e) {
                                throw propagate(e);
                            }
                        },
                        (left, right) -> left.putAll(right.build()))
                    .build(), v -> v == nullObject ? null : v);
        } catch (IntrospectionException e) {
            throw propagate(e);
        }
    }

    /**
     * Is urls address list valid.
     *
     * @param serverUrls the server urls
     * @param fieldName the field name
     * @throws ServerUrlsFormatException the server urls format exception
     */
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

    /**
     * Is url address valid.
     *
     * @param serverUrl the server url
     * @param fieldName the field name
     * @throws Exception the exception
     */
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

    /**
     * Delete file or folder.
     *
     * @param path the path
     * @throws IOException the io exception
     */
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

    private static class ServerUrlsFormatException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new Server urls format exception.
         *
         * @param msg the msg
         */
        ServerUrlsFormatException(String msg) {
            super(msg);
        }

        /**
         * Instantiates a new Server urls format exception.
         *
         * @param msg the msg
         * @param throwable the throwable
         */
        ServerUrlsFormatException(String msg, Throwable throwable) {
            super(msg, throwable);
        }
    }

    ;
}
