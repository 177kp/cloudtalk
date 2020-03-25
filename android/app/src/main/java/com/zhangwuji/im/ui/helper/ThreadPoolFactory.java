package com.zhangwuji.im.ui.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFactory {

    private static ThreadPoolFactory threadPoolFactory;
    private ExecutorService threadPool;

    public static ThreadPoolFactory getInstance() {
        if (threadPoolFactory == null) {
            threadPoolFactory = new ThreadPoolFactory();
        }
        return threadPoolFactory;
    }

    private ThreadPoolFactory() {
        threadPool = Executors.newCachedThreadPool();
    }

    public void execute(Runnable run) {
        threadPool.submit(run);
    }
}
