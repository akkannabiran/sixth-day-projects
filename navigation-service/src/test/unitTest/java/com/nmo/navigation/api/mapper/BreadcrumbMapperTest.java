package com.sixthday.navigation.api.mapper;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.mappers.BreadcrumbMapper;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.utils.BreadcrumbUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.getEmptyTestCategoryDocuments;
import static com.sixthday.navigation.api.data.CategoryTestDataFactory.getTestCategoryDocuments;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BreadcrumbMapperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    private BreadcrumbUtil breadcrumbUtil;

    private BreadcrumbMapper breadcrumbMapper;

    @Before
    public void setup() {
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getLive()).thenReturn("cat000000");
        breadcrumbUtil = new BreadcrumbUtil(navigationServiceConfig);
        breadcrumbMapper = new BreadcrumbMapper(breadcrumbUtil);
    }

    @Test
    public void shouldReturnListOfBreadcrumbsWithMobileAlternateNameForChildCategoryOfRoot() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getTestCategoryDocuments(), true);
        assertEquals("mobileAlternateName", outputBreadcrumbs.get(0).getNameForMobile());
    }

    @Test
    public void shouldReturnListOfBreadcrumbsWithDesktopAlternateNameForChildCategoryOfRoot() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getTestCategoryDocuments(), true);
        assertEquals("desktopAlternateName", outputBreadcrumbs.get(0).getName());
    }

    @Test
    public void shouldReturnListOfBreadcrumbsWhenListOfCategoriesPassedInParameter() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getTestCategoryDocuments(), true);
        assertEquals("idCat1", outputBreadcrumbs.get(0).getId());
        assertEquals("/c/nameCat1-idCat1?navpath=cat000000_idCat1&source=leftNav", outputBreadcrumbs.get(0).getUrl());
        assertEquals("idCat2", outputBreadcrumbs.get(1).getId());
        assertEquals("nameCat2", outputBreadcrumbs.get(1).getName());
        assertEquals("/c/nameCat1-idCat1?navpath=cat000000_idCat1_idCat2&source=leftNav", outputBreadcrumbs.get(1).getUrl());
    }

    @Test
    public void shouldReturnListOfBreadcrumbsWithMobileAlternateNameWhenPresent() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getTestCategoryDocuments(), true);
        assertEquals("MobileAlternateName", outputBreadcrumbs.get(3).getNameForMobile());
    }

    @Test
    public void shouldReturnListOfBreadcrumbsWithDesktopAlternateNameWhenPresent() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getTestCategoryDocuments(), true);
        assertEquals("DesktopAlternateName", outputBreadcrumbs.get(3).getName());
    }

    @Test
    public void shouldReturnEmptyResponseWhenEmptyCategoriesPassedInParameter() {
        List<Breadcrumb> outputBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(getEmptyTestCategoryDocuments(), true);
        assertThat(outputBreadcrumbs.size(), is(0));
    }

    @Test
    public void shouldBuildBreadcrumbUrlWithoutLiveRootCatIdWhenHasLiveRootCatIdInNavpathIsFalse() {
        CategoryDocument catDoc1 = new CategoryDocumentBuilder().withId("id1").withName("name1").build();
        CategoryDocument catDoc2 = new CategoryDocumentBuilder().withId("id2").withName("name2").build();
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        categoryDocuments.add(catDoc1);
        categoryDocuments.add(catDoc2);
        Breadcrumb expectedFirstBreadcrumb = new Breadcrumb(catDoc1.getId(), "", "", "/c/nameCat1-idCat1?navpath=id1&source=leftNav");
        Breadcrumb expectedSecondBreadcrumb = new Breadcrumb(catDoc1.getId(), "", "", "/c/nameCat1-idCat1?navpath=id1_id2&source=leftNav");

        List<Breadcrumb> actualBreadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocuments, false);

        assertThat(actualBreadcrumbs.get(0).getUrl(), is(expectedFirstBreadcrumb.getUrl()));
        assertThat(actualBreadcrumbs.get(1).getUrl(), is(expectedSecondBreadcrumb.getUrl()));
    }
}
