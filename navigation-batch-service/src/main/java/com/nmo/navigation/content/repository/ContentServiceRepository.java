package com.sixthday.navigation.content.repository;

import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.repository.WebRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ContentServiceRepository extends WebRepository {
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    private RestTemplate restTemplate;

    @Autowired
    public ContentServiceRepository(final NavigationBatchServiceConfig navigationBatchServiceConfig, @Qualifier("ContentServiceRestTemplate") RestTemplate restTemplate) {
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
        this.restTemplate = restTemplate;
    }

    HttpEntity getHttpEntity(final HttpHeaders httpHeaders, final List<String> categoryIds) {
        httpHeaders.add("categoryIds", categoryIds.stream().collect(Collectors.joining(",")));
        return new HttpEntity<>(httpHeaders);
    }

    @Override
    protected NavigationBatchServiceConfig.ServiceConfig getServiceConfig() {
        return navigationBatchServiceConfig.getIntegration().getContentServiceConfig();
    }

    public Map<String, Object> getSiloDrawerAsset(final List<String> categoryIds) {
        Map<String, Object> categoryAssetMap = new HashMap<>();
        if (navigationBatchServiceConfig.getIntegration().getContentServiceConfig().isEnabled()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity httpEntity = getHttpEntity(headers, categoryIds);
            String url = buildGetUrl(new ArrayList<>()).toString();
            Optional<ArrayList> assetsFromAEM = Optional.of(restTemplate.exchange(url, HttpMethod.GET, httpEntity, ArrayList.class).getBody());

            assetsFromAEM.orElse(new ArrayList()).forEach(siloAsset -> {
                String categoryIdKey = ((LinkedHashMap<String, Object>) siloAsset).get("categoryId").toString();
                categoryAssetMap.put(categoryIdKey, siloAsset);
            });
        }
        return categoryAssetMap;
    }
}
