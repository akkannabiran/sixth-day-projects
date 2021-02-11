package com.sixthday.navigation.api.controllers;

import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.NAV_PATH;
import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getTestLeftNavTree;
import static com.sixthday.navigation.config.Constants.SOURCE_TOP_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sixthday.navigation.api.exceptions.HybridLeftNavTreeNotFoundException;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.services.LeftNavTreeService;

@RunWith(MockitoJUnitRunner.class)
public class HybridLeftNavControllerTest {

    @Mock
    private LeftNavTreeService leftNavTreeService;

    @InjectMocks
    private HybridLeftNavController hybridLeftNavController;

    @Test
    public void shouldReturnLeftNavTreeIfAvailable() {
        when(leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, null)).thenReturn(getTestLeftNavTree());
        LeftNavTree result = hybridLeftNavController.getHybridLeftNavTreeForThisNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, null);
        assertThat(result.getId(), is(NAV_PATH));
    }

    @Test
    public void shouldServe404WhenLeftNavTreeIsUnavailable() {
        NavigationErrorResponse navigationErrorResponse = hybridLeftNavController.handleHybridLeftNavTreeNotFoundException(new HybridLeftNavTreeNotFoundException(NAV_PATH));
        assertThat(navigationErrorResponse.getStatusCode(), equalTo(404));
    }
    
    @Test
    public void shouldReturnLeftNavTreeFromTestSiloIfAvailableForNavKeyGroupB() {
        when(leftNavTreeService.getHybridLeftNavByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B")).thenReturn(getTestLeftNavTree());
        
        LeftNavTree result = hybridLeftNavController.getHybridLeftNavTreeForThisNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B");
        
        verify(leftNavTreeService).getHybridLeftNavByNavPath(NAV_PATH, US_COUNTRY_CODE, SOURCE_TOP_NAV, "B");
        assertThat(result.getId(), is(NAV_PATH));
    }
}
