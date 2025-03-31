package za.gov.sars.amakhwezi.config;

import java.io.IOException;
import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.gov.sars.amakhwezi.mb.ActiveUser;

//@WebFilter(filterName = "/*", urlPatterns = {"*.xhtml"})
public class AuthenticationFilter implements Filter {
    //   private static final String LOGIN_PAGE = "/login.xhtml";

    private static final String LOGIN_PAGE = "/login.html";

    @Override
    public void init(FilterConfig fc) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000");
        }
        response.setHeader("X-Frame-Options", "DENY");

        HttpSession session = request.getSession(false);

        ActiveUser activeUser = (session != null) ? (ActiveUser) session.getAttribute("activeUser") : null;
        boolean loggedIn = activeUser != null && activeUser.isUserLoginIndicator();

        //Login page url
        StringBuilder loginFilterUrl = new StringBuilder(request.getContextPath());
        loginFilterUrl.append(LOGIN_PAGE);
        boolean loginRequest = request.getRequestURI().contains(loginFilterUrl.toString());

        boolean resourceRequest = request.getRequestURI().contains(request.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER);

        if (loggedIn || resourceRequest || loginRequest) {
            //Prevent restricted pages from being cached.
            if (!resourceRequest) {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                response.setDateHeader("Expires", 0); // Proxies.
            }
            filterChain.doFilter(request, response);
        } else {
            StringBuilder builder = new StringBuilder(request.getContextPath());
            builder.append(LOGIN_PAGE);
            response.sendRedirect(builder.toString());
        }
    }

    @Override
    public void destroy() {

    }
}
