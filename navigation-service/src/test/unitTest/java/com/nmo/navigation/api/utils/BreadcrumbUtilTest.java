package com.sixthday.navigation.api.utils;

import com.google.common.collect.Iterables;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.data.CategoryTestDataFactory;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.CATEGORY_IDS_WITH_ROOTID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BreadcrumbUtilTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    @InjectMocks
    private BreadcrumbUtil breadcrumbUtil;

    @Before
    public void setup() {
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getLive()).thenReturn("cat000000");
    }

    @Test
    public void shouldRemoveAllSpecialCharactersFromTheDisplayNameAndReturnDisplayName() {
        assertThat(breadcrumbUtil.replaceSpecialCharacters("All Accessories"), equalTo("All-Accessories"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Scarves & Wraps"), equalTo("Scarves-Wraps"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Capes, Ponchos & Vests"), equalTo("Capes-Ponchos-Vests"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Alice + Olivia"), equalTo("Alice-Olivia"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Lafayette 148 New York"), equalTo("Lafayette-148-New-York"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("CUSP/Contemporary"), equalTo("CUSP-Contemporary"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Ken's Picks"), equalTo("Kens-Picks"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("#OnTheCusp"), equalTo("OnTheCusp"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Head-Turning Earrings"), equalTo("Head-Turning-Earrings"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("#NMBeauty Bestsellers"), equalTo("NMBeauty-Bestsellers"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Cl&eacute; De Peau Beaut&eacute;"), equalTo("Cleacute-De-Peau-Beauteacute"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Sizes 7-16"), equalTo("Sizes-7-16"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("25% off Home Sale"), equalTo("25-off-Home-Sale"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("$100 and Under"), equalTo("$100-and-Under"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Indulge!"), equalTo("Indulge"));
        assertThat(breadcrumbUtil.replaceSpecialCharacters("Indulge!"), equalTo("Indulge"));
    }

    @Test
    public void shouldReturnDisplaynameAsNullWhenNullDisplaynamePassed() {
        assertThat(breadcrumbUtil.replaceSpecialCharacters(null), equalTo(null));
    }

    @Test
    public void shouldRemoveCat000000AndReturnsListOfIds() {
        Iterable<String> ids = breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID, null);
        List<String> convertedList = Arrays.asList("cat1_cat2".split("_"));
        assertThat(Iterables.size(ids), is(2));
        assertThat(ids, is(convertedList));
    }

    @Test
    public void shouldReturnBreadcrumbUrlWithNavpathWhenCategoryDocumentsAvailable() {
        final String url1 = breadcrumbUtil.buildBreadcrumbUrl(CategoryTestDataFactory.getTestCategoryDocuments(), 1, true);
        final String url2 = breadcrumbUtil.buildBreadcrumbUrl(CategoryTestDataFactory.getTestCategoryDocuments(), 2, true);
        assertThat(url1, equalTo("/c/nameCat1-idCat1?navpath=cat000000_idCat1"));
        assertThat(url2, equalTo("/c/nameCat1-idCat1?navpath=cat000000_idCat1_idCat2"));
    }

    @Test
    public void shouldReturnEmptyBreadcrumbUrlWhenCategoryDocumentsNotAvailable() {
        final String url1 = breadcrumbUtil.buildBreadcrumbUrl(CategoryTestDataFactory.getEmptyTestCategoryDocuments(), 0, true);
        assertThat(url1, equalTo(""));
    }

    @Test
    public void shouldNotPrependLiveRootCatIdToBreadcrumbUrlsWhenHasLiveRootCatIdInNavPathIsFalse() {
        final String expectedUrl = "/c/nameCat1-idCat1?navpath=id1";
        CategoryDocument catDocTest = new CategoryDocumentBuilder()
                .withId("id1")
                .withName("name1")
                .build();
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        categoryDocuments.add(catDocTest);

        final String url1 = breadcrumbUtil.buildBreadcrumbUrl(categoryDocuments, 1, false);

        assertThat(url1, is(expectedUrl));
    }

    @Test
    public void shouldReturnBreadcrumbUrlWithSourceLeftNavParamAppendedToNavpath() {
        String expectedUrl = "/name1/id1/c.cat?navpath=id1&source=leftNav";
        String urlWithoutSourceParam = "/name1/id1/c.cat?navpath=id1";
        String actualUrl = breadcrumbUtil.appendSourceParam(urlWithoutSourceParam);

        assertThat(actualUrl, is(expectedUrl));
    }

    @Test
    public void shouldReturnBreadcrumbUrlWithSourceLeftNavParamAppendedWithoutNavpathAndQueryChar() {
        String expectedUrl = "/name1/id1/c.cat?source=leftNav";
        String urlWithoutQueryParam = "/name1/id1/c.cat";
        String actualUrl = breadcrumbUtil.appendSourceParam(urlWithoutQueryParam);

        assertThat(actualUrl, is(expectedUrl));
    }

    @Test
    public void shouldReturnBreadcrumbUrlWithSourceLeftNavParamAppendedWhenOnlyGivenQueryChar() {
        String expectedUrl = "/name1/id1/c.cat?source=leftNav";
        String urlWithQueryParamWithoutNavpath = "/name1/id1/c.cat?";
        String actualUrl = breadcrumbUtil.appendSourceParam(urlWithQueryParamWithoutNavpath);

        assertThat(actualUrl, is(expectedUrl));
    }
}
