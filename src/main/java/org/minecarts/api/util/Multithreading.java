/**
 * Copyright (c) 2018 - ProjectMinecarts
 * Copyright (c) 2018-2021 by Isaiah
 */
package org.minecarts.api.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreading {

    public static final ExecutorService POOL = Executors.newFixedThreadPool(100, new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("MinecartThread %s", counter.incrementAndGet()));
        }
    });

    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(10, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MinecartThread " + counter.incrementAndGet());
        }
    });

    public static void schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static void schedule(Runnable r, long delay, TimeUnit unit) {
        RUNNABLE_POOL.schedule(r, delay, unit);
    }

    /**
     * Exucutes the runnable async
     * 
     * @param runnable
     */
    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }

    /**
     * Get the total number of currently running Threads
     */
    public static int getTotal() {
        return ((ThreadPoolExecutor) POOL).getActiveCount();
    }

}