package com.sixthday.category.servlet;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.*;
import java.util.UUID;

@Component
@Slf4j
public class CategoryRequestInterceptor implements HandlerInterceptor {

    private static final String HEADER_TRACE_ID = "x-sixthday-trace-id";
    private static final String MDC_TRACE_ID = "TraceId";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String DYN_USER_ID = "DYN_USER_ID";
    private static final String TLTSID = "TLTSID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String traceId = request.getHeader(HEADER_TRACE_ID);
        final Cookie jSessionId = WebUtils.getCookie(request, JSESSIONID);
        final Cookie dynUserId = WebUtils.getCookie(request, DYN_USER_ID);
        final Cookie tltsid = WebUtils.getCookie(request, TLTSID);

        if (null != jSessionId) {
            MDC.put(JSESSIONID, jSessionId.getValue());
        }
        if (null != dynUserId) {
            MDC.put(DYN_USER_ID, dynUserId.getValue());
        }
        if (null != tltsid) {
            MDC.put(TLTSID, tltsid.getValue());
        }

        log.debug("Checking for 'X-sixthday-Trace-Id' header");
        if (null != traceId) {
            MDC.put(MDC_TRACE_ID, traceId);
            log.debug("Trace id found");
        } else {
            MDC.put(MDC_TRACE_ID, UUID.randomUUID().toString());
            log.debug("Trace id generated");
        }
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
