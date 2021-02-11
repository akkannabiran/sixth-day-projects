package com.sixthday.navigation;

import au.com.dius.pact.provider.junit.*;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.controllers.*;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.exceptions.BreadcrumbNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.api.mappers.LeftNavNodeMapper;
import com.sixthday.navigation.api.models.*;
import com.sixthday.navigation.api.models.response.BrandLinks;
import com.sixthday.navigation.api.services.*;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.domain.CategoryNode;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;

import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PactRunner.class)
@Provider("navigation-service")
@Consumer("sixthday-ui")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}",
        authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class NMOUIContractTest {
    private static final String DEVICE_TYPE_DESKTOP = "desktop";
    private static final String DEVICE_TYPE_MOBILE = "mobile";
    private static final String UNITED_STATES_COUNTRY_CODE = "US";
    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();
    private BrandLinks brandLinks;
    private String categoryIds;
    private String categoryId;
    private List<Breadcrumb> breadcrumbs;
    private LeftNavTree leftNavTree;
    private Silos desktopSilos;
    private Silos mobileSilos;
    private Silos initialMobileSilos;
    
    private Silos testGroupDesktopSilos;
    private Silos testGroupMobileSilos;
    private Silos testGroupInitialMobileSilos;
    
    @Mock
    private BrandLinksService brandLinksService;

    @Mock
    private BreadcrumbService breadcrumbService;

    @Mock
    private LeftNavTreeService leftNavTreeService;

    @InjectMocks
    private LeftNavTreeController leftNavTreeController;

    @Mock
    private SiloNavTreeService siloNavTreeService;

    private NavigationServiceConfig navigationServiceConfig;

    @Mock
    private CategoryService categoryService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        navigationServiceConfig = mock(NavigationServiceConfig.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS.get()));

        BrandLinksController brandLinksController = new BrandLinksController(brandLinksService);
        setUpBrandLinks();

        BreadcrumbController breadcrumbController = new BreadcrumbController(breadcrumbService);
        categoryIds = "cat1,cat2";
        categoryId = "cat1";
        setUpBreadcrumbs();

        SiloNavTreeController siloNavTreeController = new SiloNavTreeController(siloNavTreeService);

        target.setControllers(brandLinksController, breadcrumbController, siloNavTreeController, leftNavTreeController);
        setUpLeftNavTree();
        setupDesktopSiloData();
        setupMobileSiloData();
        setupInitialMobileSiloData();
    }

    @State("Has brandLinks")
    public void shouldReturnOkResponseWithBrandLinks() {
        when(brandLinksService.getBrandLinks()).thenReturn(brandLinks);
    }

    @State("Has breadcrumbs")
    public void shouldReturnOkResponseWithBreadcrumbs() {
        when(breadcrumbService.getBreadcrumbs(categoryIds, SOURCE_TOP_NAV, null)).thenReturn(breadcrumbs);
    }

    @State("Default breadcrumbs")
    public void shouldReturnOkResponseWithDefaultBreadcrumbs() {
        when(breadcrumbService.getBreadcrumbs(categoryId, SOURCE_TOP_NAV, null)).thenReturn(breadcrumbs);
    }

    @State("Has no breadcrumbs")
    public void shouldReturnBreadCrumbsNotFoundResponse() {
        when(breadcrumbService.getBreadcrumbs("00000", SOURCE_TOP_NAV, null)).thenThrow(new BreadcrumbNotFoundException("00000"));
    }

    @State("Has leftNavTree")
    public void shouldReturnOkResponseWithLeftNavTree() {
        when(leftNavTreeService.getLeftNavTreeByNavPath("cat1", US_COUNTRY_CODE, SOURCE_TOP_NAV, null)).thenReturn(leftNavTree);
    }

    @State("Has no leftNavTree")
    public void shouldReturnLeftNavTreeNotFoundResponse() {
        when(leftNavTreeService.getLeftNavTreeByNavPath("cat2", US_COUNTRY_CODE, SOURCE_TOP_NAV, null)).thenThrow(new LeftNavTreeNotFoundException("cat2"));
    }

    @State("Has desktop silos")
    public void shouldReturnOkResponseWithDesktopSilos() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null)).thenReturn(desktopSilos);
    }

    @State("Has mobile silos")
    public void shouldReturnOkResponseWithMobileSilos() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_MOBILE, null)).thenReturn(mobileSilos);
    }

    @State("Has initial mobile silos")
    public void shouldReturnOkResponseWithInitialMobileSilos() {
        when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null)).thenReturn(initialMobileSilos);
    }
    
    @State("Has desktop TestGroup silos")
    public void responseReturnTestGroupDesktopNavSilosWhenValidCountryCodeIsPassed() {
        when(siloNavTreeService.getSilos("US", "desktop", "B")).thenReturn(testGroupDesktopSilos);
    }

    @State("Has mobile TestGroup silos")
    public void responseReturnTestGroupMobileNavSilosWhenValidCountryCodeIsPassed() {
        when(siloNavTreeService.getSilos("US", "mobile", "B")).thenReturn(testGroupMobileSilos);
    }
    
    @State("Has initial TestGroup silos")
    public void responseReturnMobileNavTestGroupInitialSilosWhenValidCountryCodeIsPassed() {
        when(siloNavTreeService.getInitialMobileSilos("US", null)).thenReturn(testGroupInitialMobileSilos);
    }

    @State("X0 category template with valid URL in long description")
    public void shouldReturnOkResponseWithX0CategoryTemplateDetails() {
        when(categoryService.getCategoryDocument(anyString())).thenReturn(getX0CategoryDocument());
    }

    @State("X0 category template with invalid URL in long description")
    public void shouldReturn404ResponseWhenTemplateTypeIsX0AndLongDescriptionHasEmptyOrInvalidUrl() {
        when(categoryService.getCategoryDocument(anyString())).thenReturn(getX0CategoryDocumentWithInvalidLongDescription());
    }

    @State("Invalid category template type")
    public void shouldReturn404WhenCategoryTemplateTypeIsEmptyOrInvalid() {
        when(categoryService.getCategoryDocument(anyString())).thenReturn(getCategoryDocumentWithInvalidTemplateType());
    }

    private CategoryDocument getX0CategoryDocument() {
        return CategoryDocument.builder().id("cat54930740").templateType("some category type").longDescription("https://www.neimanmarcus.com/Lifestyles/Contemporary-CUSP/cat40510788_cat41200736_cat000141/c.cat#userConstrainedResults=true&refinements=73700035,73700036,73700037,73700038,73700039,73700040,73700041&page=1&pageSize=120&sort=&definitionPath=/nm/commerce/pagedef_rwd/template/EndecaDrivenHome&locationInput=&radiusInput=100&onlineOnly=instore&allStoresInput=false&rwd=true&catalogId=cat40510788").build();
    }

    private CategoryDocument getX0CategoryDocumentWithInvalidLongDescription() {
        return CategoryDocument.builder().id("catX0").templateType("some category type").longDescription("blabla").build();
    }

    private CategoryDocument getCategoryDocumentWithInvalidTemplateType() {
        return CategoryDocument.builder().id("catX0").templateType("").build();
    }

    void setUpBrandLinks() {
        brandLinks = new BrandLinks();

        List sisterSites = new ArrayList<>();

        sisterSites.add(new SisterSite("Sixthday", "https://www.neimanmarcus.com/",
                Arrays.asList(new SisterSite.TopCategory("Shop Shoes", "/Shoes/cat000141/c.cat"),
                        new SisterSite.TopCategory("All Apparel", "/Womens-Clothing/All-Apparel/cat58290731_cat17740747_cat000001/c.cat"),
                        (new SisterSite.TopCategory("Dresses", "/Womens-Clothing/Dresses/cat43810733_cat17740747_cat000001/c.cat")))));

        sisterSites.add(new SisterSite("Last Call", "http://www.lastcall.com/",
                Arrays.asList(new SisterSite.TopCategory("Shop Shoes", "/Shoes/cat000141/c.cat"),
                        new SisterSite.TopCategory("All Apparel", "/Womens-Clothing/All-Apparel/cat58290731_cat17740747_cat000001/c.cat"),
                        (new SisterSite.TopCategory("Dresses", "/Womens-Clothing/Dresses/cat43810733_cat17740747_cat000001/c.cat")))));

        sisterSites.add(new SisterSite("My Theresa", "http://www.mytheresa.com/en-us/",
                Arrays.asList(new SisterSite.TopCategory("Shop Shoes", "/Shoes/cat000141/c.cat"),
                        new SisterSite.TopCategory("All Apparel", "/Womens-Clothing/All-Apparel/cat58290731_cat17740747_cat000001/c.cat"),
                        (new SisterSite.TopCategory("Dresses", "/Womens-Clothing/Dresses/cat43810733_cat17740747_cat000001/c.cat")))));

        sisterSites.add(new SisterSite("Bergdorf Goodman", "http://www.mytheresa.com/en-us/",
                Arrays.asList(new SisterSite.TopCategory("Shop Shoes", "/Shoes/cat000141/c.cat"),
                        new SisterSite.TopCategory("All Apparel", "/Womens-Clothing/All-Apparel/cat58290731_cat17740747_cat000001/c.cat"),
                        (new SisterSite.TopCategory("Dresses", "/Womens-Clothing/Dresses/cat43810733_cat17740747_cat000001/c.cat")))));

        sisterSites.add(new SisterSite("HORCHOW", "http://www.mytheresa.com/en-us/",
                Arrays.asList(new SisterSite.TopCategory("Shop Shoes", "/Shoes/cat000141/c.cat"),
                        new SisterSite.TopCategory("All Apparel", "/Womens-Clothing/All-Apparel/cat58290731_cat17740747_cat000001/c.cat"),
                        (new SisterSite.TopCategory("Dresses", "/Womens-Clothing/Dresses/cat43810733_cat17740747_cat000001/c.cat")))));

        brandLinks.setSisterSites(sisterSites);
    }

    void setUpBreadcrumbs() {
        Breadcrumb breadcrumb1 = new Breadcrumb("cat1", "name1 ", "mobile1", "url1");
        Breadcrumb breadcrumb2 = new Breadcrumb("cat2", "name2 ", "mobile2", "url2");
        breadcrumbs = new LinkedList<>();
        breadcrumbs.add(breadcrumb1);
        breadcrumbs.add(breadcrumb2);
    }

    void setUpLeftNavTree() {
        LeftNavTreeNode level4Child = new LeftNavCategoryBuilder()
                .withName("level2childName").withUrl("url3").withId("level2childId")
                .withPath("cat1_level1childId_level2childId")
                .withLevel(4)
                .withSelected(true)
                .build();

        LeftNavTreeNode level3Child = new LeftNavCategoryBuilder()
                .withUrl("url2").withId("level1childId").withName("level1childName")
                .withPath("cat1_level1childId")
                .withLevel(3)
                .withRedText(false)
                .withCategories(Arrays.asList(level4Child)).build();

        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("cat1")
                .withName("name1").withUrl("url1")
                .withPath("cat1")
                .withLevel(2)
                .withRedText(true)
                .withCategories(
                        Arrays.asList(level3Child))
                .build();

        LeftNavTreeNode boutiqueCategory = new LeftNavCategoryBuilder().withId("cat000730")
                .withName("Shop All Designers").withUrl("designer_url")
                .withPath("")
                .withLevel(0)
                .build();
        String refreshablePath = "refreshablePath";
        leftNavTree = new LeftNavTree("cat1", "name1", Arrays.asList(new LeftNavNodeMapper().map(leftNavTreeNode)), Arrays.asList(new LeftNavNodeMapper().map(boutiqueCategory)), refreshablePath);
    }

    private void setupDesktopSiloData() {
        desktopSilos = new Silos();
        testGroupDesktopSilos = new Silos();
        
        CategoryNode desktopLevel2Node1 = new CategoryNodeBuilder().withCatmanId("cat10020738").withId("cat10020738").withLevel(2).withName("Akris")
                .withUrl("/Akris/Apparel/cat37230758_cat10020738/c.cat").build();
        CategoryNode desktopLevel1Node2 = new CategoryNodeBuilder().withCatmanId("cat44670744").withId("cat44670744").withLevel(1).withName("Featured Designers")
                .withUrl("/Womens-Clothing/Featured-Designers/cat44670744_cat000001/c.cat").withCategories(asList(desktopLevel2Node1)).build();
        CategoryNode desktopLevel1Node1 = new CategoryNodeBuilder().withCatmanId("cat000009").withId("cat000009").withLevel(1).withName("All Designers")
                .withUrl("/Womens-Clothing/All-Designers/cat000009_cat000001/c.cat").build();
        CategoryNode desktopLevel0Node1 = new CategoryNodeBuilder().withCatmanId("cat000001").withId("cat000001").withName("Women's Apparel")
                .withUrl("/Womens-Clothing/cat000001/c.cat").withCategories(asList(desktopLevel1Node1, desktopLevel1Node2)).withAttribute("promoPath", "/category/cat44710738/r_main_drawer_promo.html").build();

        CategoryNode desktopLevel2Node2 = new CategoryNodeBuilder().withCatmanId("cat59430736").withId("cat59430736").withLevel(2).withName("Apparel")
                .withUrl("/CUSP/All-Designers/Apparel/cat59430736_cat59300750/c.cat").build();
        CategoryNode desktopLevel1Node3 = new CategoryNodeBuilder().withCatmanId("cat59300750").withId("cat59300750").withLevel(1).withName("All Designers")
                .withUrl("/CUSP/All-Designers/cat59300750_cat58930763/c.cat").withCategories(asList(desktopLevel2Node2)).build();
        CategoryNode desktopLevel0Node2 = new CategoryNodeBuilder().withCatmanId("cat58930763").withId("cat58930763").withName("CUSP")
                .withUrl("/CUSP/cat58930763/c.cat").withAttribute("promoPath", "/category/cat58930763/r_cusp_drawer_promo.html").withCategories(asList(desktopLevel1Node3)).build();

        List<CategoryNode> nodes = asList(desktopLevel0Node1, desktopLevel0Node2);
        desktopSilos.setSilosTree(nodes);
        testGroupDesktopSilos.setSilosTree(getTestGroupSiloTree());
    }

    private void setupMobileSiloData() {
        mobileSilos = new Silos();
        testGroupMobileSilos = new Silos();
        
        CategoryNode mobileLevel4Node = new CategoryNodeBuilder().withCatmanId("cat41150738").withId("2139").withLevel(4).withName("Men's")
                .withUrl("/Alexander-McQueen/Mens/cat41150738_cat10230739/c.cat").withAttribute("tags", asList("HC")).build();
        CategoryNode mobileLevel3Node = new CategoryNodeBuilder().withCatmanId("cat10230739").withId("2134").withLevel(3).withName("Alexander McQueen")
                .withUrl("/Alexander-McQueen/Womens-Apparel/cat25930736_cat10230739/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(mobileLevel4Node)).build();
        CategoryNode mobileLevel2Node = new CategoryNodeBuilder().withCatmanId("cat000019").withId("2103").withLevel(2).withName("Women's Apparel")
                .withUrl("/Womens-Clothing/All-Designers/Womens-Apparel/cat000019_cat000009/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(mobileLevel3Node)).build();
        CategoryNode mobileLevel1Node = new CategoryNodeBuilder().withCatmanId("cat000009").withId("2102").withLevel(1).withName("All Designers")
                .withUrl("/Womens-Clothing/All-Designers/cat000009_cat000001/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(mobileLevel2Node)).build();
        CategoryNode mobileLevel0Node = new CategoryNodeBuilder().withCatmanId("cat000001").withId("2099").withName("Women's Apparel")
                .withUrl("/Womens-Clothing/cat000001_cat000000/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(mobileLevel1Node)).build();

        mobileSilos.setSilosTree(asList(mobileLevel0Node));
        testGroupMobileSilos.setSilosTree(getTestGroupSiloTree());
    }

    private void setupInitialMobileSiloData() {
        initialMobileSilos = new Silos();
        testGroupInitialMobileSilos = new Silos();
        
        CategoryNode initialMobileLevel2Node = new CategoryNodeBuilder().withCatmanId("cat000019").withId("2103").withLevel(2).withName("Women's Apparel")
                .withUrl("/Womens-Clothing/All-Designers/Womens-Apparel/cat000019_cat000009/c.cat").withAttribute("tags", asList("HC")).withCategories(new ArrayList<>()).build();
        CategoryNode initialMobileLevel1Node = new CategoryNodeBuilder().withCatmanId("cat000009").withId("2102").withLevel(1).withName("All Designers")
                .withUrl("/Womens-Clothing/All-Designers/cat000009_cat000001/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(initialMobileLevel2Node)).build();
        CategoryNode initialMobileLevel0Node = new CategoryNodeBuilder().withCatmanId("cat000001").withId("2099").withName("Women's Apparel")
                .withUrl("/Womens-Clothing/cat000001_cat000000/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(initialMobileLevel1Node)).build();

        initialMobileSilos.setSilosTree(asList(initialMobileLevel0Node));
        testGroupDesktopSilos.setSilosTree(getTestGroupSiloTree());
    }
    
    private List<CategoryNode> getTestGroupSiloTree() {
      CategoryNode l2Node = new CategoryNodeBuilder().withCatmanId("catLevel2CategoryId").withId("level2CategoryId").withLevel(2).withName("Women's")
              .withUrl("/Womens/All-Designers/Womens-Apparel/catLevel2CategoryId_cat000009/c.cat").withAttribute("tags", asList("HC")).withCategories(new ArrayList<>()).build();
      CategoryNode l1Node = new CategoryNodeBuilder().withCatmanId("catLevel1CategoryId").withId("level1CategoryId").withLevel(1).withName("All Designers")
              .withUrl("/Womens/All-Designers/catLevel2CategoryId_catLevel0CategoryId/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(l2Node)).build();
      CategoryNode l0Node = new CategoryNodeBuilder().withCatmanId("catLevel0CategoryId").withId("level0CategoryId").withName("Women's")
              .withUrl("/Womens/catLevel0CategoryId_cat000000/c.cat").withAttribute("tags", asList("HC")).withCategories(asList(l1Node)).build();
      return Collections.singletonList(l0Node);
    }
}
