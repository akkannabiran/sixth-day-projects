package com.sixthday.navigation.api.services;

import static com.sixthday.navigation.config.Constants.SEPARATOR;
import static com.sixthday.navigation.config.Constants.SOURCE_LEFT_NAV;
import static com.sixthday.navigation.config.Constants.TEST_CATEGORY_GROUP;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.elasticsearch.repository.LeftNavRepository;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.exceptions.HybridLeftNavTreeNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.api.mappers.LeftNavTreeMapper;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;

@Service
public class LeftNavTreeService {
    private LeftNavRepository leftNavRepository;
    private LeftNavTreeMapper leftNavTreeMapper;
    private CategoryRepository categoryRepository;
    private NavigationServiceConfig navigationServiceConfig;

    @Autowired
    public LeftNavTreeService(final LeftNavRepository leftNavRepository, final LeftNavTreeMapper leftNavTreeMapper, final CategoryRepository categoryRepository, final NavigationServiceConfig navigationServiceConfig) {
        this.leftNavRepository = leftNavRepository;
        this.leftNavTreeMapper = leftNavTreeMapper;
        this.categoryRepository = categoryRepository;
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public LeftNavTree getLeftNavTreeByNavPath(@NotNull String navPath, @NotNull String countryCode, @NotNull String source, String navKeyGroup) {
        String newNavPath = navPath.contains(SEPARATOR) ? navPath : getNavPath(navPath);
        newNavPath =  prepareForAlternateDefaultNavPath(newNavPath, navKeyGroup);
        LeftNavDocument leftNavDocument = fetchLeftNavDocument(source, newNavPath);
        LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToWebLeftNav(leftNavDocument, countryCode);
        if (leftNavTree == null) {
            throw new LeftNavTreeNotFoundException(newNavPath);
        }
        return leftNavTree;
    }

    private LeftNavDocument fetchLeftNavDocument(String source, String navPath) {
      LeftNavDocument leftNavDocument = leftNavRepository.getLeftNavDocument(navPath);
      if (shouldFetchDriveToLeftNav(leftNavDocument, source)) {
          leftNavDocument = leftNavRepository.getLeftNavDocument(leftNavDocument.getDriveToPath());
      }
      return leftNavDocument;
    }

    public LeftNavTree getHybridLeftNavByNavPath(@NotNull String navPath, @NotNull String countryCode, @NotNull String source, String navKeyGroup) {
      String newNavPath = navPath.contains(SEPARATOR) ? navPath : getNavPath(navPath);
      newNavPath =  prepareForAlternateDefaultNavPath(newNavPath, navKeyGroup);
      LeftNavDocument leftNavDocument = fetchLeftNavDocument(source, newNavPath);
      LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, countryCode);
      if (leftNavTree == null) {
          throw new HybridLeftNavTreeNotFoundException(newNavPath);
      }
      return leftNavTree;
    }
    
    private boolean shouldFetchDriveToLeftNav(LeftNavDocument leftNavDocument, String source) {
        return isNotBlank(leftNavDocument.getDriveToPath()) && !SOURCE_LEFT_NAV.equals(source);
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
    
    private String prepareForAlternateDefaultNavPath(String currentNavPath, final String navKeyGroup) {
      if (TEST_CATEGORY_GROUP.equals(navKeyGroup)) {
        return Stream.of(currentNavPath.split("_")).map(this::getAlternateForDefault).collect(Collectors.joining(SEPARATOR));
      } else {
        return currentNavPath;
      }
    }
    
    public String getAlternateForDefault(String categoryId) {
      return navigationServiceConfig.getCategoryConfig().getAlternateDefaults().getOrDefault(categoryId, categoryId);
    }
}

