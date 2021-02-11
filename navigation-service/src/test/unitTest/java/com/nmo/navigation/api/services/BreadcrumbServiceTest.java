package com.sixthday.navigation.api.services;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.mappers.BreadcrumbMapper;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.utils.BreadcrumbUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.*;
import static com.sixthday.navigation.config.Constants.SOURCE_LEFT_NAV;
import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class BreadcrumbServiceTest {
    @Rule 
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    BreadcrumbUtil breadcrumbUtil;

    @Mock
    BreadcrumbMapper breadcrumbMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    NavigationServiceConfig navigationServiceConfig;

    @InjectMocks
    BreadcrumbService breadcrumbService;
    
    private TestInput testInput;
    
    @AllArgsConstructor
    @ToString
    @Getter
    private static class TestInput {
      private String navKeyGroup;
    }
    
    @Parameters(name="TestData= {0}")
    public static Collection<?> testParameters() {
      return Arrays.asList(new Object[] {
        new TestInput(null),
        new TestInput("A"),
        new TestInput("B")
      });
    }
    
    public BreadcrumbServiceTest(TestInput testInput) {
      this.testInput = testInput;
    }
    
    @Before
    public void setup() {
        
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getLive()).thenReturn(rootCat);
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getStage()).thenReturn(stageCat);
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getMarketing()).thenReturn(marketingCat);
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner()).thenReturn(CATEGORY_DESIGNER);
    }

    @Test
    public void shouldReturnListOfBreadcrumbsIfThereAreMatchingCategoryFoundInRepository() {
        List<CategoryDocument> categoryDocuments = getTestCategoryDocuments();
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS, testInput.getNavKeyGroup())).thenReturn(CATEGORY_ID_LIST);
        when(categoryRepository.getCategoryDocuments(CATEGORY_ID_LIST)).thenReturn(categoryDocuments);
        when(categoryRepository.getCategoryDocument(CATEGORY_ID_LIST.get(CATEGORY_ID_LIST.size() - 1))).thenReturn(getTestCategoryDocument("idCat2"));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocuments, false)).thenReturn(getTestBreadcrumbs());

        List<Breadcrumb> outputBreadcrumbs = breadcrumbService.getBreadcrumbs(CATEGORY_IDS, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        assertThat(outputBreadcrumbs.size(), is(2));
        assertEquals(outputBreadcrumbs.get(0).getId(), "idCat1");
        assertEquals(outputBreadcrumbs.get(0).getName(), "nameCat1");
        assertEquals(outputBreadcrumbs.get(0).getUrl(), "/nameCat1/idCat1/c.cat");
        assertEquals(outputBreadcrumbs.get(1).getId(), "idCat2");
        assertEquals(outputBreadcrumbs.get(1).getName(), "nameCat2");
        assertEquals(outputBreadcrumbs.get(1).getUrl(), "/nameCat1/nameCat2/idCat2_idCat1/c.cat");
    }

    @Test
    public void shouldReturnDefaultBreadcrumbsIfThereAreMatchingCategoryFoundInRepository() {
        CategoryDocument categoryDocument = getTestCategoryDocument(CATEGORY_ID);
        categoryDocument.setDefaultPath(DEFAULT_BREADCRUMB);
        List<CategoryDocument> categoryDocuments = getTestCategoryDocuments();
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS, testInput.getNavKeyGroup())).thenReturn(CATEGORY_ID_LIST);
        when(breadcrumbUtil.getAlternateForDefault(CATEGORY_ID)).thenReturn(CATEGORY_ID);
        when(categoryRepository.getCategoryDocument(CATEGORY_ID)).thenReturn(categoryDocument);
        when(categoryRepository.getCategoryDocuments(CATEGORY_ID_LIST)).thenReturn(categoryDocuments);
        when(categoryRepository.getCategoryDocument(CATEGORY_ID_LIST.get(CATEGORY_ID_LIST.size() - 1))).thenReturn(getTestCategoryDocument("idCat2"));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocuments, false)).thenReturn(getDefaultTestBreadcrumbs());
        List<Breadcrumb> outputBreadcrumbs = breadcrumbService.getBreadcrumbs(CATEGORY_ID, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        assertThat(outputBreadcrumbs.size(), is(2));
        assertEquals(outputBreadcrumbs.get(0).getId(), "idCat1");
        assertEquals(outputBreadcrumbs.get(0).getName(), "nameCat1");
        assertEquals(outputBreadcrumbs.get(0).getUrl(), "/nameCat1/idCat1/c.cat?navpath=idCat0000_idCat1");
        assertEquals(outputBreadcrumbs.get(1).getId(), "idCat2");
        assertEquals(outputBreadcrumbs.get(1).getName(), "nameCat2");
        assertEquals(outputBreadcrumbs.get(1).getUrl(), "/nameCat1/nameCat2/idCat2_idCat1/c.cat?navpath=idCat0000_idCat1_idCat2");
    }

    @Test
    public void shouldReturnAlternateDefaultBreadcrumbsIfThereAreMatchingCategoryFoundInRepositoryAndNavKeyGroupIsB() {
      if ("B".equals(testInput.getNavKeyGroup()) ) {
        CategoryDocument categoryDocument = getTestCategoryDocument("AlternateDefaultCatId");
        categoryDocument.setDefaultPath("cat2_AlternateDefaultCatId");
        List<String> categoryList = asList("AlternateDefaultCatId", "cat2");
        List<CategoryDocument> categoryDocuments = getTestCategoryDocuments();
        when(breadcrumbUtil.convertToListAndRemoveRootCategory("cat2_AlternateDefaultCatId", testInput.getNavKeyGroup())).thenReturn(categoryList);
        when(breadcrumbUtil.getAlternateForDefault(CATEGORY_ID)).thenReturn("AlternateDefaultCatId");
        when(categoryRepository.getCategoryDocument("AlternateDefaultCatId")).thenReturn(categoryDocument);
        when(categoryRepository.getCategoryDocuments(categoryList)).thenReturn(categoryDocuments);
        when(categoryRepository.getCategoryDocument(categoryList.get(categoryList.size() - 1))).thenReturn(getTestCategoryDocument("idCat2"));
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("AlternateDefaultCatId", "AlternateCategoryName", "", "/nameCat1/idCat1/c.cat?navpath=idCat0000_idCat1"));
        breadcrumbs.add(new Breadcrumb("idCat2", "nameCat2", "", "/nameCat1/nameCat2/idCat2_idCat1/c.cat?navpath=idCat0000_idCat1_idCat2"));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocuments, false)).thenReturn(breadcrumbs);
        
        List<Breadcrumb> outputBreadcrumbs = breadcrumbService.getBreadcrumbs(CATEGORY_ID, SOURCE_LEFT_NAV, "B");

        assertThat(outputBreadcrumbs.size(), is(2));
        assertEquals(outputBreadcrumbs.get(0).getId(), "AlternateDefaultCatId");
        assertEquals(outputBreadcrumbs.get(0).getName(), "AlternateCategoryName");
        assertEquals(outputBreadcrumbs.get(0).getUrl(), "/nameCat1/idCat1/c.cat?navpath=idCat0000_idCat1");
        assertEquals(outputBreadcrumbs.get(1).getId(), "idCat2");
        assertEquals(outputBreadcrumbs.get(1).getName(), "nameCat2");
        assertEquals(outputBreadcrumbs.get(1).getUrl(), "/nameCat1/nameCat2/idCat2_idCat1/c.cat?navpath=idCat0000_idCat1_idCat2");
      } else {
        //Do nothing. This test case is only intended for TestGroup
      }
    }
    
    @Test
    public void shouldReturnEmptyResponseIfThereAreNoMatchingCategoryFoundInRepository() {
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS, testInput.getNavKeyGroup())).thenReturn(CATEGORY_ID_LIST);
        when(categoryRepository.getCategoryDocuments(CATEGORY_ID_LIST)).thenReturn(getTestCategoryDocuments());
        when(categoryRepository.getCategoryDocument(CATEGORY_ID_LIST.get(CATEGORY_ID_LIST.size() - 1))).thenReturn(getTestCategoryDocument("idCat2"));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(false))).thenReturn(getInvalidTestBreadcrumbs());

        assertFalse(breadcrumbService.getBreadcrumbs(CATEGORY_IDS, SOURCE_LEFT_NAV, testInput.getNavKeyGroup()).isEmpty());
    }

    // Boutique Selected
    @Test
    public void shouldReturnBreadcrumbsForBoutiqueCategoryThatIsSelected() {
        List<Breadcrumb> breadcrumbListFromBoutiqueCategory = getBreadcrumbListFromSelectedBoutiqueCategory();
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED, testInput.getNavKeyGroup())).thenReturn(CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED);
        when(categoryRepository.getCategoryDocuments(CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED)).thenReturn(getCategoryDocumentListWithSelectedAsBoutiqueTrue());
        when(categoryRepository.getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner())).thenReturn(getCategoryDocumentThatIsReplacing());
        when(categoryRepository.getCategoryDocument(CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED.get(CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED.size() - 1))).thenReturn(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrue());
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(true))).thenReturn(breadcrumbListFromBoutiqueCategory);

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb("designerCat", "Designers", "", "designerCat/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catSelected, "Apparel", "", "designerCat/Gucci/Apparel/c.cat"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
    }

    @Test
    public void shouldReturnDesignerSpecificBreadcrumbsForFeaturedDesigners() {
        List<CategoryDocument> featuredDesignerCategoryDocuments = getTestFeaturedDesignerDocuments();
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(DESIGNER_CATEGORY_IDS, testInput.getNavKeyGroup())).thenReturn(ITERABLE_DESIGNER_CATEGORY_IDS);
        when(categoryRepository.getCategoryDocuments(ITERABLE_DESIGNER_CATEGORY_IDS)).thenReturn(featuredDesignerCategoryDocuments);

        CategoryDocument designerCategoryDocument = getDesignerCategoryDocument();
        when(categoryRepository.getCategoryDocument(CATEGORY_DESIGNER)).thenReturn(designerCategoryDocument);
        when(categoryRepository.getCategoryDocument("cat02")).thenReturn(featuredDesignerCategoryDocuments.get(0));
        when(categoryRepository.getCategoryDocument("cat03")).thenReturn(featuredDesignerCategoryDocuments.get(1));
        when(categoryRepository.getCategoryDocument("cat01")).thenReturn(featuredDesignerCategoryDocuments.get(2));
        List<CategoryDocument> categoryDocumentList = asList(designerCategoryDocument, featuredDesignerCategoryDocuments.get(2), featuredDesignerCategoryDocuments.get(0));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocumentList, false)).thenReturn(getTestFeaturedBreadcrumbs());

        List<Breadcrumb> outputBreadcrumbs = breadcrumbService.getBreadcrumbs(DESIGNER_CATEGORY_IDS, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        assertThat(outputBreadcrumbs.size(), is(3));
        assertEquals(outputBreadcrumbs.get(0).getId(), CATEGORY_DESIGNER);
        assertEquals(outputBreadcrumbs.get(0).getName(), "Designer");
        assertEquals(outputBreadcrumbs.get(1).getId(), "cat01");
        assertEquals(outputBreadcrumbs.get(1).getName(), "desktopAlternateName");
        assertEquals(outputBreadcrumbs.get(2).getId(), "cat02");
        assertEquals(outputBreadcrumbs.get(2).getName(), "shoes");
    }

    // Drive-To
    @Test
    public void shouldReturnBreadcrumbsWithDriveToSubCatContextProps() {
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT, testInput.getNavKeyGroup())).thenReturn(CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT);
        when(categoryRepository.getCategoryDocuments(CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT)).thenReturn(getCategoryDocumentListWithDriveToSubCatContextProp());
        when(categoryRepository.getCategoryDocument(CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT.get(CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT.size() - 1))).thenReturn(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        when(categoryRepository.getCategoryDocument(catToDriveTo)).thenReturn(getCategoryDocumentDriveTo());
        when(categoryRepository.getCategoryDocument(catToDriveToSecond)).thenReturn(getCategoryDocumentDriveToSecond());
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(true))).thenReturn(getBreadcrumbListFromDriveToSubCatContextProp());

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT, SOURCE_TOP_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb(topNavCat, "Women's Apparel", "", "topNavCat/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catCouldBeReplaced, "Featured Designers", "", "topNavCat/catThatCouldBeReplaced/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catSelected, "Gucci", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb("catToDriveTo", "Apparel", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/catToDriveTo/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb("catToDriveToSecond", "Women's", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/catToDriveTo/catToDriveToSecond/c.cat"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
        assertEquals(expectedBreadcrumbList.get(2).getId(), actualBreadcrumbList.get(2).getId());
        assertEquals(expectedBreadcrumbList.get(3).getId(), actualBreadcrumbList.get(3).getId());
        assertEquals(expectedBreadcrumbList.get(4).getId(), actualBreadcrumbList.get(4).getId());
        assertEquals(expectedBreadcrumbList.get(5).getId(), actualBreadcrumbList.get(5).getId());
    }

    // Normal Breadcrumb Behavior
    @Test
    public void shouldReturnBreadcrumbsWithoutRootCategoryGivenCategoryIdsWithRootCategory() {
        String rootCatId = "cat000000";
        String categoryIds = CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT;
        List<String> categoryIdList = CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT;
        List<CategoryDocument> categoryDocumentList = getCategoryDocumentsList();
        List<Breadcrumb> breadcrumbList = getBreadcrumbList();

        when(breadcrumbUtil.convertToListAndRemoveRootCategory(anyString(), anyString())).thenReturn(categoryIdList);
        when(categoryRepository.getCategoryDocuments(categoryIdList)).thenReturn(categoryDocumentList);
        when(categoryRepository.getCategoryDocument(categoryIdList.get(categoryIdList.size() - 1))).thenReturn(getTestCategoryDocument("catThatWasSelectedByUser"));
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocumentList, true)).thenReturn(breadcrumbList);

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(categoryIds, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        assertFalse(actualBreadcrumbList.stream()
                .filter(breadcrumb -> breadcrumb.getId() == rootCatId)
                .findAny()
                .isPresent()
        );
    }

    // Boutique Selected w/ Drive-To
    @Test
    public void shouldReturnBreadcrumbsForBoutiqueCategoryThatIsSelectedWithDriveToSubCat() {
        List<String> categoryIdList = CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED;
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED, testInput.getNavKeyGroup())).thenReturn(categoryIdList);
        when(categoryRepository.getCategoryDocuments(categoryIdList)).thenReturn(getCategoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp());
        when(categoryRepository.getCategoryDocument(categoryIdList.get(categoryIdList.size() - 1))).thenReturn(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrueAndContextPropDriveToSubCat());
        when(categoryRepository.getCategoryDocument(catToDriveTo)).thenReturn(getCategoryDocumentDriveTo());
        when(categoryRepository.getCategoryDocument(catToDriveToSecond)).thenReturn(getCategoryDocumentDriveToSecond());
        when(categoryRepository.getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner())).thenReturn(getDesignerCategoryDocument());
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(true))).thenReturn(getBreadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp());

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED, SOURCE_TOP_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb("designerCat", "Women's Apparel", "", topNavCat + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catSelected, "Gucci", "", topNavCat + "/catThatCouldBeReplaced/" + catSelected + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catToDriveTo, "Apparel", "", topNavCat + "/catThatCouldBeReplaced/" + catSelected + "/catToDriveTo/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catToDriveToSecond, "Women's", "", "url/c.cat"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
        assertEquals(expectedBreadcrumbList.get(2).getId(), actualBreadcrumbList.get(2).getId());
        assertEquals(expectedBreadcrumbList.get(3).getId(), actualBreadcrumbList.get(3).getId());
    }

    // Boutique In Hierarchy With Drive-To
    @Test
    public void shouldReturnBreadcrumbsForSelectedCategoryWithDriveToAndBoutiqueCategoryInHierarchy() {
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY, testInput.getNavKeyGroup())).thenReturn(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY);
        when(categoryRepository.getCategoryDocuments(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY)).thenReturn(getCategoryDocumentListWithBoutiqueTrueInHierarchyAndSelectedWithDriveToSubCat());
        when(categoryRepository.getCategoryDocument(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY.get(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY.size() - 1))).thenReturn(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        when(categoryRepository.getCategoryDocument(catToDriveTo)).thenReturn(getCategoryDocumentDriveTo());
        when(categoryRepository.getCategoryDocument(catToDriveToSecond)).thenReturn(getCategoryDocumentDriveToSecond());
        when(categoryRepository.getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner())).thenReturn(getDesignerCategoryDocument());
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(true))).thenReturn(getBreadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy());

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY, SOURCE_TOP_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb("designerCat", "Women's Apparel", "", topNavCat + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catBoutique, "Gucci", "", topNavCat + "/catThatCouldBeReplaced/" + catBoutique + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catSelected, "Apparel", "", "url/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catToDriveTo, "Women's", "", "url/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catToDriveToSecond, "Dresses", "", "url/c.cat"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
        assertEquals(expectedBreadcrumbList.get(2).getId(), actualBreadcrumbList.get(2).getId());
        assertEquals(expectedBreadcrumbList.get(3).getId(), actualBreadcrumbList.get(3).getId());
        assertEquals(expectedBreadcrumbList.get(4).getId(), actualBreadcrumbList.get(4).getId());
        assertEquals(expectedBreadcrumbList.get(5).getId(), actualBreadcrumbList.get(5).getId());
    }

    // Boutique in Hierarchy
    @Test
    public void shouldReturnBreadcrumbsWithBoutiqueCategoryInHierarchy() {
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY, testInput.getNavKeyGroup())).thenReturn(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY);
        when(categoryRepository.getCategoryDocuments(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY)).thenReturn(getCategoryDocumentListWithBoutiqueCategoryInHierarchy());
        when(categoryRepository.getCategoryDocument(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY.get(CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY.size() - 1))).thenReturn(getCategoryDocumentThatWasSelectedByUser());
        when(categoryRepository.getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner())).thenReturn(getDesignerCategoryDocument());
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(anyListOf(CategoryDocument.class), eq(true))).thenReturn(getBreadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy());

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY, SOURCE_TOP_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb("designerCat", "Women's Apparel", "", topNavCat + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catBoutique, "Gucci", "", topNavCat + "/catThatCouldBeReplaced/" + catBoutique + "/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        expectedBreadcrumbList.add(new Breadcrumb(catSelected, "Apparel", "", "url/c.cat"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
        assertEquals(expectedBreadcrumbList.get(2).getId(), actualBreadcrumbList.get(2).getId());
        assertEquals(expectedBreadcrumbList.get(3).getId(), actualBreadcrumbList.get(3).getId());
    }

    @Test
    public void shouldReturnBreadcrumbsWithoutLiveCatRootIdInUrlWhenNavPathDoesNotHaveLiveCatRootId() {
        String navPath = "cat3,cat2,cat1";
        String breadcrumbCategoryIds = "cat3_cat2_cat1";
        List<String> categoryIdList = new ArrayList<>(asList("cat3", "cat2", "cat1"));
        List<CategoryDocument> categoryDocumentList = new ArrayList<>();
        CategoryDocument cat1 = new CategoryDocumentBuilder().withId("id1").withName("name1").build();
        CategoryDocument cat2 = new CategoryDocumentBuilder().withId("id2").withName("name2").build();
        CategoryDocument cat3 = new CategoryDocumentBuilder().withId("id3").withName("name3").build();
        categoryDocumentList.addAll(asList(cat1, cat2, cat3));
        List<Breadcrumb> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new Breadcrumb("id1", "name1", "name1", "name1/id1/c.cat?navpath=id1"));
        breadcrumbList.add(new Breadcrumb("id2", "name2", "name2", "name1_name2/id2_id1/c.cat?navpath=id1_id2"));
        breadcrumbList.add(new Breadcrumb("id3", "name3", "name3", "name1_name2_name3/id3_id2_id1/c.cat?navpath=id1_id2_id3"));
        when(breadcrumbUtil.convertToListAndRemoveRootCategory(breadcrumbCategoryIds, testInput.getNavKeyGroup())).thenReturn(categoryIdList);
        when(categoryRepository.getCategoryDocuments(categoryIdList)).thenReturn(categoryDocumentList);
        when(categoryRepository.getCategoryDocument(categoryIdList.get(categoryDocumentList.size() - 1))).thenReturn(cat3);
        when(breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocumentList, false)).thenReturn(breadcrumbList);

        List<Breadcrumb> actualBreadcrumbList = breadcrumbService.getBreadcrumbs(navPath, SOURCE_LEFT_NAV, testInput.getNavKeyGroup());

        List<Breadcrumb> expectedBreadcrumbList = new ArrayList<>();
        expectedBreadcrumbList.add(new Breadcrumb("id1", "name1", "name1", "name1/id1/c.cat?navpath=id1"));
        expectedBreadcrumbList.add(new Breadcrumb("id2", "name2", "name2", "name1_name2/id2_id1/c.cat?navpath=id1_id2"));
        expectedBreadcrumbList.add(new Breadcrumb("id3", "name3", "name3", "name1_name2_name3/id3_id2_id1/c.cat?navpath=id1_id2_id3"));
        assertEquals(expectedBreadcrumbList.size(), actualBreadcrumbList.size());
        assertEquals(expectedBreadcrumbList.get(0).getId(), actualBreadcrumbList.get(0).getId());
        assertEquals(expectedBreadcrumbList.get(1).getId(), actualBreadcrumbList.get(1).getId());
        assertEquals(expectedBreadcrumbList.get(2).getId(), actualBreadcrumbList.get(2).getId());
    }

    @Test
    public void shouldReplaceCommaFromTheNavPath() {
        assertEquals(breadcrumbService.buildBackwardCompatiblePath("cat0,cat1,cat2"), "cat0_cat1_cat2");
        assertEquals(breadcrumbService.buildBackwardCompatiblePath("cat1_cat2_cat3"), "cat1_cat2_cat3");
    }

    @Test
    public void shouldReverseTheNavPathWhenNavPathEndsWithLiveCatId() {
        assertEquals(breadcrumbService.buildBackwardCompatiblePath("cat0_cat1_cat2_cat000000"), "cat000000_cat2_cat1_cat0");
    }

    @Test
    public void shouldReverseTheNavPathWhenNavPathEndsWithStageCatId() {
        assertEquals(breadcrumbService.buildBackwardCompatiblePath("cat0_cat1_cat2_cat400731"), "cat400731_cat2_cat1_cat0");
    }

    @Test
    public void shouldReverseTheNavPathWhenNavPathEndsWithMarketingCatId() {
        assertEquals(breadcrumbService.buildBackwardCompatiblePath("cat0_cat1_cat2_cat8900735"), "cat8900735_cat2_cat1_cat0");
    }

    private List<Breadcrumb> getBreadcrumbList() {
        List<Breadcrumb> breadcrumbList = new ArrayList<>();
        breadcrumbList.addAll(getBreadcrumbListForCategoryIds());
        return breadcrumbList;
    }

    private List<Breadcrumb> getBreadcrumbListFromSelectedBoutiqueCategory() {
        List<Breadcrumb> breadcrumbListFromBoutiqueCategory = new ArrayList<>();
        breadcrumbListFromBoutiqueCategory.add(getBreadcrumbToReplaceDesignerCategoryWith());
        breadcrumbListFromBoutiqueCategory.add(getBreadcrumbForCategoryThatWasSelectedByUser());
        return breadcrumbListFromBoutiqueCategory;
    }

    private List<Breadcrumb> getBreadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy() {
        List<Breadcrumb> breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy = new ArrayList<>();
        breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy.add(getBreadcrumbToReplaceDesignerCategoryWith());
        breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy.add(getBreadcrumbBoutique());
        breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy.add(getBreadcrumbForCategoryThatWasSelectedByUser());
        return breadcrumbListFromSelectedCatWithBoutiqueCatInHierarchy;
    }

    private List<Breadcrumb> getBreadcrumbListFromDriveToSubCatContextProp() {
        List<Breadcrumb> breadcrumbListFromDriveToSubCatContextProp = new ArrayList<>();
        breadcrumbListFromDriveToSubCatContextProp.addAll(getBreadcrumbListForCategoryIds());
        breadcrumbListFromDriveToSubCatContextProp.add(getBreadcrumbForDriveToSubCat());
        breadcrumbListFromDriveToSubCatContextProp.add(getBreadcrumbForDriveToSubCatSecond());
        return breadcrumbListFromDriveToSubCatContextProp;
    }

    private List<Breadcrumb> getBreadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp() {
        List<Breadcrumb> breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp = new ArrayList<>();
        breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp.add(getBreadcrumbToReplaceDesignerCategoryWith());
        breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp.add(getBreadcrumbForCategoryThatWasSelectedByUser());
        breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp.add(getBreadcrumbForDriveToSubCat());
        breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp.add(getBreadcrumbForDriveToSubCatSecond());
        return breadcrumbListFromBoutiqueCatAndDriveToSubCatContextProp;
    }

    private List<Breadcrumb> getBreadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy() {
        List<Breadcrumb> breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy = new ArrayList<>();
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(getBreadcrumbToReplaceDesignerCategoryWith());
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(getBreadcrumbBoutique());
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(getBreadcrumbForCategoryThatWasSelectedByUser());
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(getBreadcrumbForDriveToSubCat());
        breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy.add(getBreadcrumbForDriveToSubCatSecond());
        return breadcrumbListFromCatWithDriveToSubCatAndBoutiqueCatInHierarchy;
    }

    private Breadcrumb getBreadcrumbBoutique() {
        return new Breadcrumb(catBoutique, "Gucci", "", "designerCat/Gucci/c.cat");
    }

    private Breadcrumb getBreadcrumbToReplaceDesignerCategoryWith() {
        return new Breadcrumb("designerCat", "Designers", "", "designerCat/c.cat");
    }

    private Breadcrumb getBreadcrumbForCategoryThatWasSelectedByUser() {
        return new Breadcrumb(catSelected, "Gucci", "", "designerCat/Gucci/c.cat");
    }

    private List<Breadcrumb> getBreadcrumbListForCategoryIds() {
        List<Breadcrumb> breadcrumbListForCategoryIds = new ArrayList<>();
        breadcrumbListForCategoryIds.add(new Breadcrumb(topNavCat, "Women's Apparel", "", "topNavCat/c.cat"));
        breadcrumbListForCategoryIds.add(new Breadcrumb(catCouldBeReplaced, "Featured Designers", "", "topNavCat/catThatCouldBeReplaced/c.cat"));
        breadcrumbListForCategoryIds.add(new Breadcrumb(someCat, "Women's Apparel", "", "topNavCat/c.cat"));
        breadcrumbListForCategoryIds.add(new Breadcrumb(catSelected, "Gucci", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/c.cat"));
        return breadcrumbListForCategoryIds;
    }

    private Breadcrumb getBreadcrumbForDriveToSubCat() {
        return new Breadcrumb("catToDriveTo", "Apparel", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/catToDriveTo/c.cat");
    }

    private Breadcrumb getBreadcrumbForDriveToSubCatSecond() {
        return new Breadcrumb("catToDriveToSecond", "Women's", "", "topNavCat/catThatCouldBeReplaced/catThatWasSelectedByUser/catToDriveTo/catToDriveToSecond/c.cat");
    }
}
