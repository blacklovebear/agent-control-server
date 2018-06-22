package com.citic.entity;

import com.citic.helper.AesUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class DataXJobConfig {

    private String jobId;
    // 用于任务执行完成返回执行结果给管控平台
    private String responseUrl;
    private Reader reader;
    private Writer writer;


    /**
     * Check properties.
     *
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

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

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

        public String getQuerySql() {
            return querySql;
        }

        public void setQuerySql(String querySql) {
            this.querySql = querySql;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return AesUtil.decForTd(password);
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

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

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getWriteMode() {
            return writeMode;
        }

        public void setWriteMode(String writeMode) {
            this.writeMode = writeMode;
        }

        public String getFieldDelimiter() {
            return fieldDelimiter;
        }

        public void setFieldDelimiter(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getFileFormat() {
            return fileFormat;
        }

        public void setFileFormat(String fileFormat) {
            this.fileFormat = fileFormat;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return AesUtil.decForTd(password);
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
