package com.sixthday.kafka.connect.jdbc.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class PreparedStatementBinder implements AutoCloseable {
    private final Logger LOGGER = LoggerFactory.getLogger(PreparedStatementBinder.class);
    private final List<String> fields = new ArrayList<>();
    private PreparedStatement preparedStatement;

    public PreparedStatementBinder(Connection conn, String statementWithNames) throws SQLException {
        Pattern findParametersPattern = Pattern.compile("(?<!')(@[\\w]*)(?!')");
        Matcher matcher = findParametersPattern.matcher(statementWithNames);
        while (matcher.find()) {
            fields.add(matcher.group().substring(1));
        }
        preparedStatement = conn.prepareStatement(statementWithNames
                .replaceAll(findParametersPattern.pattern(), "?"));
    }

    public PreparedStatement getPreparedStatement() {
        return this.preparedStatement;
    }

    public void setFetchSize(int fetchSize) throws SQLException {
        preparedStatement.setFetchSize(fetchSize);
    }

    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    public void setFetchDirection(int fetchDirection) throws SQLException {
        preparedStatement.setFetchDirection(fetchDirection);
    }

    public int[] executeBatch() throws SQLException {
        return preparedStatement.executeBatch();
    }

    public void clearParameters() throws SQLException {
        preparedStatement.clearParameters();
    }

    public void close() {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
            preparedStatement = null;
        } catch (SQLException e) {
            LOGGER.error("Error caught while closing the prepared statement! {}", e.getMessage());
        }
    }

    public void bindInt(String name, Integer value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setInt(j + 1, value);
        }
    }

    public void bindString(String name, String value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setString(j + 1, value);
        }
    }


    public void bindDouble(String name, Double value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setDouble(j + 1, value);
        }
    }

    public void bindDigDecimal(String name, BigDecimal value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setBigDecimal(j + 1, value);
        }
    }

    public void bindFloat(String name, Float value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setFloat(j + 1, value);
        }
    }

    public void bindLong(String name, Long value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setLong(j + 1, value);
        }
    }

    public void bindDate(String name, Date value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setDate(j + 1, value);
        }
    }

    public void bindTime(String name, Time value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setTime(j + 1, value);
        }
    }

    public void bindTimestamp(String name, Timestamp value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setTimestamp(j + 1, value);
        }
    }

    public void bindBoolean(String name, Boolean value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setBoolean(j + 1, false);
            else
                preparedStatement.setBoolean(j + 1, value);
        }
    }

    public void bindObject(String name, Object value) throws SQLException {
        int[] index = getIndex(name);
        for (int j : index) {
            if (value == null)
                preparedStatement.setNull(j + 1, Types.NULL);
            else
                preparedStatement.setObject(j + 1, value);
        }
    }

    private int[] getIndex(String name) {
        return IntStream.range(0, fields.size()).filter(i -> fields.get(i).equals(name)).toArray();
    }
}