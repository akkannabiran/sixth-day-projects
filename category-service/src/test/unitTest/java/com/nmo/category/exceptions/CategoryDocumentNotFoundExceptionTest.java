package com.sixthday.category.exceptions;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CategoryDocumentNotFoundExceptionTest {
    @InjectMocks
    private CategoryDocumentNotFoundException categoryDocumentNotFoundException;

    @Test
    public void shouldRespondWithErrorMessageWhenExceptionIsThrown() {

        final String exceptionMessage = "Unable to get the categories for the requested Id ";
        categoryDocumentNotFoundException = new CategoryDocumentNotFoundException("categoryId");
        Assert.assertThat(categoryDocumentNotFoundException.getMessage(), CoreMatchers.is(exceptionMessage + "categoryId"));
    }
}