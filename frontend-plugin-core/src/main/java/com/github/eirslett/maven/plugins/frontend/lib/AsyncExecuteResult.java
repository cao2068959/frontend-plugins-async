package com.github.eirslett.maven.plugins.frontend.lib;


import org.apache.commons.logging.Log;

import java.util.List;

import java.util.ArrayList;
import java.util.logging.Logger;

public class AsyncExecuteResult extends Exception{


    public final static String STATUS_FINISH = "finish";
    public final static String STATUS_NO_START = "noStart";
    public final static String STATUS_CONINUE = "continuet";
    public final static String STATUS_EXCEPTION = "exception";



    private volatile String status = STATUS_NO_START;

    private volatile boolean success = false;

    private volatile String exceptionMsg;

    private Throwable throwable;

    /**
     * 用了打印错误的原因，每一个元素是一行
     */
    private List<String> errorline = new ArrayList<String>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    /**
     * 判断是否是第一次启动还未开始
     */
    public boolean isNoStart(){
        return AsyncExecuteResult.STATUS_NO_START.equals(status);
    }


    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void addError(String line){
        errorline.add(line);
    }

    public List<String> getErrorline() {
        return errorline;
    }

    public void setErrorline(List<String> errorline) {
        this.errorline = errorline;
    }

    @Override
    public String toString() {
        return "AsyncExecuteResult{" +
                "status='" + status + '\'' +
                ", success=" + success +
                ", exceptionMsg='" + exceptionMsg + '\'' +
                '}';
    }
}
