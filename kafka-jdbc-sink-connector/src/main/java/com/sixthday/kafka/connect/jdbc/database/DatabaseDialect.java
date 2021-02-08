package com.sixthday.kafka.connect.jdbc.database;

import com.sixthday.kafka.connect.jdbc.exception.NotImplementedException;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseDialect extends AutoCloseable {
    Connection getConnection() throws SQLException;

    PreparedStatementBinder createPreparedStatement(Connection connection, String query) throws SQLException;

    Map<String, DBTableDef> tableDefinitions(List<String> tables) throws SQLException, ConnectException, NotImplementedException;
}
