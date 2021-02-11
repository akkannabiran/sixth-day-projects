package com.sixthday.navigation.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NavigationRequestInterceptorTest {

    @InjectMocks
    private NavigationRequestInterceptor navigationRequestInterceptor;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private Object object;

    @Test
    public void verifyCustomCookiesAreCopiedToMDCWhenItsAvailableInTheRequest() {
        Cookie cookies[] = {new Cookie("TLTSID", "TLTSID-VALUE"), new Cookie("JSESSIONID", "JSESSIONID-VALUE"), new Cookie("DYN_USER_ID", "DYN_USER_ID-VALUE")};

        when(httpServletRequest.getCookies()).thenReturn(cookies);
        navigationRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertThat(MDC.get("TLTSID"), is("TLTSID-VALUE"));
        assertThat(MDC.get("JSESSIONID"), is("JSESSIONID-VALUE"));
        assertThat(MDC.get("DYN_USER_ID"), is("DYN_USER_ID-VALUE"));
        navigationRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        navigationRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyCustomCookiesAreReturningUnknownToMDCWhenItsNotAvailableInTheRequest() {
        navigationRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertThat(MDC.get("TLTSID"), is("UNKNOWN"));
        assertThat(MDC.get("JSESSIONID"), is("UNKNOWN"));
        assertThat(MDC.get("DYN_USER_ID"), is("UNKNOWN"));
        navigationRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        navigationRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyTraceIdIsCopiedToMDCWhenItsAvailableInTheRequest() {
        when(httpServletRequest.getHeader("x-sixthday-trace-id")).thenReturn("x-sixthday-trace-id-value");
        navigationRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertThat(MDC.get("TraceId"), is("x-sixthday-trace-id-value"));
        navigationRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        navigationRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyTraceIdIsGeneratedToMDCWhenItsAvailableInTheRequest() {
        navigationRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertNotNull(MDC.get("TraceId"));
        navigationRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        navigationRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }
}