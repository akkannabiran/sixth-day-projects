package com.sixthday.category.api.mappers;

import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.models.CategoryDocumentInfo;
import com.sixthday.category.config.CategoryServiceConfig;

import com.sixthday.category.elasticsearch.repository.CategoryRepository;
import com.sixthday.category.exceptions.UnknownCategoryTemplateTypeException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryMapperTest {

    private static final String SOME_TEMPLATE_TYPE = "some template type";
    private static final String SOME = "some";
    private static final String MY_CAT = "myCat";
    private static final String CATEGORY_NAME = "Category Name";
    private static final String PARENT_CATEGORY_ID = "parentCategoryId";
    private static final String DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE = "driveToChildCategoryIdImmediate";
    @InjectMocks
    private CategoryMapper categoryMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CategoryServiceConfig categoryServiceConfig;

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceConfig.CategoryTemplate anyCategoryTemplate = new CategoryServiceConfig.CategoryTemplate();

    @Before
    public void before() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        when(categoryServiceConfig.getImageServerUrl()).thenReturn("//images.sixthday.com");
        when(categoryServiceConfig.getDefaultProductImageSrc()).thenReturn("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg");
        when(categoryServiceConfig.getHeaderAssetUrl()).thenReturn("/category/{categoryId}/r_head_long.html");
    }

    @Test
    public void testCategoryBoutiqueIsTrueWhenBoutiqueChildIsTrue() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .boutiqueChild(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        categoryDocumentInfo.setCategoryDocument(categoryDocument);
        categoryDocumentInfo.setParentCategoryId(PARENT_CATEGORY_ID);
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getPageTemplateType(), is(SOME_TEMPLATE_TYPE));
        assertThat(category.isBoutique(), is(true));
    }

    @Test
    public void testCategoryHiddenIsTrueWhenHiddenIsTrue() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .name("name")
                .hidden(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo,Optional.empty());
        assertThat(category.isHidden(), is(true));
    }


    @Test
    public void testCategoryAttributesWhenCategoryAttributesAreSet() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteria validSearchCriteria = SearchCriteria.builder()
                .include(SearchCriteriaOptions.builder()
                        .hierarchy(Arrays.asList(hierarchyMap)).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .exclude(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList()).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .build();
        Filter filter2 = Filter.builder()
                .defaultName("filter2")
                .alternateName("alternateFilter2")
                .disabled(Arrays.asList("disabledFilter"))
                .values(Arrays.asList("filterValue1")).build();

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .name("name")
                .hidden(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .applicableFilters(Arrays.asList(
                        Filter.builder().defaultName("filter1").alternateName("alternateFilter1").disabled(Collections.emptyList()).values(Collections.emptyList()).build(),
                        filter2))
                .newArrivalLimit(1)
                .preferredProductIds(Arrays.asList("prod1","prod2"))
                .searchCriteria(validSearchCriteria)
                .showAllProducts(true)
                .cmosCatalogCodes(Arrays.asList("NMF119"))
                .hideMobileImage(false)
                .imageAvailable(true)
                .imageAvailableOverride("imageOverride")
                .alternateSeoName("seoAlternate")
                .canonicalUrl("canonical")
                .seoContentTitle("seoTitle")
                .seoTags("seoTags")
                .seoTitleOverride("seoTitleOverride")
                .seoContentDescription("seoContentDesc")
                .redirectType("301")
                .redirectTo("cat123")
                .excludeFromPCS(false)
                .thumbImageShot("imgShot")
                .displayAsGroups(true)
                .driveToGroupPDP(true)
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo,Optional.empty());
        assertThat(category.getApplicableFilters().get(0).getFilterKey(), is("filter1"));
        assertThat(category.getApplicableFilters().get(0).getDisplayText(), is("alternateFilter1"));
        assertThat(category.getApplicableFilters().get(0).getExcludeFields(), is(Collections.emptyList()));
        assertThat(category.getApplicableFilters().get(0).getValues(), is(Collections.emptyList()));
        assertThat(category.getApplicableFilters().get(1).getExcludeFields(), is(Arrays.asList("disabledFilter")));
        assertThat(category.getApplicableFilters().get(1).getValues(), is(Arrays.asList("filterValue1")));
        assertThat(category.getSearchCriteria().getCatalogIds(), is(Arrays.asList("NMF119")));
        assertThat(category.getSearchCriteria().getProductClassType(), is("INCLUDE_ALL"));
        assertThat(category.getNewArrivalLimit(), is(1));
        assertThat(category.getPreferredProductIds(), is(Arrays.asList("prod1","prod2")));
        assertThat(category.getCategoryHeaderAsset().isHideOsixthdaybile(), is(false));
        assertThat(category.getCategoryHeaderAsset().getCategoryAssetUrl(), is("/category/imageOverride/r_head_long.html"));
        assertThat(category.getSeo().getCanonicalUrl(), is("canonical"));
        assertThat(category.getSeo().getContent(), is("seoContentDesc"));
        assertThat(category.getSeo().getNameOverride(), is("seoAlternate"));
        assertThat(category.getSeo().getTitle(), is("seoTitle"));
        assertThat(category.getSeo().getRedirectDetails().getHttpCode(), is(301));
        assertThat(category.getSeo().getRedirectDetails().getRedirectToCategory(), is("cat123"));
        assertThat(category.getDefaultSortOption(), is("BEST_MATCH"));
        assertThat(category.getImageTemplateType(), is("imgShot"));
        assertThat(category.getPcsEnabled(), is(true));
        assertThat(category.isDisplayAsGroups(), is(true));
        assertThat(category.isDriveToGroupPDP(), is(true));
        assertThat(category.isReducedChildCount(), is(false));

        Filter filter = new Filter();
        filter.setDefaultName("filter2");
        filter.setAlternateName("alternateFilter2");
        filter.setDisabled(Arrays.asList("disabledFilter"));
        filter.setValues(Arrays.asList("filterValue1"));

        assertThat(filter2.getDefaultName(), equalTo(filter.getDefaultName()));
        assertThat(filter2.getAlternateName(), equalTo(filter.getAlternateName()));
        assertThat(filter2.getDisabled(), equalTo(filter.getDisabled()));
        assertThat(filter2.getValues(), equalTo(filter.getValues()));

        com.sixthday.category.api.models.Filter filterModel = com.sixthday.category.api.models.Filter.builder().build();
        filterModel.setFilterKey("filter1");
        filterModel.setDisplayText("alternateFilter1");
        filterModel.setExcludeFields(Collections.emptyList());
        filterModel.setValues(Collections.emptyList());

        assertThat(category.getApplicableFilters().get(0).getFilterKey(), equalTo(filterModel.getFilterKey()));
        assertThat(category.getApplicableFilters().get(0).getDisplayText(), equalTo(filterModel.getDisplayText()));
        assertThat(category.getApplicableFilters().get(0).getExcludeFields(), equalTo(filterModel.getExcludeFields()));
        assertThat(category.getApplicableFilters().get(0).getValues(), equalTo(filterModel.getValues()));

    }

    @Test
    public void testCategoryHiddenIsFalseWhenHiddenIsFalse() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .name("name")
                .hidden(false)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.isHidden(), is(false));
    }

    @Test
    public void testCategoryMobileHideSubcatIsTrueWhesixthdaybileHideSubcatIsTrue() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .name("name")
                .mobileHideEntrySubcats(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.isMobileHideEntrySubcats(), is(true));
    }

    @Test
    public void testCategoryMobileHideSubcatIsFalseWhesixthdaybileHideSubcatIsFalse() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .mobileHideEntrySubcats(false)
                .name("name")
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.isMobileHideEntrySubcats(), is(false));
    }

    @Test
    public void testCategoryBoutiqueIsTrueWhenBoutiqueIsTrue() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .boutique(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.isBoutique(), is(true));
    }

    @Test
    public void testCategoryBoutiqueIsTrueWhenBoutiqueAndBoutiqueChildAreTrue() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .boutique(true)
                .boutiqueChild(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.isBoutique(), is(true));
    }

    @Test
    public void testCategoryBoutiqueIsFalseWhenBoutiqueAndBoutiqueChildAreFalse() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .boutique(false)
                .boutiqueChild(false)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.isBoutique(), is(false));
    }

    @Test
    public void shouldReturnCategoryObjectBasedOnDriveToSubCategoryFirstSellableProductImageURL() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .redirectType("301")
                .children(children)
                .contextualProperties(contextualProperties)
                .excludedCountries(Collections.singletonList("US"))
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument driveToSubcategoryChildDocument = CategoryDocument.builder()
                .id(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)
                .name("DriveToSubcategoryChildImmediate Category Name")
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/DriveToSubcategoryChildImmediate  /some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(driveToSubcategoryChildDocument);
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is("DesktopAlternateName"));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getRedirectUrl(), is("some redirect to value"));
        assertThat(category.getRedirectType(), is("301"));
        assertThat(category.getFirstSellableProductImageUrl(), is("//images.sixthday.com/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/"));
        assertThat(category.getSeoMetaTags(), is("some seo tag"));
        assertThat(category.getSeoContentTitle(), is("some seo content title"));
        assertThat(category.getSeoContentDescription(), is("some seo content description"));
        assertThat(category.getCanonicalUrl(), is("/DriveToSubcategoryChildImmediate  /some_canonical_url/"));
        assertThat(category.getSeoPageTitle(), is("some seo title override"));
        assertThat(category.getChildren(), is(children));
        assertThat(category.getExcludedCountries(), is(Collections.singletonList("US")));
    }

    @Test
    public void testDriveToSubcategoryValueSetToGrandChildCatIdWhenGrandChildAvailable() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .contextualProperties(contextualProperties)
                .id(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)
                .name("DriveToSubcategoryChildImmediate Category Name")
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/DriveToSubcategoryChildImmediate  /some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getDriveToSubcategoryId(), is("driveToChildCategoryId:driveToChildCategoryIdImmediate"));
    }

    @Test
    public void testDriveToSubcategoryValueSetToImmediateChildCatIdWhenImmediateChildAvailable() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .contextualProperties(contextualProperties)
                .id(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)
                .name("DriveToSubcategoryChildImmediate Category Name")
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/DriveToSubcategoryChildImmediate  /some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId("parentCategoryId2")
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertNull(category.getDriveToSubcategoryId());
    }

    @Test
    public void testDriveToSubcategoryValueSetToNullWhenNoContextualPropertyMatchs() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(null, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .contextualProperties(contextualProperties)
                .id(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)
                .name("DriveToSubcategoryChildImmediate Category Name")
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/DriveToSubcategoryChildImmediate  /some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertNull(category.getDriveToSubcategoryId());
    }

    @Test
    public void shouldReturnCategoryObjectBasedOnItsOwnFirstSellableProductImageURL() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .redirectType("301")
                .children(children)
                .contextualProperties(contextualProperties)
                .excludedCountries(Collections.singletonList("US"))
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument driveToSubcategoryChildDocument = CategoryDocument.builder()
                .id(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)
                .name("DriveToSubcategoryChildImmediate Category Name")
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/DriveToSubcategoryChildImmediate  /some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/DriveToSubcategoryChildImmediate  /some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(driveToSubcategoryChildDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is("DesktopAlternateName"));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getRedirectUrl(), is("some redirect to value"));
        assertThat(category.getRedirectType(), is("301"));
        assertThat(category.getFirstSellableProductImageUrl(), is("//images.sixthday.com/some_first_sellable_product_image_url.jpg"));
        assertThat(category.getSeoMetaTags(), is("some seo tag"));
        assertThat(category.getSeoContentTitle(), is("some seo content title"));
        assertThat(category.getSeoContentDescription(), is("some seo content description"));
        assertThat(category.getCanonicalUrl(), is("/some_canonical_url/"));
        assertThat(category.getSeoPageTitle(), is("some seo title override"));
        assertThat(category.getChildren(), is(children));
        assertThat(category.getExcludedCountries(), is(Collections.singletonList("US")));
    }

    @Test
    public void shouldReturnCategoryObjectWhenDriveToSubcategoryDocumentIsNullButDriveToSubcategoryIdIsPresent() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .redirectType("301")
                .children(children)
                .contextualProperties(contextualProperties)
                .excludedCountries(Collections.singletonList("US"))
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(CategoryDocument.builder().build())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();

        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(null);

        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is("DesktopAlternateName"));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getRedirectUrl(), is("some redirect to value"));
        assertThat(category.getRedirectType(), is("301"));
        assertThat(category.getFirstSellableProductImageUrl(), is("//images.sixthday.com/some_first_sellable_product_image_url.jpg"));
        assertThat(category.getSeoMetaTags(), is("some seo tag"));
        assertThat(category.getSeoContentTitle(), is("some seo content title"));
        assertThat(category.getSeoContentDescription(), is("some seo content description"));
        assertThat(category.getCanonicalUrl(), is("/some_canonical_url/"));
        assertThat(category.getSeoPageTitle(), is("some seo title override"));
        assertThat(category.getChildren(), is(children));
        assertThat(category.getExcludedCountries(), is(Collections.singletonList("US")));
    }

    @Test
    public void shouldReturnCategoryObjectWithDriveToSubcategoryIdAsNullWhenDriveToSubcategoryIdIsNotPresent() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", null, null, false);
        List<ContextualProperty> contextualProperties = Collections.singletonList(contextualProperty1);

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .templateType(SOME_TEMPLATE_TYPE)
                .contextualProperties(contextualProperties)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(null);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is("DesktopAlternateName"));
        assertThat(category.getTemplateType(), is(SOME));
        assertNull(category.getDriveToSubcategoryId());
    }

    @Test
    public void shouldReturnCategoryObjectWhenDriveTOSubCategoryDocumentIsNull() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty(PARENT_CATEGORY_ID, "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .templateType(SOME_TEMPLATE_TYPE)
                .longDescription("http://some-redirect-url")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .redirectType("301")
                .children(children)
                .contextualProperties(contextualProperties)
                .excludedCountries(Collections.singletonList("US"))
                .mobileHideEntrySubcats(true)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(null);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is("DesktopAlternateName"));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getRedirectUrl(), is("some redirect to value"));
        assertThat(category.getRedirectType(), is("301"));
        assertThat(category.getFirstSellableProductImageUrl(), is("//images.sixthday.com/some_first_sellable_product_image_url.jpg"));
        assertThat(category.getSeoMetaTags(), is("some seo tag"));
        assertThat(category.getSeoContentTitle(), is("some seo content title"));
        assertThat(category.getSeoContentDescription(), is("some seo content description"));
        assertThat(category.getCanonicalUrl(), is("/some_canonical_url/"));
        assertThat(category.getSeoPageTitle(), is("some seo title override"));
        assertThat(category.getChildren(), is(children));
        assertThat(category.getExcludedCountries(), is(Collections.singletonList("US")));
        assertThat(category.isMobileHideEntrySubcats(), is(true));
    }

    @Test
    public void testDynamicPropertyWhenSearchCriteriaIsNotAvailable() {
        anyCategoryTemplate.setName("some template type");
        anyCategoryTemplate.setKey("some");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument("myCat")).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertTrue(category.isDynamic());
    }

    @Test
    public void testDynamicPropertyWhenSearchCriteriaIsAvailable() {
        anyCategoryTemplate.setName("some template type");
        anyCategoryTemplate.setKey("some");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions includeFactors = SearchCriteriaOptions.builder()
                .hierarchy(Arrays.asList(hierarchyMap))
                .build();

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .searchCriteria(com.sixthday.navigation.api.elasticsearch.models.SearchCriteria.builder().build().builder().include(includeFactors).build())
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument("myCat")).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertFalse(category.isDynamic());
    }

    @Test
    public void testResultsPropertyWhenCategoryDocumentHasNoResultsPropertyAsTrue() {
        anyCategoryTemplate.setName("some template type");
        anyCategoryTemplate.setKey("some");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .results(true)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .preferredProductIds(Arrays.asList("prod1","prod2"))
                .excludeFromPCS(true)
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(true)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument("myCat")).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertFalse(category.isResults());
    }

    @Test
    public void testResultsPropertyWhenCategoryDocumentHasNoResultsPropertyAsFalse() {
        anyCategoryTemplate.setName("some template type");
        anyCategoryTemplate.setKey("some");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .results(false)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        when(categoryRepository.getCategoryDocument("myCat")).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());
        assertTrue(category.isResults());
    }

    @Test(expected = UnknownCategoryTemplateTypeException.class)
    public void shouldThrowUnknownCategoryTemplateTypeExceptionWhenUnsupportedTemplateTypeFound() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        List<String> children = new ArrayList<>();
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name("name")
                .templateType("some template")
                .children(children)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        categoryMapper.map(categoryDocumentInfo, Optional.empty());
    }

    private CategoryDocument getParentCategoryDocumentWithBasicInformation() {
        return CategoryDocument.builder().id("pId").name("pName").build();
    }

    @Test
    public void testCategoryExcludeFromPCSIsTrueWhenExcludeFromPCSIsTrue() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .excludeFromPCS(true)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertTrue(category.isExcludeFromPCS());
    }

    @Test
    public void testCategoryExcludeFromPCSIsTrueWhenExcludeFromPCSIsFalse() {
        anyCategoryTemplate.setName(SOME_TEMPLATE_TYPE);
        anyCategoryTemplate.setKey(SOME);
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .excludeFromPCS(false)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        when(categoryRepository.getCategoryDocument(DRIVE_TO_CHILD_CATEGORY_ID_IMMEDIATE)).thenReturn(categoryDocument);
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .parentCategoryDocument(getParentCategoryDocumentWithBasicInformation())
                .parentCategoryId(PARENT_CATEGORY_ID)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertFalse(category.isExcludeFromPCS());
    }
    
    @Test
    public void testCategoryTreesIsSetToDefinedListWhenCaegoryTreeIsDefinedListInCategoryDocument() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .type(Arrays.asList("STAGE", "LIVE"))
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getCatalogTrees(), is(Arrays.asList("STAGE", "LIVE")));
    }
    
    @Test
    public void testCategoryTreesIsSetToEmptyWhenCaegoryTreeIsEmptyInCategoryDocument() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .type(Collections.emptyList())
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getCatalogTrees(), is(Collections.emptyList()));
    }
    
    @Test
    public void testCategoryTreesIsSetToNullWhenCaegoryTreeIsNullInCategoryDocument() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .type(null)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getCatalogTrees(), nullValue());
    }
    
    @Test
    public void testCategoryIsSetWithDefaultPathWhenCategoryHasDefaultPathInCategoryDocument() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id(MY_CAT)
                .name(CATEGORY_NAME)
                .type(null)
                .templateType(SOME_TEMPLATE_TYPE)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();
        CategoryDocumentInfo categoryDocumentInfo = CategoryDocumentInfo.builder()
                .categoryDocument(categoryDocument)
                .categoryId(MY_CAT)
                .build();
        Category category = categoryMapper.map(categoryDocumentInfo, Optional.empty());

        assertThat(category.getId(), is(MY_CAT));
        assertThat(category.getName(), is(CATEGORY_NAME));
        assertThat(category.getTemplateType(), is(SOME));
        assertThat(category.getCatalogTrees(), nullValue());
        assertThat(category.getDefaultPath(), is("cat000000_T1CAT39040731_myCat"));
    }
}