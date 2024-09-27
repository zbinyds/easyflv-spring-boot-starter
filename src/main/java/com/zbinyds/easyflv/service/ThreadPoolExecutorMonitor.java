package com.zbinyds.easyflv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池监视器
 *
 * @Author zbinyds
 * @Create 2024-09-27 09:21
 */
public class ThreadPoolExecutorMonitor extends ThreadPoolExecutor {
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecutorMonitor.class);
    private static final InheritableThreadLocal<Long> EXECUTE_TIMER_MONITOR = new InheritableThreadLocal<>();
    private final String poolName;

    public ThreadPoolExecutorMonitor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue, ThreadFactoryMonitor threadFactory,
                                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = threadFactory.getNamePrefix();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        long cost = System.currentTimeMillis() - EXECUTE_TIMER_MONITOR.get();
        log.info("[{}]: " +
                        "任务耗时: {} ms, 当前线程数: {}, 核心线程数: {}, 执行的任务数量: {}, " +
                        "已完成任务数量: {}, 任务总数: {}, 队列里缓存的任务数量: {}, 池中存在的最大线程数: {}, " +
                        "最大允许的线程数: {},  线程空闲时间: {}, 线程池是否关闭: {}, 线程池是否终止: {}",
                this.poolName,
                cost, this.getPoolSize(), this.getCorePoolSize(), this.getActiveCount(),
                this.getCompletedTaskCount(), this.getTaskCount(), this.getQueue().size(), this.getLargestPoolSize(),
                this.getMaximumPoolSize(), this.getKeepAliveTime(TimeUnit.MILLISECONDS), this.isShutdown(), this.isTerminated());
        EXECUTE_TIMER_MONITOR.remove();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        EXECUTE_TIMER_MONITOR.set(System.currentTimeMillis());
    }

    @Override
    public List<Runnable> shutdownNow() {
        log.info("[{}] 立即关闭线程池，已执行任务: {}, 正在执行任务: {}, 未执行任务数量: {}",
                this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        return super.shutdownNow();
    }

    @Override
    public void shutdown() {
        log.info("[{}] 关闭线程池，已执行任务: {}, 正在执行任务: {}, 未执行任务数量: {}",
                this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        super.shutdown();
    }

    @SuppressWarnings("unused")
    public static ThreadFactoryMonitor monitorThreadFactory() {
        return ThreadPoolExecutorMonitor.monitorThreadFactory(null);
    }

    public static ThreadFactoryMonitor monitorThreadFactory(String poolName) {
        if (!StringUtils.hasText(poolName)) {
            log.warn("poolName is null, use default name: {}", poolName);
            poolName = "monitor";
        }
        return new ThreadFactoryMonitor(poolName);
    }

    private static class ThreadFactoryMonitor implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ThreadFactoryMonitor(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "-" + poolName + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

        public String getNamePrefix() {
            return namePrefix;
        }
    }
}
