package ecommerce.configuration.logs;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class LogsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String method = request.getMethod();
        final String uri = request.getRequestURI();

        log.debug("started processing request [method={} uri={}]", method, uri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            final int status = response.getStatus();
            log.debug("finished processing request [status={}]", status);
        }
    }

}
