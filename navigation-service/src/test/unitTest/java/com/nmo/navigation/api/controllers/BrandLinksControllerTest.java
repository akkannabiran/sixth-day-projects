package com.sixthday.navigation.api.controllers;

import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;
import com.sixthday.navigation.api.services.BrandLinksService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrandLinksControllerTest {
    @Mock
    private BrandLinksService brandLinksService;

    @InjectMocks
    private BrandLinksController brandLinksController;

    @Test
    public void shouldReturnBrandLinks() {
        when(brandLinksService.getBrandLinks()).thenReturn(new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));
        BrandLinks actual = brandLinksController.getBrandLinks();
        assertEquals("name", actual.getSisterSites().get(0).getName());
        assertEquals("url", actual.getSisterSites().get(0).getUrl());
        assertEquals("topCat", actual.getSisterSites().get(0).getTopCategories().get(0).getName());
        assertEquals("topCatUrl", actual.getSisterSites().get(0).getTopCategories().get(0).getUrl());
    }

    @Test
    public void testHandleCategoryNotFoundException() {
        NavigationErrorResponse navigationErrorResponse = brandLinksController.handleCategoryNotFoundException(new CategoryNotFoundException("cat1"));
        assertThat(navigationErrorResponse.getStatusCode(), is(404));
        assertThat(navigationErrorResponse.getMessage(), is("Category information is not available for the requested category cat1"));
    }

}