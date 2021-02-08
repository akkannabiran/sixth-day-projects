package com.sixthday.kafka.connect.jdbc.database;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.exception.NotImplementedException;
import com.sixthday.kafka.connect.jdbc.json.JsonColumnDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PostgreSqlDatabaseDialect implements DatabaseDialect {

    private final Logger LOGGER = LoggerFactory.getLogger(PostgreSqlDatabaseDialect.class);
    private final CustomJDBCSinkConfig customJdbcSinkConfig;
    private List<String> supportedTypes;
    private Connection connection;

    public PostgreSqlDatabaseDialect(CustomJDBCSinkConfig customJdbcSinkConfig) {
        this.customJdbcSinkConfig = customJdbcSinkConfig;
        this.supportedTypes = supportedTypes();
    }

    private List<String> supportedTypes() {
        supportedTypes = new ArrayList<>();
        supportedTypes.add("int2");
        supportedTypes.add("int8");
        supportedTypes.add("bigserial");
        supportedTypes.add("varchar");
        supportedTypes.add("bit");
        supportedTypes.add("bool");
        supportedTypes.add("date");
        supportedTypes.add("float8");
        supportedTypes.add("float4");
        supportedTypes.add("int4");
        supportedTypes.add("json");
        supportedTypes.add("jsonb");
        supportedTypes.add("name");
        supportedTypes.add("numeric");
        supportedTypes.add("serial");
        supportedTypes.add("smallserial");
        supportedTypes.add("text");
        supportedTypes.add("timetz");
        supportedTypes.add("time");
        supportedTypes.add("timestamptz");
        supportedTypes.add("timestamp");
        return supportedTypes;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        DriverManager.setLoginTimeout(customJdbcSinkConfig.getInt(CustomJDBCSinkConfig.LOGIN_TIMEOUT));

        String url = "jdbc:postgresql://"
                + customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.HOST)
                + ":" + customJdbcSinkConfig.getInt(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.PORT)
                + "/" + customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.CATALOG)
                + "?user=" + customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.USERNAME)
                + "&password=" + customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.PASSWORD);

        int maxRetries = customJdbcSinkConfig.getInt(CustomJDBCSinkConfig.MAX_RETRIES);

        for (int i = 1; i <= maxRetries; i++) {
            try {
                connection = DriverManager.getConnection(url);
                LOGGER.info("Database connection established!");
                break;
            } catch (SQLException e) {
                if (i == maxRetries) {
                    throw e;
                }
                try {
                    LOGGER.warn("Unable to connect the database '{}', retrying {}/{}.", e.getMessage(), i, maxRetries);
                    TimeUnit.MILLISECONDS.sleep(customJdbcSinkConfig.getLong(CustomJDBCSinkConfig.RETRY_BACKOFF_MS));
                } catch (InterruptedException ex) {
                    if (customJdbcSinkConfig.isErrorsLogEnabled())
                        LOGGER.error("Exception caught {}, while sleeping the thread", ex.getMessage());
                }
            }
        }
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
            LOGGER.info("Database connection closed!");
        }
    }

    @Override
    public PreparedStatementBinder createPreparedStatement(Connection connection, String query) throws SQLException {
        PreparedStatementBinder preparedStatementBinder = new PreparedStatementBinder(connection, query);
        preparedStatementBinder.setFetchSize(10);
        preparedStatementBinder.setFetchDirection(ResultSet.FETCH_FORWARD);
        return preparedStatementBinder;
    }

    @Override
    public Map<String, DBTableDef> tableDefinitions(List<String> tables) throws SQLException, NotImplementedException {
        Map<String, DBTableDef> tableDefinitions = new HashMap<>();

        String schema = customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.SCHEMA);

        try (Connection connection = getConnection()) {
            for (String table : tables) {
                List<DBColumnDef> dbColumnDefs = new ArrayList<>();
                try (ResultSet resultSet = connection.getMetaData().getColumns(
                        customJdbcSinkConfig.getString(CustomJDBCSinkConfig.DATABASE_PREFIX + CustomJDBCSinkConfig.CATALOG), schema, table, null)) {
                    while (resultSet.next()) {
                        String typeName = resultSet.getString("TYPE_NAME");
                        if (!supportedTypes.contains(typeName)) {
                            throw new NotImplementedException("Datatype '" + typeName + "' is not supported right now!");
                        }
                        DBColumnDef dbColumnDef = new JsonColumnDef();
                        dbColumnDef.setColumnName(resultSet.getString("COLUMN_NAME"));
                        dbColumnDef.setTypeName(typeName);
                        dbColumnDef.setDataType(resultSet.getInt("DATA_TYPE"));
                        dbColumnDef.setMaxLength(resultSet.getInt("COLUMN_SIZE"));
                        dbColumnDef.setNullable(resultSet.getBoolean("NULLABLE"));
                        dbColumnDef.setAutoIncrement("yes".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")));
                        dbColumnDefs.add(dbColumnDef);
                    }
                }
                DBTableDef dbTableDef = new DBTableDef();
                dbTableDef.setTableName(table.toLowerCase());
                dbTableDef.setSchemaName(schema.toLowerCase());
                dbTableDef.setDBColumnDefs(dbColumnDefs);

                tableDefinitions.put(table, dbTableDef);
            }
        } finally {
            close();
        }
        return tableDefinitions;
    }
}
