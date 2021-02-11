package com.sixthday.category.api.controllers;

import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.models.CategoryErrorResponse;
import com.sixthday.category.api.models.Filter;
import com.sixthday.category.api.services.CategoryService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    @Test
    public void shouldReturnCategoriesWhenGetCategoriesInvoked() {
        List<Category> categories = Collections.singletonList(Category.builder().id("cat123").build());
        Filter filter = new Filter();
        filter.setFilterKey("filterKey");
        categories.get(0).setApplicableFilters(Arrays.asList(filter));
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat123", "parentCat123");
        categoryIds.put("cat1234", "parentCat1234");

        when(categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty())).thenReturn(categories);

        List<Category> response =categoryController.getCategories("US", categoryIds, Optional.empty(), Optional.empty());

        verify(categoryService, times(1)).getCategories(categoryIds,Optional.empty(), Optional.empty());
        assertThat(response.get(0).getApplicableFilters().get(0).getFilterKey(),equalTo("filterKey"));
    }

    @Test
    public void shouldReturnCategoriesWhenGetCategoriesInvokedWithNavPath() {
        List<Category> categories = Collections.singletonList(Category.builder().id("cat123").build());
        Filter filter = new Filter();
        filter.setFilterKey("filterKey");
        categories.get(0).setApplicableFilters(Arrays.asList(filter));
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat123", "parentCat123");
        categoryIds.put("cat1234", "parentCat1234");

        when(categoryService.getCategories(categoryIds, Optional.empty(), Optional.of("cat000000_parentCat123_cat123"))).thenReturn(categories);

        categoryController.getCategories("US", categoryIds, Optional.empty(), Optional.of("cat000000_parentCat123_cat123"));

        verify(categoryService, times(1)).getCategories(categoryIds,Optional.empty(), Optional.of("cat000000_parentCat123_cat123"));
    }

    @Test
    public void shouldServe404WhenHandleCategoryControllerExceptionCalled() {
        CategoryErrorResponse categoryErrorResponse = categoryController.handleCategoryControllerException(new Exception("cat123"));

        assertThat(categoryErrorResponse.getMessage(), equalTo("Unable to process the request, internal error occurred cat123"));
        assertThat(categoryErrorResponse.getStatusCode(), equalTo(404));
    }

    @Test
    public void shouldMDCHoldCountryCode() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat123", "parentCat123");
        categoryIds.put("cat1234", "parentCat1234");

        categoryController.getCategories("US", categoryIds, Optional.empty(), Optional.empty());
        assertThat(MDC.get("CountryCode"), Matchers.equalTo("\"US\""));
    }
}