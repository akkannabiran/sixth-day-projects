package com.sixthday.store.filter;


import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.sixthday.store.hooks.RequestContextHystrixRequestVariable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.*;
import java.io.IOException;

@Component
@SuppressWarnings("unused")
public class HystrixRequestContextEnablerFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        @SuppressWarnings("squid:S2095")
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        RequestContextHystrixRequestVariable.getInstance().set(RequestContextHolder.getRequestAttributes());
        try {
            chain.doFilter(request, response);
        } finally {
            context.shutdown();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
