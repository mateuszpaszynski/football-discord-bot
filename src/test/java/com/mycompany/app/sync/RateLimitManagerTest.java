package com.mycompany.app.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitManagerTest {
    
    private static class TestableRateLimitManager extends RateLimitManager {
        long currTime = 100000;
        long totalSleepTime = 0;

        @Override
        protected long getCurrentTime() {
            return currTime;
        }

        @Override
        protected void sleep(long ms) {
            totalSleepTime += ms;
            currTime += ms;
        }
    }
    private TestableRateLimitManager rateLimitManager;
    
    @BeforeEach
    void setUp() {
        this.rateLimitManager = new TestableRateLimitManager();
    }
    @Test
    void shouldNotSleepForFirst10Requests() {
        for (int i = 0; i < 10; i++) {
            rateLimitManager.apply();
        }
        assertThat(rateLimitManager.totalSleepTime).isEqualTo(0L);
    }

    @Test
    void shouldSleepOn11thRequest() {
        for (int i = 0; i < 10; i++) {
            rateLimitManager.apply();
        }
        rateLimitManager.currTime += 11000;
        rateLimitManager.apply();

        assertThat(rateLimitManager.totalSleepTime).isEqualTo(50000L); // 61k - 11k we test arithmetic
    }
    @Test
    void shouldResetCounterAfter61Seconds() {
        for (int i = 0; i < 10; i++) {
            rateLimitManager.apply();
        }
        rateLimitManager.currTime += 62000;
        for (int i = 0; i < 10; i++) {
            rateLimitManager.apply();
        }
        assertThat(rateLimitManager.totalSleepTime).isEqualTo(0L);
    }
}
