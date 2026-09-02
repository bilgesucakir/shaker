package com.shaker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * CSRF wiring for a cookie-based SPA, following the Spring Security reference guide
 * ("Integrating with a JavaScript frontend").
 *
 * <p>The server writes an {@code XSRF-TOKEN} cookie (readable by JS); the SPA echoes it
 * back in the {@code X-XSRF-TOKEN} header on mutating requests.
 */
public final class SpaCsrfConfig {

    private SpaCsrfConfig() {
    }

    /**
     * Ensures the {@code XSRF-TOKEN} cookie is actually rendered on responses. Spring defers
     * token creation until the token is read, so we force a read on every request.
     */
    public static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Resolves the raw token from the request header (SPA case) or falls back to the
     * BREACH-protected handling for form posts.
     */
    public static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestAttributeHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           Supplier<CsrfToken> csrfToken) {
            this.xor.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                return this.plain.resolveCsrfTokenValue(request, csrfToken);
            }
            return this.xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
