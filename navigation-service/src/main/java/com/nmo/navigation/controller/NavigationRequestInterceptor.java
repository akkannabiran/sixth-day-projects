package com.sixthday.navigation.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.*;
import java.util.UUID;

@Component
@Slf4j
public class NavigationRequestInterceptor implements HandlerInterceptor {

    private static final String JSESSIONID = "JSESSIONID";
    private static final String DYN_USER_ID = "DYN_USER_ID";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String HEADER_TRACE_ID = "x-sixthday-trace-id";
    private static final String MDC_TRACE_ID = "TraceId";
    private static final String TLTSID = "TLTSID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String traceId = request.getHeader(HEADER_TRACE_ID);
        final Cookie jSessionId = WebUtils.getCookie(request, JSESSIONID);
        final Cookie dynUserId = WebUtils.getCookie(request, DYN_USER_ID);
        final Cookie tltsid = WebUtils.getCookie(request, TLTSID);

        MDC.put(JSESSIONID, ObjectUtils.isEmpty(jSessionId) ? UNKNOWN : jSessionId.getValue());
        MDC.put(DYN_USER_ID, ObjectUtils.isEmpty(dynUserId) ? UNKNOWN : dynUserId.getValue());
        MDC.put(TLTSID, ObjectUtils.isEmpty(tltsid) ? UNKNOWN : tltsid.getValue());
        MDC.put(MDC_TRACE_ID, ObjectUtils.isEmpty(traceId) ? UUID.randomUUID().toString() : traceId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        log.debug("Nothing to handle in post");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}