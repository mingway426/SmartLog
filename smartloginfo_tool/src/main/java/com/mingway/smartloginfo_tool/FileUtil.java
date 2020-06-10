package com.mingway.smartloginfo_tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


/**
 * Used for help create file, delete file, create file base on data.
 * //TODO move log relative code to log module
 *
 */

public class FileUtil {
    private static final String TAG = "fileUtil";
    private static final int BUFFER_SIZE = 4048;
    private static final int DISK_SIZE_1M = 1024 * 1024;
    private static final int DISK_SIZE_8M = 8 * DISK_SIZE_1M;
    private static final String DATE_FORMAT_FOR_LOG = "%4d-%02d-%02d %02d:%02d:%02d:%03d";
    private static final String DATE_FORMAT_FOR_FNAME = "%4d%02d%02d-%02d%02d%02d-%03d";
    private static final int MSG_INIT = 1000;
    private static final int MSG_NEW_LOG = 3000;
    private static final String LOG_DIR = "logs";
    private static long LOG_MAX_SIZE = 0;
    private static long LOG_FILE_SIZE = 0;
    private static final int LOG_MAX_FILES = 20;

    private static FileUtil sInstance;
    private BufferedWriter fileWriter;
    private static File sLogDir;
    private  long currFileSize;
    private static boolean DEBUG_TIME = false;
    private static boolean DEBUG_LOG = false;
    private volatile boolean doingDelOld = false;
    private Handler workHandler;


    public static FileUtil getsInstance() {
        if (sInstance == null) {
            synchronized (FileUtil.class) {
                sInstance = new FileUtil();
            }
        }
        return sInstance;
    }

    private FileUtil() {
    }


    /**
     * when file not exist and current file size is match to max_size.
     * need create new file.
     * sdcard/closertvR/2016-11-15/millsecounds
     *
     * @return
     */
    public static File initFileWriter(boolean isDebug) {
        LOG_FILE_SIZE = isDebug ? 3 * DISK_SIZE_1M : DISK_SIZE_1M *2;
        LOG_MAX_SIZE = isDebug ? LOG_FILE_SIZE *  25: LOG_FILE_SIZE * 25;
        HandlerThread workThread = new HandlerThread("LogWork");
        workThread.start();

        sInstance.workHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (what == MSG_INIT) {
                    internalInit();
                } else if (what == MSG_NEW_LOG) {
                    Line line = (Line) msg.obj;
                    sInstance.internalWrite(line.tag, line.content,line.thread);
                }
                super.handleMessage(msg);
            }
        };

        sInstance.workHandler.sendEmptyMessage(MSG_INIT);

        return null;
    }

  
    public static File internalInit() {
        File dir = UiUtil.getContext().getExternalCacheDir();
        sLogDir = new File(dir, LOG_DIR);
        if (!sLogDir.exists()) {
            if (!sLogDir.mkdir()) {
                //log create log dir fail
                return null;
            }
        }

        if (sLogDir == null) {
            return null;
        }

        File logFie = createLogFile(sLogDir);
        return logFie;
    }

    public static boolean createFilterWriter(File logFie) {
        if (logFie == null) {
            return true;
        }
        try {
            sInstance.currFileSize = logFie.length();
            if(sInstance.fileWriter != null){
                sInstance.fileWriter.close();
            }
            sInstance.fileWriter = new BufferedWriter(new FileWriter(logFie, true), BUFFER_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

  
    private static File createLogFile(File logDir) {
        if (logDir == null) {
            return null;
        }
        Calendar curr = Calendar.getInstance();
        //get yy-mm-rr
        int year = curr.get(Calendar.YEAR);
        int month = curr.get(Calendar.MONTH) + 1;
        int day = curr.get(Calendar.DAY_OF_MONTH);
        String dateName = String.format("%4d-%02d-%02d", year, month, day);
        File dataDir = getFile(logDir, dateName, true);

        if (dataDir == null) {
            return null;
        }

        File logFie = getFile(dataDir, getFormatedTime(DATE_FORMAT_FOR_FNAME, curr) + ".txt", false);
        if (DEBUG_LOG) {
            Log.e(TAG, "NEW FILE " + logFie.getName());
        }

        if (logFie == null) {
            Log.d(TAG, "Create file  fail!");
            return null;
        }

        if (createFilterWriter(logFie)) return null;
        if (!sInstance.doingDelOld) {
            sInstance.doingDelOld = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    makeSdcardSizeAvaliable(sLogDir);
                }
            }).start();
        }
        return logFie;
    }

    public static File getFileBySuffixWithDate(String prefix, String suffix, String pathname) {

        File dir  = new File(pathname);
        if(!dir.exists()){
            dir.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        if(prefix == null) prefix = "";
        if(suffix == null) suffix = "";
        pathname = pathname +prefix+sdf.format(new Date())+suffix;
        dir = new File(pathname);
        if(!dir.exists()){
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dir;
    }


    public static File getOrCreateFile(String file, String pathname, boolean createIfNoExist) {

        File dir  = new File(pathname);
        if(!dir.exists()){
            if(!createIfNoExist){
                return null;
            }
            dir.mkdirs();
        }
        dir = new File(pathname,file);
        if(!dir.exists()){
            if(!createIfNoExist){
                return null;
            }
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dir;
    }
    
    public static File getOrCreateFile(String file, String pathname) {
        return getOrCreateFile(file,pathname,true);
    }



    /**
     * list the child files which is end with {@param suffixes}
     * @param path
     * @param suffixes
     * @return
     */
    public static String[] listChilds(String path, final String[] suffixes) {
        File dir = new File(path);
        String[] files =   dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.length() <= 4){
                    return false;
                }

                String[] arr =  name.split("\\.");
                if(arr == null || arr.length < 2){
                    return false;
                }

                String last =  arr[arr.length-1];
                for(String n: suffixes){
                    if(n.equals(last)){
                        return true;
                    }
                }
                return false;
            }
        });

        if(files == null){
            files = new String[0];
        }
        return files;
    }

    public static void makeDir(String dirRoot) {
        File dir = new File(dirRoot);
        if(!dir.exists()){
            dir.mkdirs();
            dir.mkdir();
        }
    }

    /**
     * input:  /mnt/sdcard/1.txt
     * return 1.txt
     * input:  1txt
     * return 1txt
     *
     * @param f
     * @return
     */
    public static String getNameFromPath(String f) {
        String[] arr =  f.split("\\/");
        if(arr == null || arr.length < 2){
            return f;
        }
       return   arr[arr.length-1];
    }

    public static boolean deleteFile(String srcPath) {
        if(srcPath == null || srcPath.length() ==0){
            return true;
        }
       return  deleteFile(new File(srcPath));
    }

    public static String readAllContent(File f) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader( new FileReader(f));
            String c  = bf.readLine();
            while (c != null){
                sb.append(c);
                c = bf.readLine();
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG," File not exist! "+ f.getAbsolutePath());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, String.format("Read %s, meet IO exception! ",f.getAbsolutePath()));
            return sb.toString();
        }
        return sb.toString();
    }

    public static boolean  copy2Dst(String srcPath, File dstFile) {
        if(srcPath == null){
            Log.w(TAG," src path is null!");
            return false;
        }

        if(dstFile == null){
            Log.w(TAG," dst File is null!");
            return false;
        }

        File srcFile = new File(srcPath);
        if(!srcFile.exists()){
            Log.w(TAG," src File is not exist!");
            return false;
        }
        if(srcFile.length() <= 0){
            Log.w(TAG," src File is length is 0!");
            return false;
        }

        if(!dstFile.exists()){
            try {
                if(!dstFile.createNewFile()){
                    Log.w(TAG," dst File not exist, try create and fail!");
                    return false;
                }
            } catch (IOException e) {
                Log.w(TAG," dst File not exist, try create and fail!");
                e.printStackTrace();
                return false;
            }
        }

        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(dstFile);
            byte[] buffer = new byte[1024*2];
            int byteRead;
            while (-1 != (byteRead = fis.read(buffer))) {
                fos.write(buffer, 0, byteRead);
            }
            fis.close();
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    private void internalWrite(String tag, String content, String thread) {
        if (fileWriter == null) {
            createLogFile(sLogDir);
        }
        if (fileWriter != null) {
            try {

                String desStr = String.format("%s %s %s:%s\n", getFormatedTime(DATE_FORMAT_FOR_LOG),
                       thread, tag, content);
                if (currFileSize + desStr.length() > LOG_FILE_SIZE) {
                    if (DEBUG_LOG)
                        Log.d(TAG, "WILL NEW FILE, currFileSize:" + currFileSize + ", LOG_FILE_SIZE:" + LOG_FILE_SIZE + ", msg:" + desStr.length());
                    fileWriter.close();
                    createLogFile(sLogDir);
                }
                if (fileWriter == null) {
                    return;
                }
                fileWriter.write(desStr);
                fileWriter.flush();
                currFileSize += desStr.length();

            } catch (IOException e) {
                Log.d(TAG, "Write log to file fail!", e);
                e.printStackTrace();
            }
        }
    }

    public static void f(String tag, String content) {
        if (sInstance == null) {
            return;
        }
        Message msg = sInstance.workHandler.obtainMessage(MSG_NEW_LOG);
        msg.obj = new Line(tag, content, String.format("%s-%02d", Thread.currentThread().getName(),
                Thread.currentThread().getId()));
        sInstance.workHandler.sendMessage(msg);
    }

    private static boolean makeSdcardSizeAvaliable(File dir) {
        if (dir == null) {
            return false;
        }
        File[] childs = dir.listFiles();//log root dir.
        if (childs == null || childs.length == 0) {
            return true;
        }
        long amount = 0;
        ArrayList<File> list = new ArrayList<>(30);
        File[] logs = null;
        for (File d : childs) {//date dir
            logs = d.listFiles();
            if (logs != null && logs.length >= 0) {
                for (File l : logs) {
                    list.add(l);
                    amount += l.length();
                }
            }
        }

        long dela = LOG_MAX_SIZE - amount;

        if (dela <= 0) {
            if (DEBUG_LOG) {
                Log.e(TAG, "MAKE AVA.dela < 0; amount: " + amount + ", dela:" + dela
                        + ", max:" + LOG_MAX_SIZE + ", list:" + list + ", size:" + list.size());
            }

            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    long tlm = lhs.lastModified();
                    long olm = rhs.lastModified();
                    if (tlm == olm) {
                        return 0;
                    } else if (tlm > olm) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

            File older = null;
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (dela > LOG_FILE_SIZE ) {
                    break;
                }
                older = list.get(i);
                dela += older.length();
                if (DEBUG_LOG) {
                    Log.e(TAG, "MAKE AVA. dela  " + dela + ", MAKE_AVA:" + LOG_FILE_SIZE + older.getName() + ", length:" + older.length());
                }
                older.delete();
                if (DEBUG_LOG) {
                    Log.e(TAG, "DEL OLDER " + older.getName());
                }


            }
        }
        sInstance.doingDelOld = false;
        return true;
    }

    /**
     * if file exist return, if not create it.
     *
     * @param logDir
     * @param dateName
     * @return
     */
    private static File getFile(File logDir, String dateName, boolean dir) {
        File file = new File(logDir, dateName);
        if (!file.exists()) {
            if (dir) {
                if (file.mkdirs()) {
                    return file;
                } else {
                    return null;
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return file;
    }


    /**
     * yyyy-MM-dd HH:mm:ss.SSS
     * More effect than SimpleDateFormat.
     *
     * @param format
     * @return
     */
    private static String getFormatedTime(String format) {
        Calendar calendar = Calendar.getInstance();
        return getFormatedTime(format, calendar);
    }

    private static String getFormatedTime(String format, Calendar calendar) {
        return String.format(format, calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)
                , calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
                , calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
    }


    /**
     * get disk available size
     *
     * @return byte
     */
    public static long getDiskAvailableSize() {
        if (!existsSdcard()) return 0;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getAbsolutePath());
        long bytes = stat.getAvailableBytes();
        return bytes;
    }

    public static Boolean existsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private static class Line {
        String tag;
        String content;
        String thread;

        public Line(String tag, String content, String thread) {
            this.tag = tag;
            this.content = content;
            this.thread = thread;
        }
    }

    public static boolean deleteFile(File procDir) {
        if(procDir.exists()){
            if(procDir.isDirectory()){
                File[] files =  procDir.listFiles();
                boolean result = true;
                for( File f: files){
                     if(!deleteFile(f)){
                         result = false;
                     }
                }
                return result;
            }else{
                return procDir.delete();
            }
        }
        return true;
    }

    /**
     * Cleanup the given {@link OutputStream}.
     *
     * @param outputStream the stream to close.
     */
    public  static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
