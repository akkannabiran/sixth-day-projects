package com.sixthday.navigation.api.services;

import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.LEFTNAV_REFRESHABLE_PATH;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.NAV_PATH;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.NAV_PATH_WITH_MULTIPLE_CATEGORIES;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getLeftNavDocumentWithDriveTo;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getLeftNavTreeWithDriveTo;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getTestLeftNavDocument;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getTestLeftNavTree;
import static com.sixthday.navigation.config.Constants.SOURCE_LEFT_NAV;
import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.elasticsearch.repository.LeftNavRepository;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.exceptions.HybridLeftNavTreeNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.api.mappers.LeftNavNodeMapper;
import com.sixthday.navigation.api.mappers.LeftNavTreeMapper;
import com.sixthday.navigation.api.models.LeftNavCategoryBuilder;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;

@RunWith(MockitoJUnitRunner.class)
public class HybridNavTreeServiceTest {

    @Mock
    LeftNavRepository leftNavRepository;

    @Mock
    LeftNavTreeMapper leftNavTreeMapper;

    @Mock
    CategoryRepository categoryRepository;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navConfig;

    @InjectMocks
    LeftNavTreeService leftNavTreeService;
    
    @Before
    public void setup() {
      Map<String, String> alternateDefaults = new HashMap<>();
      alternateDefaults.put("cat123", "altCat235");
      when(navConfig.getCategoryConfig().getAlternateDefaults()).thenReturn(alternateDefaults);
    }

    @Test
    public void shouldReturnLeftNavTreeForNavPathWithMultipleCategoriesIfFoundInRepository() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        when(leftNavRepository.getLeftNavDocument(NAV_PATH_WITH_MULTIPLE_CATEGORIES)).thenReturn(leftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(getTestLeftNavTree());

        LeftNavTree result = leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH_WITH_MULTIPLE_CATEGORIES, US_COUNTRY_CODE, SOURCE_LEFT_NAV, null);

        assertEquals("cat1", result.getId());
        assertEquals("name1", result.getName());
        assertEquals("cat1", result.getLeftNav().get(0).getId());
        assertEquals("testurl", result.getLeftNav().get(0).getUrl());
    }
    
    @Test
    public void shouldReturnTestLeftNavTreeForNavPathWithMultipleCategoriesIfFoundInRepositoryForNavKeyGroupB() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        leftNavDocument.setId("altCat235");leftNavDocument.setName("AlternateCategoryName");
        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("altCat235")
                .withName("AlternateCategoryName")
                .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("child1").build(),new LeftNavCategoryBuilder().withId("child2").build()))
                .build();
        LeftNavNodeMapper leftNavNodeMapper = new LeftNavNodeMapper();

        LeftNavTree expectedLeftNavTree = new LeftNavTree("altCat235", "AlternateCategoryName", Arrays.asList(leftNavNodeMapper.map(leftNavTreeNode)), Collections.emptyList(), LEFTNAV_REFRESHABLE_PATH);
        
        when(leftNavRepository.getLeftNavDocument("altCat235_cat2")).thenReturn(leftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(expectedLeftNavTree);

        LeftNavTree result = leftNavTreeService.getHybridLeftNavByNavPath("cat123_cat2", US_COUNTRY_CODE, SOURCE_LEFT_NAV, "B");

        assertEquals("altCat235", result.getId());
        assertEquals("AlternateCategoryName", result.getName());
        assertEquals("altCat235", result.getLeftNav().get(0).getId());
        assertEquals("testurl", result.getLeftNav().get(0).getUrl());
    }
    
    @Test
    public void shouldReturnLeftNavTreeForNavPathWithSingleCategoryIfFoundInRepository() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        CategoryDocument categoryDocument = mock(CategoryDocument.class);
        when(categoryDocument.getDefaultPath()).thenReturn("cat1_cat2");
        when(categoryRepository.getCategoryDocument(NAV_PATH)).thenReturn(categoryDocument);
        when(leftNavRepository.getLeftNavDocument("cat1_cat2")).thenReturn(leftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(getTestLeftNavTree());

        LeftNavTree result = leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_LEFT_NAV, "");

        assertEquals("cat1", result.getId());
        assertEquals("name1", result.getName());
        assertEquals("cat1", result.getLeftNav().get(0).getId());
        assertEquals("testurl", result.getLeftNav().get(0).getUrl());
    }

    @Test
    public void shouldReturnTestLeftNavTreeForNavPathWithSingleCategoryIfFoundInRepositoryForNavKeyGroupB() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        leftNavDocument.setId("altCat235");leftNavDocument.setName("AlternateCategoryName");
        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("altCat235")
                .withName("AlternateCategoryName")
                .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("child1").build(),new LeftNavCategoryBuilder().withId("child2").build()))
                .build();
        LeftNavNodeMapper leftNavNodeMapper = new LeftNavNodeMapper();
        LeftNavTree expectedLeftNavTree = new LeftNavTree("altCat235", "AlternateCategoryName", Arrays.asList(leftNavNodeMapper.map(leftNavTreeNode)), Collections.emptyList(), LEFTNAV_REFRESHABLE_PATH);
        CategoryDocument categoryDocument = mock(CategoryDocument.class);
        when(categoryDocument.getDefaultPath()).thenReturn("altCat235_cat2");
        when(categoryRepository.getCategoryDocument("cat123")).thenReturn(categoryDocument);
        when(leftNavRepository.getLeftNavDocument("altCat235_cat2")).thenReturn(leftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(expectedLeftNavTree);

        LeftNavTree result = leftNavTreeService.getHybridLeftNavByNavPath("cat123", US_COUNTRY_CODE, SOURCE_LEFT_NAV, "");

        assertEquals("altCat235", result.getId());
        assertEquals("AlternateCategoryName", result.getName());
        assertEquals("altCat235", result.getLeftNav().get(0).getId());
        assertEquals("testurl", result.getLeftNav().get(0).getUrl());
    }
    
    @Test(expected = HybridLeftNavTreeNotFoundException.class)
    public void shouldReturnEmptyResponseForNavPathWithMultipleCategoriesIfTheyAreNotFoundInRepository() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        when(leftNavRepository.getLeftNavDocument(NAV_PATH_WITH_MULTIPLE_CATEGORIES)).thenReturn(leftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(null);

        leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH_WITH_MULTIPLE_CATEGORIES, US_COUNTRY_CODE, SOURCE_LEFT_NAV, null);
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    public void shouldReturnEmptyResponseForNavPathWithSingleCategoryIfTheyAreNotFoundInRepository() {
        when(categoryRepository.getCategoryDocument(NAV_PATH)).thenThrow(new CategoryNotFoundException(NAV_PATH));

        leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_LEFT_NAV, null);
    }

    @Test
    public void shouldReturnDriveToLeftNavIfSourceIsNotLeftNavAndDocumentHasDriveTo() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        when(leftNavRepository.getLeftNavDocument(NAV_PATH_WITH_MULTIPLE_CATEGORIES)).thenReturn(leftNavDocument);
        LeftNavDocument driveToLeftNavDocument = getLeftNavDocumentWithDriveTo();
        when(leftNavRepository.getLeftNavDocument(leftNavDocument.getDriveToPath())).thenReturn(driveToLeftNavDocument);
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(driveToLeftNavDocument, US_COUNTRY_CODE)).thenReturn(getLeftNavTreeWithDriveTo());

        LeftNavTree tree = leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH_WITH_MULTIPLE_CATEGORIES, US_COUNTRY_CODE, SOURCE_TOP_NAV, null);

        assertEquals("cat1_child1", tree.getId());
        assertEquals("name1", tree.getName());
        assertEquals(2, tree.getLeftNav().get(0).getCategories().size());
        assertEquals("child1", tree.getLeftNav().get(0).getCategories().get(0).getId());
        assertEquals("child2", tree.getLeftNav().get(0).getCategories().get(1).getId());
    }

    @Test
    public void shouldReturnLeftNavIfSourceIsNotLeftNavAndDriveToIsEmpty() {
        LeftNavDocument leftNavDocument = getTestLeftNavDocument();
        when(leftNavRepository.getLeftNavDocument(NAV_PATH_WITH_MULTIPLE_CATEGORIES)).thenReturn(leftNavDocument);
        leftNavDocument.setDriveToPath("");
        when(leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE)).thenReturn(getTestLeftNavTree());

        LeftNavTree tree = leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH_WITH_MULTIPLE_CATEGORIES, US_COUNTRY_CODE, SOURCE_TOP_NAV, null);

        assertEquals("cat1", tree.getId());
        assertEquals("name1", tree.getName());
        assertEquals(2, tree.getLeftNav().get(0).getCategories().size());
        assertEquals("child1", tree.getLeftNav().get(0).getCategories().get(0).getId());
        assertEquals("child2", tree.getLeftNav().get(0).getCategories().get(1).getId());
    }
}
