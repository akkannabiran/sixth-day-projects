package com.sixthday.navigation.exceptions;

public class DynamoDBConfigurationException extends RuntimeException {
    public DynamoDBConfigurationException(String error) {
        super(error);
    }

}
