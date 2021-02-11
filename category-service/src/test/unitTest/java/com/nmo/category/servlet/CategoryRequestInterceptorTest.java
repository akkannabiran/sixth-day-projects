package com.sixthday.category.servlet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryRequestInterceptorTest {

    @InjectMocks
    private CategoryRequestInterceptor categoryRequestInterceptor;

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
        categoryRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertThat(MDC.get("TLTSID"), is("TLTSID-VALUE"));
        assertThat(MDC.get("JSESSIONID"), is("JSESSIONID-VALUE"));
        assertThat(MDC.get("DYN_USER_ID"), is("DYN_USER_ID-VALUE"));
        categoryRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        categoryRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyCustomCookiesAreNotCopiedToMDCWhenItsNotAvailableInTheRequest() {
        categoryRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertNull(MDC.get("TLTSID"));
        assertNull(MDC.get("JSESSIONID"));
        assertNull(MDC.get("DYN_USER_ID"));
        categoryRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        categoryRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyTraceIdIsCopiedToMDCWhenItsAvailableInTheRequest() {
        when(httpServletRequest.getHeader("x-sixthday-trace-id")).thenReturn("x-sixthday-trace-id-value");
        categoryRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertThat(MDC.get("TraceId"), is("x-sixthday-trace-id-value"));
        categoryRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        categoryRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }

    @Test
    public void verifyTraceIdIsGeneratedToMDCWhenItsAvailableInTheRequest() {
        categoryRequestInterceptor.preHandle(httpServletRequest, httpServletResponse, object);
        assertNotNull(MDC.get("TraceId"));
        categoryRequestInterceptor.postHandle(httpServletRequest, httpServletResponse, object, new ModelAndView());
        categoryRequestInterceptor.afterCompletion(httpServletRequest, httpServletResponse, object, new Exception());
    }
}