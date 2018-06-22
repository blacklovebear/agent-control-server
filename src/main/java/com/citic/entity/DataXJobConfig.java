package com.citic.entity;

import com.citic.AppConf;
import com.citic.helper.AesUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * The type Data x job config.
 */
public class DataXJobConfig {

    private String jobId;
    // 用于任务执行完成返回执行结果给管控平台
    private String responseUrl;
    private Reader reader;
    private Writer writer;


    /**
     * Check properties.
     */
    public void checkProperties() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobId), "jobId is null or empty");
        Preconditions
            .checkArgument(!Strings.isNullOrEmpty(responseUrl), "responseUrl is null or empty");
        Preconditions.checkNotNull(reader, "reader is null");
        Preconditions.checkNotNull(writer, "writer is null");

        reader.checkProperties();
        writer.checkProperties();
    }

    /**
     * Gets response url.
     *
     * @return the response url
     */
    public String getResponseUrl() {
        return responseUrl;
    }

    /**
     * Sets response url.
     *
     * @param responseUrl the response url
     */
    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    /**
     * Gets job id.
     *
     * @return the job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Gets reader.
     *
     * @return the reader
     */
    public Reader getReader() {
        return reader;
    }

    /**
     * Sets reader.
     *
     * @param reader the reader
     */
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Gets writer.
     *
     * @return the writer
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Sets writer.
     *
     * @param writer the writer
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * The type Reader.
     */
    public static class Reader {

        private String username;
        private String password;
        private String querySql;
        private String jdbcUrl;

        private void checkProperties() {
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(username), "username is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(password), "password is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(querySql), "querySql is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(jdbcUrl), "jdbcUrl is null or empty");
            Preconditions.checkArgument(AesUtil.decForTd(password) != null,
                "reader password decrypt error");
        }

        /**
         * Gets query sql.
         *
         * @return the query sql
         */
        public String getQuerySql() {
            return querySql;
        }

        /**
         * Sets query sql.
         *
         * @param querySql the query sql
         */
        public void setQuerySql(String querySql) {
            this.querySql = querySql;
        }

        /**
         * Gets jdbc url.
         *
         * @return the jdbc url
         */
        public String getJdbcUrl() {
            return jdbcUrl;
        }

        /**
         * Sets jdbc url.
         *
         * @param jdbcUrl the jdbc url
         */
        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        /**
         * Gets username.
         *
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets username.
         *
         * @param username the username
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Gets password.
         *
         * @return the password
         */
        public String getPassword() {
            // 通过管理平台传过来的秘密为密文
            if (AppConf.isCanalPasswordEncrypt()) {
                // 配置文件中保持密文密码
                return password;
            } else {
                // 转换为明文密码
                return AesUtil.decForTd(password);
            }
        }

        /**
         * Sets password.
         *
         * @param password the password
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * The type Writer.
     */
    public static class Writer {

        private String host;
        private String port;
        private String username;
        private String password;
        private String path;
        private String fileName;
        private String writeMode;
        private String fieldDelimiter;
        private String encoding;
        private String fileFormat;

        private void checkProperties() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "host is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(port), "port is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(username), "username is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(password), "password is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(fileName), "fileName is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(writeMode), "writeMode is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldDelimiter),
                "fieldDelimiter is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(encoding), "encoding is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(fileFormat), "fileFormat is null or empty");
            Preconditions.checkArgument(AesUtil.decForTd(password) != null,
                "reader password decrypt error");
        }

        /**
         * Gets host.
         *
         * @return the host
         */
        public String getHost() {
            return host;
        }

        /**
         * Sets host.
         *
         * @param host the host
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Gets port.
         *
         * @return the port
         */
        public String getPort() {
            return port;
        }

        /**
         * Sets port.
         *
         * @param port the port
         */
        public void setPort(String port) {
            this.port = port;
        }

        /**
         * Gets path.
         *
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * Sets path.
         *
         * @param path the path
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * Gets file name.
         *
         * @return the file name
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Sets file name.
         *
         * @param fileName the file name
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Gets write mode.
         *
         * @return the write mode
         */
        public String getWriteMode() {
            return writeMode;
        }

        /**
         * Sets write mode.
         *
         * @param writeMode the write mode
         */
        public void setWriteMode(String writeMode) {
            this.writeMode = writeMode;
        }

        /**
         * Gets field delimiter.
         *
         * @return the field delimiter
         */
        public String getFieldDelimiter() {
            return fieldDelimiter;
        }

        /**
         * Sets field delimiter.
         *
         * @param fieldDelimiter the field delimiter
         */
        public void setFieldDelimiter(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        /**
         * Gets encoding.
         *
         * @return the encoding
         */
        public String getEncoding() {
            return encoding;
        }

        /**
         * Sets encoding.
         *
         * @param encoding the encoding
         */
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        /**
         * Gets file format.
         *
         * @return the file format
         */
        public String getFileFormat() {
            return fileFormat;
        }

        /**
         * Sets file format.
         *
         * @param fileFormat the file format
         */
        public void setFileFormat(String fileFormat) {
            this.fileFormat = fileFormat;
        }

        /**
         * Gets username.
         *
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets username.
         *
         * @param username the username
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Gets password.
         *
         * @return the password
         */
        public String getPassword() {
            // 通过管理平台传过来的秘密为密文
            if (AppConf.isCanalPasswordEncrypt()) {
                // 配置文件中保持密文密码
                return password;
            } else {
                // 转换为明文密码
                return AesUtil.decForTd(password);
            }
        }

        /**
         * Sets password.
         *
         * @param password the password
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

}
