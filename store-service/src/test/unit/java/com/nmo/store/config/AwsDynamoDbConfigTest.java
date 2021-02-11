package com.sixthday.store.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwsDynamoDbConfigTest {

  @Mock
  private AWSCredentials credentials;

  @InjectMocks private AwsDynamoDbConfig config;

  @Before public void setup() {
    config.setTableNamePrefix("PREFIX_");
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowApplicationErrorWhenTableNamePrefixIsNotConfigured() {
    config.setTableNamePrefix(null);
    config.amazonDynamoDB(null);
  }

  @Test
  public void shouldCreateTableNameOverriderWithConfiguredPrefix() {
    DynamoDBMapperConfig.TableNameOverride tableNameOverride = config.tableNameOverrider();
    assertThat("Table name prefix should be set to TableNameOverride bean", tableNameOverride.getTableNamePrefix(), equalTo(config.getTableNamePrefix()));
  }

  @Test
  public void shouldStartDynamoDBConfigBeanWithTablePrefixConfigured() {
    DynamoDBMapperConfig dynamoDBMapperConfig = config.dynamoDBMapperConfig(config.tableNameOverrider());
    assertThat("TableNameOverride should be set to dynamoDBMapperConfig bean", dynamoDBMapperConfig.getTableNameOverride(), notNullValue());
    assertThat("DBMapper should have table name override with configured value", dynamoDBMapperConfig.getTableNameOverride().getTableNamePrefix(), equalTo("PREFIX_"));

  }
}
