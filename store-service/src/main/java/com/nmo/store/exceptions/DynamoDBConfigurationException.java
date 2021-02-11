package com.sixthday.store.exceptions;

public class DynamoDBConfigurationException extends RuntimeException {
  public DynamoDBConfigurationException(String error) {
    super(error);
  }

}
