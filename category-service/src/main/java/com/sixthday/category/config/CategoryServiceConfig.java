package com.sixthday.category.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "category-service")
@Getter
@Setter
public class CategoryServiceConfig {
    private String imageServerUrl;
    private String defaultProductImageSrc;
    private int thresholdLimitValue;
    private List<CategoryTemplate> categoryTemplates;
    private String headerAssetUrl;
    private List<FilterOption> filterOptions = new ArrayList<>();
    private List<String> reducedChildCountSilos;
    private List<String> newAspectRatioCategoryIdList = new ArrayList<>();
    private String seoContentTitle;

    @Getter
    @Setter
    public static class CategoryTemplate {
        private String name;
        private String key;
    }

    @Getter
    @Setter
    public static class FilterOption {
        private String filterKey;
        private String displayText;
    }
}
