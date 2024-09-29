package io.vepo.maestro.framework.parallel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThreadFactory implements ThreadFactory {
    private static final Logger logger = LoggerFactory.getLogger(WorkerThreadFactory.class);

    private final AtomicInteger threadCounter;
    private final String prefix;

    public WorkerThreadFactory(String prefix) {
        this.prefix = prefix;
        threadCounter = new AtomicInteger();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, String.format("%s-%d", prefix, threadCounter.incrementAndGet()));

        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("Uncaught exception in thread: " + t.getName(), e);
            }
        });

        return t;
    }

}
