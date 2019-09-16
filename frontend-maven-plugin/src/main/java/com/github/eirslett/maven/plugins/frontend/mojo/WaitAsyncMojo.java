package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.AsyncExecuteResult;
import com.github.eirslett.maven.plugins.frontend.lib.FrontendException;
import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static com.github.eirslett.maven.plugins.frontend.lib.Global.asyncExecuteResult;

@Mojo(name="waitAsync",  defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class WaitAsyncMojo extends AbstractFrontendMojo{


    @Parameter(property = "skip.jspm", defaultValue = "${skip.jspm}")
    private boolean skip;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }


    @Override
    protected void execute(FrontendPluginFactory factory) throws TaskRunnerException {
        if(asyncExecuteResult == null){
            return;
        }

        getLog().info("------------------> 开始判断异步任务是否完成");

        if(asyncHandle()){
            return;
        }
        await();
    }

    private void await() throws TaskRunnerException {

        synchronized (asyncExecuteResult){
            //最后再给你一次机会
            if(asyncHandle()){
                return;
            }
            try {
                asyncExecuteResult.wait();
                getLog().info("------------------> 唤醒主流程");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(asyncHandle()){
                return;
            }
        }
    }


    private boolean asyncHandle() throws TaskRunnerException {

        getLog().info(asyncExecuteResult.toString());

        //异步任务异常，直接终止整个maven生命周期
        if(AsyncExecuteResult.STATUS_EXCEPTION.equals(asyncExecuteResult.getStatus())
                && !asyncExecuteResult.isSuccess()){
            List<String> errorline = asyncExecuteResult.getErrorline();
            for (String line : errorline) {
                getLog().error(line);
            }
            throw new TaskRunnerException("前段打包异步任务失败 ",asyncExecuteResult.getCause());
        }

        //进这里面说明已经正确执行完异步任务
        if(AsyncExecuteResult.STATUS_FINISH.equals(asyncExecuteResult.getStatus())
                && asyncExecuteResult.isSuccess()){
            getLog().info("------------------>异步任务执行结束");
            return true;
        }

        return false;
    }


}
