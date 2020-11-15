package tv.mediagenix.xslt.transformer.server.ratelimiter;

import tv.mediagenix.xslt.transformer.server.InvalidRequestException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RateLimiterImpl implements RateLimiter {

    private final RateLimiterSettings settings;
    private final Map<String, Deque<LocalDateTime>> requestMap;

    public RateLimiterImpl(RateLimiterSettings settings) {
        if (settings.getMaxNumberOfRequests() < 1 || settings.getSeconds() < 1) {
            throw new IllegalArgumentException("Seconds and maxNumberRequests must be positive integers");
        }
        this.settings = settings;
        requestMap = new HashMap<>();
    }

    public RateLimiterImpl(int seconds, int maxNumberOfRequests) {
        this(new RateLimiterSettings(seconds, maxNumberOfRequests));
    }

    private boolean hasSeenIp(String ip) {
        return requestMap.containsKey(ip);
    }

    public boolean canRequest(String ip) {
        if (requestMap.containsKey(ip)) {
            Deque<LocalDateTime> requests = requestMap.get(ip);
            //if max number of requests has not been reached, request is allowed
            if (requests.size() < settings.getMaxNumberOfRequests()) {
                return true;
            }
            //else, if the last request was longer ago than the specified duration, it is also allowed.
            Duration d = Duration.between(requests.getLast(), LocalDateTime.now());
            return d.getSeconds() > settings.getSeconds();
        }
        return true;
    }

    public Duration timeToAllowed(String ip) {
        if (!(hasSeenIp(ip)) || canRequest(ip)) {
            return Duration.ZERO;
        }
        Duration sinceLast = Duration.between(requestMap.get(ip).getLast(), LocalDateTime.now());
        return Duration.ofSeconds(settings.getSeconds() - sinceLast.getSeconds());
    }

    public synchronized void registerRequest(String ip) throws IllegalStateException {
        //first check if request is allowed - if not, throw exception.
        if (!canRequest(ip)) {
            throw new InvalidRequestException("Request not allowed by rate limiter");
        }
        if (!hasSeenIp(ip)) {
            //if the ip is unknown, add a new entry and register request
            Deque<LocalDateTime> requests = new ArrayDeque<>();
            requests.add(LocalDateTime.now());
            requestMap.put(ip, requests);
        } else {
            //if the number of requests == max number of requests, remove the last.
            //then add new request to the top.
            Deque<LocalDateTime> requests = requestMap.get(ip);
            if (requests.size() == settings.getMaxNumberOfRequests()) {
                requests.removeLast();
            }
            requests.addFirst(LocalDateTime.now());
        }

    }

    public RateLimiterSettings getSettings() {
        return this.settings;
    }
}

