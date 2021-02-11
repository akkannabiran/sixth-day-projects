package com.sixthday.navigation.api.exceptions;

import com.sixthday.navigation.api.models.NavigationErrorResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class NavigationGlobalExceptionHandlerTest {

    @InjectMocks
    private NavigationGlobalExceptionHandler navigationGlobalExceptionHandler;

    @Test
    public void testShouldRespond500ResponseCode_whenExceptionIsThrown() {

        final String exceptionMessage = "Runtime exception occurred";

        NavigationErrorResponse navigationErrorResponse = navigationGlobalExceptionHandler.handleException(new Exception(exceptionMessage));

        Assert.assertThat(navigationErrorResponse.getMessage(), CoreMatchers.is(exceptionMessage));
        Assert.assertThat(navigationErrorResponse.getStatusCode(), CoreMatchers.equalTo(500));
    }

}
