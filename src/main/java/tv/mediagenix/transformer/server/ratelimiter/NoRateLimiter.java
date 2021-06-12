package tv.mediagenix.transformer.server.ratelimiter;

import java.time.Duration;

public class NoRateLimiter implements RateLimiter {
    @Override
    public Duration timeToAllowed(String ip) {
        return Duration.ZERO;
    }

    @Override
    public void registerRequest(String ip) {

    }

    @Override
    public boolean canRequest(String ip) {
        return true;
    }

    @Override
    public RateLimiterSettings getSettings() {
        return new RateLimiterSettings(-1, -1);
    }
}
