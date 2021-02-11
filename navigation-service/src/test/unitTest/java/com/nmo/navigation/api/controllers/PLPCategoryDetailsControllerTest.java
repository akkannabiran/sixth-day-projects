package com.sixthday.navigation.api.controllers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.mappers.PLPCategoryDetailsMapper;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.response.PLPCategoryDetails;
import com.sixthday.navigation.api.services.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PLPCategoryDetailsControllerTest {
    @Mock
    private CategoryService categoryService;

    @Mock
    private PLPCategoryDetailsMapper plpCategoryDetailsMapper;

    @InjectMocks
    private PLPCategoryDetailsController plpCategoryDetailsController;

    @Test
    public void shouldServe200WhenFoundCategoryDetailsForPLPForGivenCategoryId() {
        CategoryDocument testCategoryDocument = getTestCategoryDocument(CATEGORY_ID);

        when(categoryService.getCategoryDocument(CATEGORY_ID)).thenReturn(testCategoryDocument);
        when(plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty())).thenReturn(new PLPCategoryDetails());
        plpCategoryDetailsController.getCategoryDetailsForPLP(CATEGORY_ID, Optional.empty(), Optional.empty());
        verify(plpCategoryDetailsMapper, times(1)).map(testCategoryDocument, Optional.empty(), Optional.empty());
    }

    @Test(expected = NullPointerException.class)
    public void shouldServeNullPointerException_whenCategoryIdIsNull() {
        plpCategoryDetailsController.getCategoryDetailsForPLP(null, Optional.empty(), Optional.empty());
    }

    @Test
    public void shouldServe404WhenHandleCategoryNotFoundExceptionCalled() {
        NavigationErrorResponse navigationErrorResponse = plpCategoryDetailsController.handleCategoryNotFoundException(new CategoryNotFoundException(CATEGORY_ID));

        assertThat(navigationErrorResponse.getMessage(), equalTo(CATEGORY_EXCEPTION_MSG));
        assertThat(navigationErrorResponse.getStatusCode(), equalTo(404));
    }

}