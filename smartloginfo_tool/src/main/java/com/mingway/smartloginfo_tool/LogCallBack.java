package com.mingway.smartloginfo_tool;

/***
 * file name: LogCallBack
 * @author： lumingwei
 * @date: 2020/6/5 14:12
 */
public interface LogCallBack {

    void onLog(String tag, String message, Throwable throwable);
}
