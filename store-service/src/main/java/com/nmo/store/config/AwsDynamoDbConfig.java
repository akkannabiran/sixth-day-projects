package com.sixthday.store.config;

import java.util.Objects;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.sixthday.store.exceptions.DynamoDBConfigurationException;

import lombok.Getter;
import lombok.Setter;

@Configuration
@EnableDynamoDBRepositories(basePackages = "com.sixthday.store.repository.dynamodb", dynamoDBMapperConfigRef = "dynamoDBMapperConfig")
@ConfigurationProperties("sixthday-dynamodb-config")
@Getter
@Setter
public class AwsDynamoDbConfig {
  
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String region;
  private String tableNamePrefix;
  private long readCapacityUnits;
  private long writeCapacityUnits;
  
  @Bean
  public AmazonDynamoDB amazonDynamoDB(AWSCredentials amazonAWSCredentials) {
    
    if (StringUtils.isEmpty(tableNamePrefix)) {
      throw new DynamoDBConfigurationException("DynamoDB Environment configuration is missing. This is mandatory setting for application to start.");
    }
    AmazonDynamoDB amazonDynamoDB = null;
    if (Objects.nonNull(endpoint) && Objects.nonNull(accessKey) && Objects.nonNull(secretKey)) {
      // Local installation of dyanamo db.
      AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(amazonAWSCredentials));
      builder.setEndpointConfiguration(new EndpointConfiguration(endpoint, Regions.DEFAULT_REGION.getName()));
      amazonDynamoDB = builder.build();
    } else {
      amazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
    }
    return amazonDynamoDB;
  }
  
  @Bean
  public AWSCredentials amazonAWSCredentials() {
    AWSCredentials credentials = null;
    if (Objects.nonNull(accessKey) && Objects.nonNull(secretKey)) {
      credentials = new BasicAWSCredentials(accessKey, secretKey);
    } else {
      credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    }
    return credentials;
  }
  
  @Bean
  public DynamoDBMapperConfig dynamoDBMapperConfig(DynamoDBMapperConfig.TableNameOverride tableNameOverrider) {
    DynamoDBMapperConfig.Builder builder = DynamoDBMapperConfig.builder();
    builder.setTableNameOverride(tableNameOverrider);
    
    // Sadly this is a @deprecated method but new DynamoDBMapperConfig.Builder() is incomplete compared to DynamoDBMapperConfig.DEFAULT
    return new DynamoDBMapperConfig(DynamoDBMapperConfig.DEFAULT, builder.build());
  }
  
  @Bean
  public DynamoDBMapperConfig.TableNameOverride tableNameOverrider() {
    return DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(getTableNamePrefix());
  }
  
}
