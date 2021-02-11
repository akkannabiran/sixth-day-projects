package com.sixthday.navigation.api.elasticsearch.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.config.Constants;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.TimeZone;

import static com.sixthday.navigation.config.Constants.SEPARATOR;

@Repository
@Slf4j
public class ESLeftNavRepository {

    private NavigationServiceConfig navigationServiceConfig;
    private ObjectMapper objectMapper;
    private RestHighLevelClient restHighLevelClient;
    private CategoryRepository categoryRepository;

    @Autowired
    public ESLeftNavRepository(final RestHighLevelClient restHighLevelClient, final NavigationServiceConfig navigationServiceConfig, final CategoryRepository categoryRepository) {
        this.restHighLevelClient = restHighLevelClient;
        this.navigationServiceConfig = navigationServiceConfig;
        this.categoryRepository = categoryRepository;
        this.objectMapper = new ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).setTimeZone(TimeZone.getTimeZone("America/Chicago"));
    }

    @PreDestroy
    @SneakyThrows
    public void closeClient() {
        restHighLevelClient.close();
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.GET_LEFTNAV_DOCUMENT_FROM_ES)
    @SneakyThrows
    public LeftNavDocument getLeftNavDocument(final String navPath) {

        GetResponse getResponse = restHighLevelClient.get(buildSearchRequest(navPath),RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            return processSearchResponseForNavPath(getResponse);
        } else if (navPath.contains(SEPARATOR)) {
            String currentCategoryId = navPath.substring( navPath.lastIndexOf( SEPARATOR) + 1);
            final String defaultPath = getNavPath(currentCategoryId);
            getResponse = restHighLevelClient.get(buildSearchRequest(defaultPath),RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
                return processSearchResponseForNavPath(getResponse);
            }
        }
        throw new LeftNavTreeNotFoundException(navPath);
    }

    private String getNavPath(String navPath) {
        CategoryDocument categoryDocument;
        try {
            categoryDocument = categoryRepository.getCategoryDocument(navPath);
        } catch (CategoryNotFoundException e) {
            throw new LeftNavTreeNotFoundException(navPath, e);
        }
        return categoryDocument.getDefaultPath();
    }

    @SneakyThrows
    private GetRequest buildSearchRequest(final String navPath) {
        return new GetRequest(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName(),
                navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6(),
                navPath);
    }

    private LeftNavDocument processSearchResponseForNavPath(final GetResponse getResponse) {
        try {
            return objectMapper.readValue(getResponse.getSourceAsString(), LeftNavDocument.class);
        } catch (IOException e) {
            log.error(Constants.REPOSITORY + Constants.PARSE_LEFTNAV_DOCUMENT + "\" Error parsing document" + getResponse.getId(), e);
            throw new LeftNavTreeNotFoundException(getResponse.getId());
        }
    }
}
