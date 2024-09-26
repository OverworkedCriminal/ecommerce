package ecommerce.configuration.logs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LogsConfiguration
{
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        final var filter = new CommonsRequestLoggingFilter();
        filter.setBeforeMessagePrefix("started processing request [");
        filter.setAfterMessagePrefix("finished processing request [");
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        return filter;
    }
}
