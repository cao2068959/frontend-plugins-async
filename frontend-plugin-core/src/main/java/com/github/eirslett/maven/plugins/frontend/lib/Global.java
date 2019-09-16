package com.github.eirslett.maven.plugins.frontend.lib;


import java.util.concurrent.ThreadPoolExecutor;

public class Global {

    /**
     * 执行异步任务的线程池，这里用的是单线程的线程池，保证执行任务的顺序
     */
    public static volatile ThreadPoolExecutor threadPoolExecutor;

    /**
     * 异步任务的执行状态
     */
    public static volatile AsyncExecuteResult asyncExecuteResult;

}
