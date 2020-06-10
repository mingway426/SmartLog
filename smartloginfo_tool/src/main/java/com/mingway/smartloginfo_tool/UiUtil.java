package com.mingway.smartloginfo_tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.view.WindowManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UiUtil {
    private static Context context;
    private static SharedPreferences defSettings;
    public static Context getContext() {
        return context;
    }
    private static ExecutorService mCachedIoPool;

    public static void init(Context context) {
        UiUtil.context = context;
        defSettings = context.getSharedPreferences("default", Context.MODE_PRIVATE);
//        mCachedIoPool = Executors.newCachedThreadPool();
        mCachedIoPool = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
                5L, TimeUnit.MINUTES,
                new SynchronousQueue<Runnable>());
    }
    public static ExecutorService getCacheIoPool(){
        return mCachedIoPool;
    }

    public static  int getNavigationBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
    public static  int getNavigationBarWidth() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_width","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


    public static  int getStatusBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static int getVersionCode() {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVerName() {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getVerNameForPath() {
        String verName = getVerName();
        verName = "v"+verName.replace(".","_").replace("-debug","");
        return verName;
    }

    public static SharedPreferences getDefSettings(){
        return defSettings;
    }


    public static boolean isAppInstalled(String packageName) {
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static int[] getDisplaySize() {
        WindowManager wm = (WindowManager)getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int[] sizes = new int[2];
        sizes[0] = wm.getDefaultDisplay().getWidth();
        sizes[1] = wm.getDefaultDisplay().getHeight();
        return sizes;

    }

    public static boolean isWifiNetworkOk() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            } else {
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }


    public static String getString(Object message) {
        return message==null?"NULL":message.toString();
    }

    public static boolean isUIThread( ) {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }
}


