package com.toggler.core.utils;

import com.toggler.core.toggles.Feature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestUtilsTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void shouldReturnAFeatureTogglesMapGivenValidFeatureTogglesHeader() throws Exception {
        when(httpServletRequest.getHeader(Feature.X_FEATURE_TOGGLES))
                .thenReturn("{\"TOGGLE_A\": true, \"TOGGLE_B\": \"false\"}");

        Map<String, Boolean> toggles = RequestUtils.getToggles(httpServletRequest);

        assertThat(toggles.get("TOGGLE_A"), is(true));
        assertThat(toggles.get("TOGGLE_B"), is(false));
    }

    @Test
    public void shouldReturnEmptyFeatureTogglesMapGivenInValidFeatureTogglesHeader() throws Exception {
        when(httpServletRequest.getHeader(Feature.X_FEATURE_TOGGLES))
                .thenReturn("{\"TOGGLE_A\": true, \"TOGGLE_B\": false");

        Map<String, Boolean> toggles = RequestUtils.getToggles(httpServletRequest);

        assertThat(toggles.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnEmptyFeatureTogglesMapGivenNullValueInFeatureTogglesHeader() throws Exception {
        when(httpServletRequest.getHeader(Feature.X_FEATURE_TOGGLES))
                .thenReturn(null);

        Map<String, Boolean> toggles = RequestUtils.getToggles(httpServletRequest);

        assertThat(toggles.isEmpty(), is(true));
    }
}