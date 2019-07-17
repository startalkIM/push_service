package com.qunar.qchat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ExecutorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtils.class);

    private volatile static ExecutorService executorService;

    private static ExecutorService newLimitedCachedThreadPool(int activeNum, int queueNum) {
        if(executorService == null) {
            synchronized (ExecutorUtils.class) {
                if(executorService == null) {
                    executorService = new ThreadPoolExecutor(activeNum, activeNum, 60L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(queueNum), new ThreadPoolExecutor.DiscardOldestPolicy() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                            super.rejectedExecution(r, e);
                            LOGGER.warn("线程池满了，执行拒绝策略 t.count={}", e.getQueue().size());
                        }
                    });//拒绝策略，抛弃最老的任务
                }
            }
        }
        return executorService;
    }

    public static ExecutorService newLimitedCachedThreadPool() {
        return newLimitedCachedThreadPool(1000, 1000);
    }
}
