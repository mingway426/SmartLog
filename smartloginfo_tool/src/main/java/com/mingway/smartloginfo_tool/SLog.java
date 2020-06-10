package com.mingway.smartloginfo_tool;

import android.content.Context;

public class SLog {
    static LogCallBack sLogCb;
   private SLog() {
        super();
    }



    /**
     * Send a  log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static void w(String tag, Throwable tr) {
        if (LEVEL > android.util.Log.WARN) {
            return ;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,null,tr);
        }
        android.util.Log.w(tag,tr);
    }




    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
       return  android.util.Log.getStackTraceString(tr);
    }


    public static final int LEVEL =  android.util.Log.VERBOSE;
    private static String MAIN_TAG;
    public static final boolean IS_DEBUG = true;
    public static final String SWITCH = "SWITCH";//debug  app switch
    public static final boolean USE_E = false;


    public static void init(Context context, String mainTag, LogCallBack cb){
        sLogCb = cb;
        MAIN_TAG  = mainTag;
        UiUtil.init(context);
        FileUtil.getsInstance();
        FileUtil.initFileWriter(IS_DEBUG);
    }

    public static void i(String tag, String msg) {
        if (LEVEL > android.util.Log.INFO) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,null);
        }
        FileUtil.f(tag,msg);
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg);
        }else {
            android.util.Log.i(MAIN_TAG, tag + ":  " + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (LEVEL > android.util.Log.INFO) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,tr);
        }
        FileUtil.f(tag,msg+" EX:" + android.util.Log.getStackTraceString(tr));
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg, tr);
        }else {
            android.util.Log.i(MAIN_TAG, tag + ":  " + msg, tr);
        }
    }

    public static void v(String tag, String msg) {
        if (LEVEL > android.util.Log.VERBOSE) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,null);
        }
        FileUtil.f(tag,msg);
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg);
        }else {
            android.util.Log.v(MAIN_TAG, tag + ":  " + msg);
        }

    }

    public static void v(String tag, String msg, Throwable tr) {
        if (LEVEL > android.util.Log.VERBOSE) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,tr);
        }
        FileUtil.f(tag,msg +" EX:" + android.util.Log.getStackTraceString(tr));
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg, tr);
        }else {
            android.util.Log.v(MAIN_TAG, tag + ":  " + msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (LEVEL > android.util.Log.DEBUG) {
            return;
        }
        if (sLogCb != null) {
            sLogCb.onLog(tag, msg, null);
        }
        FileUtil.f(tag,msg);
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg);
        }else {
            android.util.Log.d(MAIN_TAG, tag + ":  " + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LEVEL > android.util.Log.DEBUG) {
            return;
        }

        if(sLogCb != null){
            sLogCb.onLog(tag,msg,tr);
        }
        FileUtil.f(tag, msg+ " EX:" + android.util.Log.getStackTraceString(tr));
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg,tr);
        }else {
            android.util.Log.d(MAIN_TAG,tag+":  " + msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (LEVEL > android.util.Log.WARN) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,null);
        }
        FileUtil.f(tag,msg);
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg);
        }else {
            android.util.Log.w(MAIN_TAG, tag + ":  " + msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (LEVEL > android.util.Log.WARN) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,tr);
        }
        FileUtil.f(tag,msg+ " EX:" + android.util.Log.getStackTraceString(tr));
        if(USE_E){
            android.util.Log.e(MAIN_TAG,tag+":  " +msg, tr);
        }else {
            android.util.Log.w(MAIN_TAG, tag + ":  " + msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL > android.util.Log.ERROR) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,null);
        }
        FileUtil.f(tag,msg);
        android.util.Log.e(MAIN_TAG, tag + ":  " + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (LEVEL > android.util.Log.ERROR) {
            return;
        }
        if(sLogCb != null){
            sLogCb.onLog(tag,msg,tr);
        }
        FileUtil.f(tag,msg + " EX:" + android.util.Log.getStackTraceString(tr));
        android.util.Log.e(MAIN_TAG,tag+":  " + msg, tr);
    }

    public static void report(String msg){
//        Bugtags.sendException(new Throwable("ERROR: "+msg));
//        Bugtags.F("ERROR, " + msg);
        android.util.Log.e(MAIN_TAG,"ERROR:" + msg);
    }

    /**
     *  used for debug memory usage.
     * @param x
     */
    public static void dm(String x){
        android.util.Log.e("MEM",x);
    }



}

