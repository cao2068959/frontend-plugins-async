package com.github.eirslett.maven.plugins.frontend.mojo;

import java.io.File;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.eirslett.maven.plugins.frontend.lib.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;

import sun.nio.ch.ThreadPool;

public abstract class AbstractFrontendMojo extends AbstractMojo {

    @Component
    protected MojoExecution execution;

    /**
     * Whether you should skip while running in the test phase (default is false)
     */
    @Parameter(property = "skipTests", required = false, defaultValue = "false")
    protected Boolean skipTests;

    /**
     * Set this to true to ignore a failure during testing. Its use is NOT RECOMMENDED, but quite convenient on
     * occasion.
     *
     * @since 1.4
     */
    @Parameter(property = "maven.test.failure.ignore", defaultValue = "false")
    protected boolean testFailureIgnore;

    /**
     * The base directory for running all Node commands. (Usually the directory that contains package.json)
     */
    @Parameter(defaultValue = "${basedir}", property = "workingDirectory", required = false)
    protected File workingDirectory;

    /**
     * The base directory for installing node and npm.
     */
    @Parameter(property = "installDirectory", required = false)
    protected File installDirectory;

    /**
     * Additional environment variables to pass to the build.
     */
    @Parameter
    protected Map<String, String> environmentVariables;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    /**
     * 开启异步
     */
    @Parameter(property = "async",defaultValue = "fasle")
    private boolean async;

    /**
     * 如果被设置了是最后一个任务，当这个任务执行完成后，将会唤醒在主流程上阻塞的线程,然后设置异步任务的结果是成功
     */
    @Parameter(property = "lastTask",defaultValue = "fasle")
    private boolean lastTask;






    /**
     * Determines if this execution should be skipped.
     */
    private boolean skipTestPhase() {
        return skipTests && isTestingPhase();
    }

    /**
     * Determines if the current execution is during a testing phase (e.g., "test" or "integration-test").
     */
    private boolean isTestingPhase() {
        String phase = execution.getLifecyclePhase();
        return "test".equals(phase) || "integration-test".equals(phase);
    }

    protected abstract void execute(FrontendPluginFactory factory) throws FrontendException;

    /**
     * Implemented by children to determine if this execution should be skipped.
     */
    protected abstract boolean skipExecution();

    @Override
    public void execute() throws MojoFailureException {
        if (testFailureIgnore && !isTestingPhase()) {
            getLog().info("testFailureIgnore property is ignored in non test phases");
        }
        if (!(skipTestPhase() || skipExecution())) {
            if (installDirectory == null) {
                installDirectory = workingDirectory;
            }
            try {
                initAsyncTask();
                executeRouter(new FrontendPluginFactory(workingDirectory, installDirectory,
                        new RepositoryCacheResolver(repositorySystemSession)));
            } catch (TaskRunnerException e) {
                if (testFailureIgnore && isTestingPhase()) {
                    getLog().error("There are test failures.\nFailed to run task: " + e.getMessage(), e);
                } else {
                    throw new MojoFailureException("Failed to run task", e);
                }
            } catch (FrontendException e) {
                throw MojoUtils.toMojoFailureException(e);
            }
        } else {
            getLog().info("Skipping execution.");
        }
    }




    /**
     * 如果设置了 asyn=true，并且是第一次进入这个方法才会创建线程池,以及异步任务的结果对象
     * 该线程池是单线程线程池，保证线程池中的任务按照顺序执行
     */
    private void initAsyncTask(){
        if(!async){
            return;
        }

        if(Global.threadPoolExecutor != null && Global.asyncExecuteResult !=null){
            return;
        }

        Global.asyncExecuteResult = new AsyncExecuteResult();
        Global.threadPoolExecutor = new ThreadPoolExecutor(1,1,5, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>());
    }



    protected  void executeRouter(FrontendPluginFactory frontendPluginFactory) throws FrontendException {
        if(!async){
            execute(new FrontendPluginFactory(workingDirectory, installDirectory,
                    new RepositoryCacheResolver(repositorySystemSession)));
            return;
        }

        Global.threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(Global.asyncExecuteResult.isNoStart()){
                        Global.asyncExecuteResult.setStatus(AsyncExecuteResult.STATUS_CONINUE);
                    }
                    execute(new FrontendPluginFactory(workingDirectory, installDirectory,
                            new RepositoryCacheResolver(repositorySystemSession)));

                    if(lastTask){
                        synchronized (Global.asyncExecuteResult){
                            Global.asyncExecuteResult.setStatus(AsyncExecuteResult.STATUS_FINISH);
                            Global.asyncExecuteResult.setSuccess(true);
                            Global.asyncExecuteResult.notifyAll();
                        }
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                    Global.threadPoolExecutor.shutdownNow();
                    synchronized (Global.asyncExecuteResult){
                        Global.asyncExecuteResult.setStatus(AsyncExecuteResult.STATUS_EXCEPTION);
                        Global.asyncExecuteResult.setSuccess(false);
                        Global.asyncExecuteResult.setExceptionMsg(e.getMessage());
                        Global.asyncExecuteResult.setThrowable(e.getCause());
                        Global.asyncExecuteResult.notifyAll();
                    }
                }
            }
        });

    }






}
