package tv.mediagenix.transformer.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
class LogRequestFilter extends HttpFilter {

    private final Log logger = LogFactory.getLog(LogRequestFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        long time = System.currentTimeMillis();
        logger.info(String.format("Received request for %s from %s", req.getRequestURL(), req.getRemoteAddr()));
        chain.doFilter(request, response);
        logger.info(String.format("Finished request in %s milliseconds (status = %s)", System.currentTimeMillis() - time, res.getStatus()));

    }
}
