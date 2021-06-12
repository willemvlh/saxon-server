package tv.mediagenix.transformer.server.ratelimiter;

import java.util.Objects;

public class RateLimiterSettings {
    private final int seconds;
    private final int maxNumberOfRequests;

    public RateLimiterSettings(int seconds, int maxNumberOfRequests) {
        this.seconds = seconds;
        this.maxNumberOfRequests = maxNumberOfRequests;
    }

    public int getMaxNumberOfRequests() {
        return maxNumberOfRequests;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimiterSettings that = (RateLimiterSettings) o;
        return seconds == that.seconds && maxNumberOfRequests == that.maxNumberOfRequests;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seconds, maxNumberOfRequests);
    }
}
