package com.sixthday.navigation.api.controllers;

import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.services.LeftNavTreeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.NAV_PATH;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getTestLeftNavTree;
import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LeftNavTreeNodeTreeControllerTest {

    @Mock
    private LeftNavTreeService leftNavTreeService;

    @InjectMocks
    private LeftNavTreeController leftNavTreeController;

    @Test
    public void shouldReturnLeftNavTreeIfAvailable() {
        when(leftNavTreeService.getLeftNavTreeByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, null)).thenReturn(getTestLeftNavTree());
        LeftNavTree result = leftNavTreeController.getLeftNavTreeForThisNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, null);
        assertThat(result.getId(), is(NAV_PATH));
    }

    @Test
    public void shouldServe404WhenLeftNavTreeIsUnavailable() {
        NavigationErrorResponse navigationErrorResponse = leftNavTreeController.handleLeftNavTreeNotFoundException(new LeftNavTreeNotFoundException(NAV_PATH));
        assertThat(navigationErrorResponse.getStatusCode(), equalTo(404));
    }
    
    @Test
    public void shouldReturnLeftNavTreeFromTestSiloIfAvailableForNavKeyGroupB() {
        when(leftNavTreeService.getLeftNavTreeByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B")).thenReturn(getTestLeftNavTree());
        
        LeftNavTree result = leftNavTreeController.getLeftNavTreeForThisNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B");
        
        verify(leftNavTreeService).getLeftNavTreeByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B");
        assertThat(result.getId(), is(NAV_PATH));
    }
}
