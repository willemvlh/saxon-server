package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiterImpl;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    @Test
    public void testRateLimiter() throws InterruptedException {
        RateLimiterImpl rl = new RateLimiterImpl(3, 2);
        String myIp = "abc";
        assertTrue(rl.canRequest(myIp));
        for (int i = 0; i < 3; i++) {
            rl.registerRequest(myIp);
        }
        assertFalse(rl.canRequest(myIp));
        Thread.sleep(3000);
        assertTrue(rl.canRequest(myIp));
    }

    @Test
    public void testUntilAllowed() {
        String myIp = "abc";
        RateLimiterImpl rl = new RateLimiterImpl(1, 10);
        assertEquals(Duration.ZERO, rl.timeToAllowed(myIp));
        rl.registerRequest(myIp);
        long until = rl.timeToAllowed(myIp).getSeconds();
        assertTrue(until > 8 && until <= 10);
    }

    @Test
    public void testThrowsException() {
        RateLimiterImpl rl = new RateLimiterImpl(1, 1);
        rl.registerRequest("abc");
        assertThrows(InvalidRequestException.class, () -> rl.registerRequest("abc"));
    }

    @Test
    public void testMultiThreaded() throws ExecutionException, InterruptedException {
        RateLimiterImpl rl = new RateLimiterImpl(1, 1000);
        Thread t1 = new Thread(() -> rl.registerRequest("123"));
        Thread t2 = new Thread(() -> rl.registerRequest("123"));
        ForkJoinPool p = new ForkJoinPool(5);
        p.submit(t1).get();
        assertThrows(ExecutionException.class, () -> p.submit(t2).get());
    }
}