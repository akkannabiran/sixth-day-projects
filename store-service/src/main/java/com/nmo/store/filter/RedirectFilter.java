package com.sixthday.store.filter;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RedirectFilter implements Filter {

    public static final String OPTIONAL_CONTEXT_PATH_PREFIX = "/stores/";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.startsWith(OPTIONAL_CONTEXT_PATH_PREFIX)) {
            String newURI = requestURI.replaceFirst(OPTIONAL_CONTEXT_PATH_PREFIX, "/");
            req.getRequestDispatcher(newURI).forward(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
}
