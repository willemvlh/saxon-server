package tv.mediagenix.xslt.transformer.server.ratelimiter;

import java.time.Duration;

public interface RateLimiter {
    Duration timeToAllowed(String ip);

    void registerRequest(String ip);

    boolean canRequest(String ip);

    RateLimiterSettings getSettings();
}
