package com.sixthday.kafka.connect.jdbc.config;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class CustomJDBCSinkConfig extends AbstractConfig {

    public static final String BATCH_SIZE = "batch.size";
    public static final String CATALOG = "catalog";
    public static final String COLUMNS_MAPPING = ".columns.mapping";
    public static final String PRODUCER_PREFIX = "producer.";
    public static final String DATABASE_PREFIX = "database.";
    public static final String LOGIN_TIMEOUT = "login.timeout.seconds";
    public static final String MAX_RETRIES = "max.retries";
    public static final String MODE = ".mode";
    public static final String ERRORS_LOG_ENABLE = "errors.log.enable";
    public static final String ERRORS_LOG_INCLUDE_MESSAGE = "errors.log.include.messages";
    public static final String ERRORS_DLT_TOPIC_NAME = "errors.deadletterqueue.topic.name";
    public static final String ERRORS_ENABLE_DLT_HEADERS = "errors.deadletterqueue.context.headers.enable";
    public static final String MODE_DEFAULT = "insert";
    public static final String MODE_INSERT_EXCLUDE_COLUMNS = MODE + ".insert.exclude.columns";
    public static final String MODE_UPDATE_KEYS = MODE + ".update.keys";
    public static final String MODE_UPDATE_CLAUSE = MODE + ".update.clause";
    public static final String MODE_UPDATE_COLUMNS = MODE + ".update.columns";
    public static final String PASSWORD = "password";
    public static final String PAYLOAD = ".payload";
    public static final String PAYLOAD_DEFAULT = "$";
    public static final String PORT = "port";
    public static final String RETRY_BACKOFF_MS = "retry.backoff.ms";
    public static final String SCHEMA = "schema";
    public static final String HOST = "host";
    public static final String TABLES = "tables";
    public static final String TABLES_PREFIX = "tables.";
    public static final String USERNAME = "username";
    private static final int BATCH_SIZE_DEFAULT = 1000;
    private static final int LOGIN_TIMEOUT_DEFAULT = 15;
    private static final int MAX_RETRIES_DEFAULT = 10;
    private static final long RETRY_BACKOFF_MS_DEFAULT = 1000;
    private static final String BATCH_SIZE_DOCS = "";
    private static final String CATALOG_DOC = "";
    private static final String LOGIN_TIMEOUT_DOC = "";
    private static final String MAX_RETRIES_DOC = "";
    private static final String PASSWORD_DOC = "";
    private static final String PORT_DOC = "";
    private static final String RETRY_BACKOFF_MS_DOCS = "";
    private static final String SCHEMA_DOC = "";
    private static final String HOST_DOC = "";
    private static final String TABLES_DOC = "";
    private static final String USERNAME_DOC = "";

    public CustomJDBCSinkConfig(Map<?, ?> props) {
        super(configDef(), props);
    }

    public static ConfigDef configDef() {
        return new ConfigDef()
                .define(
                        BATCH_SIZE,
                        ConfigDef.Type.INT,
                        BATCH_SIZE_DEFAULT,
                        ConfigDef.Importance.LOW,
                        BATCH_SIZE_DOCS
                ).define(
                        DATABASE_PREFIX + CATALOG,
                        ConfigDef.Type.STRING,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        CATALOG_DOC
                ).define(
                        DATABASE_PREFIX + PORT,
                        ConfigDef.Type.INT,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        PORT_DOC
                ).define(
                        DATABASE_PREFIX + PASSWORD,
                        ConfigDef.Type.STRING,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        PASSWORD_DOC
                ).define(
                        DATABASE_PREFIX + SCHEMA,
                        ConfigDef.Type.STRING,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        SCHEMA_DOC
                ).define(
                        DATABASE_PREFIX + HOST,
                        ConfigDef.Type.STRING,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        HOST_DOC
                ).define(
                        DATABASE_PREFIX + USERNAME,
                        ConfigDef.Type.STRING,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        USERNAME_DOC
                ).define(
                        LOGIN_TIMEOUT,
                        ConfigDef.Type.INT,
                        LOGIN_TIMEOUT_DEFAULT,
                        ConfigDef.Importance.LOW,
                        LOGIN_TIMEOUT_DOC
                ).define(
                        MAX_RETRIES,
                        ConfigDef.Type.INT,
                        MAX_RETRIES_DEFAULT,
                        ConfigDef.Importance.LOW,
                        MAX_RETRIES_DOC
                ).define(
                        RETRY_BACKOFF_MS,
                        ConfigDef.Type.LONG,
                        RETRY_BACKOFF_MS_DEFAULT,
                        ConfigDef.Importance.LOW,
                        RETRY_BACKOFF_MS_DOCS
                ).define(
                        TABLES,
                        ConfigDef.Type.LIST,
                        ConfigDef.NO_DEFAULT_VALUE,
                        ConfigDef.Importance.HIGH,
                        TABLES_DOC
                );
    }

    public String updateKeys(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + MODE_UPDATE_KEYS, "")
                .toString().trim();
    }

    public String updateClause(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + MODE_UPDATE_CLAUSE, "")
                .toString().trim();
    }

    public String updateColumns(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + MODE_UPDATE_COLUMNS, "")
                .toString().trim();
    }

    public String columnMapping(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + COLUMNS_MAPPING, "")
                .toString().trim();
    }

    public String insertExcludeColumn(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + MODE_INSERT_EXCLUDE_COLUMNS, "")
                .toString().trim();
    }

    public String mode(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + MODE,
                MODE_DEFAULT).toString().trim();
    }

    public String payload(String tableName) {
        return originals().getOrDefault(TABLES_PREFIX + tableName + PAYLOAD,
                PAYLOAD_DEFAULT).toString().trim();
    }

    public Boolean isErrorsLogEnabled() {
        return Boolean.valueOf(originals().getOrDefault(ERRORS_LOG_ENABLE, true).toString());
    }

    public Boolean isErrorsLogIncludeMessage() {
        return Boolean.valueOf(originals().getOrDefault(ERRORS_LOG_INCLUDE_MESSAGE, true).toString());
    }

    public Boolean isErrorsEnableDLTHeaders() {
        return Boolean.valueOf(originals().getOrDefault(ERRORS_ENABLE_DLT_HEADERS, false).toString());
    }

    public String dltName() {
        return originals().getOrDefault(ERRORS_DLT_TOPIC_NAME, "").toString().trim();
    }

    public Map<String, Object> producerConfig() {
        return originalsWithPrefix(PRODUCER_PREFIX);
    }
}
