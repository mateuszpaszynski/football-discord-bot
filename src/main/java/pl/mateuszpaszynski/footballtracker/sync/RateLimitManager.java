package pl.mateuszpaszynski.footballtracker.sync;

import org.springframework.stereotype.Component;

@Component
public class RateLimitManager {
    
    private int requestCount = 0;
    private long windowStartTime = 0;
    private static final int BURST_LIMIT = 10;
    private final static long WINDOW_MS = 61000;

    public synchronized void apply() {
        
        long now = getCurrentTime();
        
        if (now - windowStartTime > WINDOW_MS) {
            requestCount = 0;
            windowStartTime = now;
        }

        requestCount++;

        if (requestCount > BURST_LIMIT) {
            long timeToWait = WINDOW_MS - (now - windowStartTime);
            if (timeToWait > 0) {
                try {
                    sleep(timeToWait);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            requestCount = 1;
            windowStartTime = getCurrentTime();
        }
    }
    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }
    protected void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
