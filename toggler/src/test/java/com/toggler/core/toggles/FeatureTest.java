package com.toggler.core.toggles;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FeatureTest {

    private static final String TOGGLE_ONE = "TOGGLE_ONE";
    private static final String TOGGLE_TWO = "TOGGLE_TWO";
    private FeatureToggleConfig featureToggleConfig = new FeatureToggleConfig();

    @Before
    public void setUp() throws Exception {
        RequestContextHolder.resetRequestAttributes();
        featureToggleConfig.setToggles(new HashMap<String, Boolean>() {{
            put(TOGGLE_ONE, true);
            put(TOGGLE_TWO, false);
        }});
    }

    @After
    public void tearDown() throws Exception {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void shouldReturnToggleStatus() throws Exception {
        assertThat(Feature.isEnabled(TOGGLE_ONE), is(true));
        assertThat(Feature.isEnabled(TOGGLE_TWO), is(false));
    }

    @Test
    public void shouldOverrideFeatureToggleConfigValueWithValueInRequestHeader() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(Feature.X_FEATURE_TOGGLES, "{\"TOGGLE_TWO\": true}");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        assertThat(Feature.isEnabled(TOGGLE_TWO), is(true));
    }

    @Test
    public void shouldNotOverrideFeatureToggleConfigValueWhenValueInRequestHeaderIsInvalid() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(Feature.X_FEATURE_TOGGLES, "\"TOGGLE_TWO\": true");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        assertThat(Feature.isEnabled(TOGGLE_TWO), is(false));
    }
}