package com.github.linsea.clue;

import android.util.Log;


/**
 * Base Log class for log receiver client.
 */
public abstract class BaseLog {

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See {@link Log} for constants.
     * @param tag Explicit or inferred tag. May be {@code null}.
     * @param message Formatted log message. May be {@code null}, but then {@code t} will not be.
     * @param t Accompanying exceptions. May be {@code null}, but then {@code message} will not be.
     * @param javaFilename java source file name the log call occurs
     * @param methodName method name enclosing the log call
     * @param lineNumber line number the log call from the java source file
     * @param threadName thread name which the log call occurs at
     */
    protected void log(int priority, String tag, String message, Throwable t,
                       String javaFilename, String methodName, int lineNumber, String threadName){
        log(priority, tag, message);
    }

    protected void log(int priority, String tag, String message){

    }


    /** Return whether a message at {@code priority} should be logged. */
    protected boolean isLoggable(int priority) {
        return true;
    }

}