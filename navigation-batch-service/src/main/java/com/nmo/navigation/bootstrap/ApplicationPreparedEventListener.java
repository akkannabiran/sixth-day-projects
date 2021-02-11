package com.sixthday.navigation.bootstrap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.config.AwsDynamoDbConfig;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {

    public static final String LEFT_NAV_MAPPING_FILE_NAME = "leftnav_index_elasticsearch_mapping.json";

    private RestHighLevelClient esClient;
    private NavigationBatchServiceConfig appConfig;
    private AwsDynamoDbConfig awsDynamoDbConfig;
    private AmazonDynamoDB amazonDynamoDB;
    private DynamoDBMapper dynamoDbMapper;

    @Autowired
    public ApplicationPreparedEventListener(RestHighLevelClient esClient, NavigationBatchServiceConfig appConfig,
                                            AwsDynamoDbConfig awsDynamoDbConfig,
                                            AmazonDynamoDB amazonDynamoDB,
                                            DynamoDBMapper dynamoDbMapper) {
        this.esClient = esClient;
        this.appConfig = appConfig;
        this.awsDynamoDbConfig = awsDynamoDbConfig;
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDbMapper = dynamoDbMapper;
    }

    private static String getStrFromResource(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new IOException("File not found: " + resource.getFilename());
        }
        return Resources.toString(resource.getURL(), Charsets.UTF_8);
    }

    private static String getMappingFromMappingsAndSettings(String mappingsAndSettings) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(mappingsAndSettings);
            return jsonNode.get("mappings").toString();
        } catch (JsonProcessingException e) {
            throw new NavigationBatchServiceException("Unable to process mappings and settings json file.", e);
        } catch (IOException e) {
            throw new NavigationBatchServiceException("Unable to read mappings and settings json file.", e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        String leftNavIndexName = appConfig.getElasticSearchConfig().getLeftNavIndex().getName();
        Resource leftNavIndexFile = new ClassPathResource(LEFT_NAV_MAPPING_FILE_NAME);
        createIndex(leftNavIndexName, leftNavIndexFile);
        createDesignerByIdTableIfNotExists();
    }

    private void createIndex(String indexName, Resource mappingsAndSettingsFile) {
        GetIndexRequest existsRequest = new GetIndexRequest().indices(indexName);
        boolean indexExists;
        try {
            indexExists = esClient.indices().exists(existsRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            log.error("Existence checking is failed for index " + indexName, ex);
            throw new NavigationBatchServiceException("Existence checking is failed for index " + indexName);
        }

        String action = "create";
        try {
            String mappingsAndSettings = getStrFromResource(mappingsAndSettingsFile);
            if (!indexExists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                        .source(mappingsAndSettings, XContentType.JSON);
                CreateIndexResponse createIndexResponse = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                if (!createIndexResponse.isAcknowledged()) {
                    throw new NavigationBatchServiceException("Creating index not acknowledged for indexName: " + indexName);
                }
            } else {
                action = "update";
                String mappings = getMappingFromMappingsAndSettings(mappingsAndSettings);
                PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName).type("_doc").source(mappings, XContentType.JSON);
                esClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
            }
        } catch (ResourceAlreadyExistsException ex) {
            log.error("ElasticSearch Index \"{}\" already exists", indexName, ex);
        } catch (IOException ex) {
            log.error("Unable to read file with mappings and settings for \"" + indexName
                    + "\" index: " + mappingsAndSettingsFile.getFilename(), ex);
        } catch (Exception ex) {
            log.error("Failed to " + action + " ElasticSearch index \"" + indexName
                    + "\" from file: " + mappingsAndSettingsFile.getFilename(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    private void createDesignerByIdTableIfNotExists() {
        CreateTableRequest createTableRequest = dynamoDbMapper.generateCreateTableRequest(DesignerIndex.class)
            .withProvisionedThroughput(new ProvisionedThroughput(awsDynamoDbConfig.getReadCapacityUnits(), awsDynamoDbConfig.getWriteCapacityUnits()));
        String tableName = createTableRequest.getTableName();
        boolean isCreated = TableUtils.createTableIfNotExists(amazonDynamoDB, createTableRequest);
        if (isCreated) {
            TableUtils.waitUntilActive(amazonDynamoDB, tableName);
        }
    }
}
