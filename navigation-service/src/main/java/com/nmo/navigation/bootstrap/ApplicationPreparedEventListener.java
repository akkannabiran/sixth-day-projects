package com.sixthday.navigation.bootstrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.config.Loggable;
import com.sixthday.navigation.exceptions.NavigationServiceException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Loggable
@Slf4j
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {
    private RestHighLevelClient esClient;
    private NavigationServiceConfig appConfig;

    @Autowired
    public ApplicationPreparedEventListener(RestHighLevelClient esClient, NavigationServiceConfig appConfig) {
        this.esClient = esClient;
        this.appConfig = appConfig;
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        String indexName = appConfig.getElasticSearchConfig().getIndexName();
        Resource indexFile = new ClassPathResource("category_index_elasticsearch_mapping.json");
        createIndex(indexName, indexFile);


    }

    private void createIndex(String indexName, Resource indexFile) {
            boolean indexExists;
            String action = "create";
            try {
                indexExists = esClient.indices().exists(new GetIndexRequest().indices(indexName), RequestOptions.DEFAULT);
                String mappingsAndSettings = getResourceAsString(indexFile);
                if (!indexExists) {
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                            .source(mappingsAndSettings, XContentType.JSON);
                    CreateIndexResponse createIndexResponse = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                    if (!createIndexResponse.isAcknowledged()) {
                        throw new NavigationServiceException("Creating index not acknowledged for indexName: " + indexName);
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
                        + "\" index: " + indexFile.getFilename(), ex);
            } catch (Exception ex) {
                log.error("Failed to " + action + " ElasticSearch index \"" + indexName
                        + "\" from file: " + indexFile.getFilename(), ex);
                throw ex;
            }
    }

    private static String getResourceAsString(Resource resource) {
        try {
            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found: " + resource.getFilename());
            }
            return Resources.toString(resource.getURL(), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Can not read resource file: " + resource.getFilename(), ex);
        }
    }

    private static String getMappingFromMappingsAndSettings(String mappingsAndSettings) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(mappingsAndSettings);
            return jsonNode.get("mappings").toString();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to process mappings and settings json file.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read mappings and settings json file.", e);
        }
    }
}
