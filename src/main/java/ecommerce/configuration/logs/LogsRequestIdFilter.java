package ecommerce.configuration.logs;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter that assigns UUID identifier to every request's log
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogsRequestIdFilter extends OncePerRequestFilter {

    private final static String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var requestId = request.getRequestId();
        final var mdcText = new StringBuilder()
            .append("[requestId=")
            .append(requestId)
            .append("]")
            .toString();

        MDC.put(REQUEST_ID, mdcText);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }

}
