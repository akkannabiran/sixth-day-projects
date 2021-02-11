package com.sixthday.navigation;

import au.com.dius.pact.provider.junit.*;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.navigation.api.controllers.BreadcrumbController;
import com.sixthday.navigation.api.controllers.LeftNavTreeController;
import com.sixthday.navigation.api.exceptions.BreadcrumbNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.api.mappers.LeftNavNodeMapper;
import com.sixthday.navigation.api.models.*;
import com.sixthday.navigation.api.services.BreadcrumbService;
import com.sixthday.navigation.api.services.LeftNavTreeService;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;

import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.mockito.Mockito.when;

@RunWith(SpringRestPactRunner.class)
@Provider("navigation-service")
@Consumer("ctp-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}",
        authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class CTPServiceContractTest {
    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();

    @InjectMocks
    private LeftNavTreeController leftNavTreeController;

    @InjectMocks
    private BreadcrumbController breadcrumbController;

    @Mock
    private LeftNavTreeService leftNavTreeService;

    @Mock
    private BreadcrumbService breadcrumbService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target.setControllers(breadcrumbController, leftNavTreeController);
    }

    @State("Has breadcrumbs")
    public void responseReturnBreadCrumbsWhenValidCategoryIdsAreThere() {
        List<Breadcrumb> breadcrumbs = Arrays.asList(
                new Breadcrumb("cat1", "name1", "mobile1", "url1"),
                new Breadcrumb("cat2", "name2", "mobile2", "url2")
        );

        when(breadcrumbService.getBreadcrumbs("cat1", "topNav", null)).thenReturn(breadcrumbs);
    }

    @State("Has breadcrumbs for test group")
    public void responseReturnTestBreadCrumbsWhenValidCategoryIdsAreThereForTestGroup() {
        List<Breadcrumb> breadcrumbs = Arrays.asList(
                new Breadcrumb("cat3", "BGroupSilo", "mobile1", "url1"),
                new Breadcrumb("cat2", "name2", "mobile2", "url2")
        );

        when(breadcrumbService.getBreadcrumbs("cat3", "topNav", "B")).thenReturn(breadcrumbs);
    }
    
    @State("Has no breadcrumbs")
    public void shouldReturnBreadCrumbsNotFoundResponse() {
        when(breadcrumbService.getBreadcrumbs("00000", SOURCE_TOP_NAV, null)).thenThrow(new BreadcrumbNotFoundException("00000"));
    }

    @State("Has leftNavTree")
    public void responseReturnsOkWithLeft() {
        LeftNavTree leftNavTree = setUpLeftNavTree();
        when(leftNavTreeService.getLeftNavTreeByNavPath("cat1", "US", "topNav", null)).thenReturn(leftNavTree);
    }
    
    @State("Has leftNavTree for test group")
    public void responseReturnsOkWithTestGroupLeftNavForTestGroup() {
        LeftNavTree leftNavTree = setUpLeftNavTree();
        when(leftNavTreeService.getLeftNavTreeByNavPath("cat1", "US", "topNav", "B")).thenReturn(leftNavTree);
    }

    @State("Has no leftNavTree")
    public void shouldReturnLeftNavTreeNotFoundResponse() {
        when(leftNavTreeService.getLeftNavTreeByNavPath("cat2", US_COUNTRY_CODE, SOURCE_TOP_NAV, null)).thenThrow(new LeftNavTreeNotFoundException("cat2"));
    }

    private LeftNavTree setUpLeftNavTree() {
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
                .withCategories(Collections.singletonList(level4Child)).build();

        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("cat1")
                .withName("name1")
                .withUrl("url1")
                .withPath("cat1")
                .withLevel(2)
                .withRedText(true)
                .withCategories(
                        Collections.singletonList(level3Child))
                .build();

        LeftNavTreeNode boutiqueCategory = new LeftNavCategoryBuilder().withId("cat000730")
                .withName("Shop All Designers").withUrl("designer_url")
                .withPath("")
                .withLevel(0)
                .build();
        String refreshablePath = "refreshablePath";
        return new LeftNavTree("cat1", "name1", Collections.singletonList(new LeftNavNodeMapper().map(leftNavTreeNode)), Collections.singletonList(new LeftNavNodeMapper().map(boutiqueCategory)), refreshablePath);
    }
}