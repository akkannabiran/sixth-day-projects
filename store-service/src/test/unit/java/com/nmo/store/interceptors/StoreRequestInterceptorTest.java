package com.sixthday.store.interceptors;

import com.sixthday.store.util.CookieUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.sixthday.store.config.Constants.Logging.*;

import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(CookieUtils.class)
public class StoreRequestInterceptorTest {

    @Mock
    private sixthdayMDCAdapter sixthdayMDCAdapter;

    @Mock
    private HttpServletRequest httpServletRequestMock;

    @Mock
    private HttpServletResponse httpServletResponseMock;

    @InjectMocks
    private StoreRequestInterceptor storeRequestInterceptor;

    @Test
    public void shouldSetMDCWithCookies() throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(JSESSIONID_COOKIE_KEY, "SOME_JSESSIONID");
        headerMap.put(DYN_USER_ID_COOKIE_KEY, "SOME_DYN_USR_ID");
        headerMap.put(TLTSID_COOKIE_KEY, "SOME_TLTSID");

        PowerMockito.mockStatic(CookieUtils.class);
        BDDMockito.given(CookieUtils.getCookies(httpServletRequestMock)).willReturn(headerMap);

        storeRequestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, new Object());

        verify(sixthdayMDCAdapter).put(JSESSIONID_COOKIE_KEY, "SOME_JSESSIONID");
        verify(sixthdayMDCAdapter).put(DYN_USER_ID_COOKIE_KEY, "SOME_DYN_USR_ID");
        verify(sixthdayMDCAdapter).put(TLTSID_COOKIE_KEY, "SOME_TLTSID");
    }

    @Test
    public void shouldSetCookiesWithUnknownValuesIfNotExists() throws Exception {
        PowerMockito.mockStatic(CookieUtils.class);
        BDDMockito.given(CookieUtils.getCookies(httpServletRequestMock)).willReturn(Collections.emptyMap());

        storeRequestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, new Object());

        verify(sixthdayMDCAdapter).put(JSESSIONID_COOKIE_KEY, UNKNOWN);
        verify(sixthdayMDCAdapter).put(DYN_USER_ID_COOKIE_KEY, UNKNOWN);
        verify(sixthdayMDCAdapter).put(TLTSID_COOKIE_KEY, UNKNOWN);
    }

    @Test
    public void shouldSetTraceIdHeaderInMDCIfExists() throws Exception {
        when(httpServletRequestMock.getHeader(TRACE_ID_HEADER_TAG)).thenReturn("SOME_TRACE_ID");

        storeRequestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, new Object());

        verify(httpServletRequestMock).getHeader(TRACE_ID_HEADER_TAG);
        verify(sixthdayMDCAdapter, atLeastOnce()).put(TRACE_ID, "SOME_TRACE_ID");
    }

    @Test
    public void shouldSetNewTraceIdInMCDIfNotExists() throws Exception {
        when(httpServletRequestMock.getHeader(TRACE_ID_HEADER_TAG)).thenReturn(null);

        storeRequestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, new Object());
        verify(sixthdayMDCAdapter, atLeastOnce()).put(eq(TRACE_ID), anyString());
    }

    @Test
    public void shouldClearMDCAfterCompletion() throws Exception {
        storeRequestInterceptor.afterCompletion(httpServletRequestMock, httpServletResponseMock, new Object(), new Exception());

        verify(sixthdayMDCAdapter).clear();
    }

}