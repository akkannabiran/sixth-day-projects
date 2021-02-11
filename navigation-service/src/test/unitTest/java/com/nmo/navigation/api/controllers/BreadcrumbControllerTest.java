package com.sixthday.navigation.api.controllers;

import com.sixthday.navigation.api.exceptions.BreadcrumbNotFoundException;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.models.Breadcrumbs;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.services.BreadcrumbService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.*;
import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class BreadcrumbControllerTest {

    @Mock
    private BreadcrumbService breadcrumbService;

    @InjectMocks
    private BreadcrumbController breadcrumbController;

    @Test
    public void shouldReturnListOfBreadcrumbsIfThereAreMatchingCategoryFound() {
        when(breadcrumbService.getBreadcrumbs(CATEGORY_IDS, SOURCE_TOP_NAV, null)).thenReturn(getTestBreadcrumbs());
        Breadcrumbs outputBreadcrumbs = breadcrumbController.getBreadcrumbs(CATEGORY_IDS, SOURCE_TOP_NAV, null);
        assertThat(outputBreadcrumbs.getBreadcrumbList().size(), is(2));
    }

    @Test
    public void shouldServe404WhenHandleBreadcrumbNotFoundExceptionCalled() {
        NavigationErrorResponse navigationErrorResponse = breadcrumbController.handleBreadcrumbNotFoundException(new BreadcrumbNotFoundException(CATEGORY_IDS));
        assertThat(navigationErrorResponse.getMessage(), equalTo(BREADCRUMB_EXCEPTION_MSG));
        assertThat(navigationErrorResponse.getStatusCode(), equalTo(404));
    }
    
    @Test
    public void shouldReturnListOfBreadcrumbsForGroupBIfThereAreMatchingCategoryFoundAndNavKeyGroupIsB() {
        when(breadcrumbService.getBreadcrumbs(CATEGORY_IDS, SOURCE_TOP_NAV, "B")).thenReturn(Arrays.asList(new Breadcrumb("idCat1", "nameCat1", "", "/nameCat1/idCat1/c.cat"),
                new Breadcrumb("idCat2B", "nameCat2B", "", "/nameCat2B/idCat2B/c.cat")));
        
        Breadcrumbs outputBreadcrumbs = breadcrumbController.getBreadcrumbs(CATEGORY_IDS, SOURCE_TOP_NAV, "B");
        
        verify(breadcrumbService).getBreadcrumbs(anyString(), anyString(), eq("B"));
        assertThat(outputBreadcrumbs.getBreadcrumbList().size(), is(2));
        
    }
}
