package com.github.linsea.clue;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * High Performance Log with detail info(line number, method name, source file name, thread name)
 */
public class Clue {

    private static final List<BaseLog> receiverList = new ArrayList<BaseLog>();
    static volatile BaseLog[] receivers;

    private Clue() {
    }

    /** Add a new log receiver. */
    public static void addLog(BaseLog log) {
        if (log == null) {
            throw new NullPointerException("log == null");
        }
        synchronized (receiverList) {
            receiverList.add(log);
            receivers = receiverList.toArray(new BaseLog[receiverList.size()]);
        }
    }

    public static void v(@NonNull String message, Object... args) {
        log(message, args, Log.VERBOSE, Object.class, "methodName", 0);
    }

    public static void vt(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.VERBOSE, Object.class, "methodName", 0);
    }

    public static void v(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.VERBOSE, Object.class, "methodName", 0);
    }

    public static void vt(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.VERBOSE, Object.class, "methodName", 0);
    }

    public static void d(@NonNull String message, Object... args) {
        log(message, args, Log.DEBUG, Object.class, "methodName", 0);
    }

    public static void dt(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.DEBUG, Object.class, "methodName", 0);
    }

    public static void d(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.DEBUG, Object.class, "methodName", 0);
    }

    public static void dt(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.DEBUG, Object.class, "methodName", 0);
    }

    public static void i(@NonNull String message, Object... args) {
        log(message, args, Log.INFO, Object.class, "methodName", 0);
    }

    public static void it(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.INFO, Object.class, "methodName", 0);
    }

    public static void i(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.INFO, Object.class, "methodName", 0);
    }

    public static void it(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.INFO, Object.class, "methodName", 0);
    }

    public static void w(@NonNull String message, Object... args) {
        log(message, args, Log.WARN, Object.class, "methodName", 0);
    }

    public static void wt(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.WARN, Object.class, "methodName", 0);
    }

    public static void w(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.WARN, Object.class, "methodName", 0);
    }

    public static void wt(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.WARN, Object.class, "methodName", 0);
    }

    public static void e(@NonNull String message, Object... args) {
        log(message, args, Log.ERROR, Object.class, "methodName", 0);
    }

    public static void et(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.ERROR, Object.class, "methodName", 0);
    }

    public static void e(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.ERROR, Object.class, "methodName", 0);
    }

    public static void et(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.ERROR, Object.class, "methodName", 0);
    }

    public static void wtf(@NonNull String message, Object... args) {
        log(message, args, Log.ASSERT, Object.class, "methodName", 0);
    }

    public static void wtft(String tag, @NonNull String message, Object... args) {
        log(tag, message, args, Log.ASSERT, Object.class, "methodName", 0);
    }

    public static void wtf(Throwable t, @NonNull String message, Object... args) {
        log(t, message, args, Log.ASSERT, Object.class, "methodName", 0);
    }

    public static void wtft(String tag, Throwable t, @NonNull String message, Object... args) {
        log(tag, t, message, args, Log.ASSERT, Object.class, "methodName", 0);
    }

    private static void log(String message, @NonNull Object[] args,
                                    int priority, Class javaFilename, String methodName, int lineNumber) {
        internalLog(null, message, args, null, priority, javaFilename, methodName, lineNumber);
    }

    private static void log(String tag, String message, @NonNull Object[] args,
                                    int priority, Class javaFilename, String methodName, int lineNumber) {
        internalLog(tag, message, args, null, priority, javaFilename, methodName, lineNumber);

    }

    private static void log(Throwable t, String message, @NonNull Object[] args,
                                    int priority, Class javaFilename, String methodName, int lineNumber) {
        internalLog(null, message, args, t, priority, javaFilename, methodName, lineNumber);
    }

    private static void log(String tag, Throwable t, String message, @NonNull Object[] args,
                                    int priority, Class javaFilename, String methodName, int lineNumber) {
        internalLog(tag, message, args, t, priority, javaFilename, methodName, lineNumber);
    }

    private static void internalLog(String tag, String message, Object[] args, Throwable t,
                                    int priority, Class javaFilename, String methodName, int lineNumber) {
        String cachedThreadName = null;
        String cachedJavaFilename = null;
        String cacheTag = null;
        String cacheMessage = null;

        for (int i = 0, j = receivers.length; i < j; i++) {
            if (receivers[i].isLoggable(priority)) {
                if (cachedThreadName == null) {
                    cachedThreadName = Thread.currentThread().getName();
                }
                if (cachedJavaFilename == null) {
                    cachedJavaFilename = javaFilename.getSimpleName();
                }
                if(cacheTag == null){
                    cacheTag = tag;
                    if (cacheTag == null) {
                        cacheTag = cachedJavaFilename;
                    }
                }
                if (cacheMessage == null) {
                    if (args.length > 0) {
                        cacheMessage = String.format(message, args);
                    } else {
                        cacheMessage = message;
                    }

                    StringBuilder sb = new StringBuilder(cacheMessage);
                    sb.append(" [ Thread:").append(cachedThreadName);
                    sb.append(" ").append(methodName).append("() ");
                    sb.append("at(").append(cachedJavaFilename).append(".java");
                    sb.append(":").append(lineNumber).append(") ]");

                    if (t != null) {
                        sb.append("\n").append(getStackTraceString(t));
                    }
                    cacheMessage = sb.toString();
                }
                receivers[i].log(priority, cacheTag, cacheMessage, t, cachedJavaFilename, cachedThreadName, lineNumber, cachedThreadName);
            }
        }
    }

    public static String getStackTraceString(Throwable t) {
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        StringWriter sw = new StringWriter(256);
        PrintWriter pw = new PrintWriter(sw, false);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
