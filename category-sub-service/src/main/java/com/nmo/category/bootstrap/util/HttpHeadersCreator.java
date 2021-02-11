package com.sixthday.category.bootstrap.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.HttpHeaders.COOKIE;
import static org.springframework.http.HttpHeaders.USER_AGENT;

@Service
public class HttpHeadersCreator {

    public static final String NO_CACHE = "no-cache";
    public static final String X_REQUESTED_WITH_KEY = "X-Requested-With";
    public static final String X_REQUESTED_WITH_VALUE = "XMLHttpRequest";
    public static final String TRACE_ID_HEADER_TAG = "x-sixthday-trace-id";

    private HttpServletRequestUtil httpServletRequestUtil;

    @Autowired
    public HttpHeadersCreator(final HttpServletRequestUtil httpServletRequestUtil) {
        this.httpServletRequestUtil = httpServletRequestUtil;
    }

    public HttpHeaders createHeaders() {
        HttpServletRequest httpServletRequest = httpServletRequestUtil.getRequest();
        Map<String, String> cookies = CookieUtils.getCookies(httpServletRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setCacheControl(NO_CACHE);
        httpHeaders.add(X_REQUESTED_WITH_KEY, X_REQUESTED_WITH_VALUE);
        httpHeaders.add(USER_AGENT, httpServletRequest.getHeader(USER_AGENT));
        httpHeaders.add(TRACE_ID_HEADER_TAG, httpServletRequest.getHeader(TRACE_ID_HEADER_TAG));
        cookies.forEach((cookieKey, cookieValue) -> httpHeaders.add(COOKIE, cookieKey + "=" + cookieValue));
        return httpHeaders;
    }
}
