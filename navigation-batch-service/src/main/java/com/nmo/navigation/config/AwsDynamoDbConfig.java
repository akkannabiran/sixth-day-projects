package com.sixthday.navigation.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverterFactory;
import com.sixthday.navigation.exceptions.DynamoDBConfigurationException;

import lombok.Getter;
import lombok.Setter;

@Configuration
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
  public DynamoDBMapper dynamoDbMapper(@Autowired AmazonDynamoDB amazonDynamoDB) {
    return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig());
  }
  
  private DynamoDBMapperConfig dynamoDBMapperConfig() {
    return new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(tableNamePrefix))
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.CLOBBER).withTypeConverterFactory(DynamoDBTypeConverterFactory.standard()).build();
  }
  
}
