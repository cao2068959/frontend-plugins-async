package com.github.eirslett.maven.plugins.frontend.lib;

public class TaskRunnerException extends FrontendException {
    TaskRunnerException(String message) {
        super(message);
    }

    public TaskRunnerException(String message, Throwable cause){
        super(message, cause);
    }
}
