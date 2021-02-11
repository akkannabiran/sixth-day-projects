package com.sixthday.store.interceptors;

import static com.sixthday.store.config.Constants.Logging.*;

import com.sixthday.store.util.CookieUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class StoreRequestInterceptor implements HandlerInterceptor {

    private final sixthdayMDCAdapter sixthdayMDCAdapter;

    @Autowired
    public StoreRequestInterceptor(sixthdayMDCAdapter sixthdayMDCAdapter) {
        this.sixthdayMDCAdapter = sixthdayMDCAdapter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, String> cookieMap = CookieUtils.getCookies(request);

        sixthdayMDCAdapter.put(JSESSIONID_COOKIE_KEY, cookieMap.getOrDefault(JSESSIONID_COOKIE_KEY, UNKNOWN));
        sixthdayMDCAdapter.put(DYN_USER_ID_COOKIE_KEY, cookieMap.getOrDefault(DYN_USER_ID_COOKIE_KEY, UNKNOWN));
        sixthdayMDCAdapter.put(TLTSID_COOKIE_KEY, cookieMap.getOrDefault(TLTSID_COOKIE_KEY, UNKNOWN));

        Optional<String> traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER_TAG));
        sixthdayMDCAdapter.put(TRACE_ID, traceId.orElse(UUID.randomUUID().toString()));

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        sixthdayMDCAdapter.clear();
    }
}
