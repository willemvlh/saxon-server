package tv.mediagenix.xslt.transformer.server.ratelimiter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RateLimiterImpl implements RateLimiter {

    private final int maxNumberRequests;
    private final int seconds;
    private final Map<String, Deque<LocalDateTime>> requestMap;

    public RateLimiterImpl(int maxNumberRequests, int seconds) {
        if (seconds < 1 || maxNumberRequests < 1) {
            throw new IllegalArgumentException("Seconds and maxNumberRequests must be positive integers");
        }
        this.maxNumberRequests = maxNumberRequests;
        this.seconds = seconds;
        requestMap = new HashMap<>();
    }

    private boolean hasSeenIp(String ip) {
        return requestMap.containsKey(ip);
    }

    public boolean canRequest(String ip) {
        if (requestMap.containsKey(ip)) {
            Deque<LocalDateTime> requests = requestMap.get(ip);
            //if max number of requests has not been reached, request is allowed
            if (requests.size() < maxNumberRequests) {
                return true;
            }
            //else, if the last request was longer ago than the specified duration, it is also allowed.
            Duration d = Duration.between(requests.getLast(), LocalDateTime.now());
            return d.getSeconds() > seconds;
        }
        return true;
    }

    public Duration timeToAllowed(String ip) {
        if (!(hasSeenIp(ip)) || canRequest(ip)) {
            return Duration.ZERO;
        }
        Duration sinceLast = Duration.between(requestMap.get(ip).getLast(), LocalDateTime.now());
        return Duration.ofSeconds(seconds - sinceLast.getSeconds());
    }

    public synchronized void registerRequest(String ip) throws IllegalStateException {
        //first check if request is allowed - if not, throw exception.
        if (!canRequest(ip)) {
            throw new IllegalStateException("Request not allowed by rate limiter");
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
            if (requests.size() == maxNumberRequests) {
                requests.removeLast();
            }
            requests.addFirst(LocalDateTime.now());
        }

    }
}

