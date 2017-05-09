package com.plugin.silent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;

/**
 * 后台静默下载并可以断点续传功能,需要作为一个jar使用
 *
 * @author yuanjl
 */
public class SlientDownloadService extends Service {
    private static int thread_size = 4;
    private Queue<Intent> queue = new java.util.LinkedList<Intent>(); //存放下载队列
    private Context context;
    private boolean download = false; //是否正在下载
    private long lastdownload = 0l; //上次开始下载时间
    private MyHandler handler;
    private String SILENT_TAG = "silentplugin" + SilentConfig.SILENT_PLUGIN_VERSION;

    public void onCreate(Context c, Class<?> cs) {
        this.context = c;
    }

    public Context getContext() {
        return this.context == null ? this : context;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String url = intent.getStringExtra(SilentConfig.DOWNLOAD_URL_NAME);
            String filename = intent.getStringExtra(SilentConfig.DOWNLOAD_FILE_NAME);
            if (url != null && url.trim().length() > 0
                    && filename != null && filename.trim().length() > 0) {
                queue.add(intent);
            }
        }

        //有网络，有下载队列，没有下载或者上次下载已经过去很长时间，开始下次下载
        if ("WIFI".equals(NetworkUtil.getNetworkTypeStr(context))
                && queue.size() > 0 && (!download
                || System.currentTimeMillis() - lastdownload >= SilentConfig.LAST_DOWNLOAD_ERROR_TIME)) {
            download();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 下载功能
     */
    public void download() {
        final Intent intent = queue.poll();
        final String path = intent.getStringExtra(SilentConfig.DOWNLOAD_URL_NAME);
        final String filename = intent.getStringExtra(SilentConfig.DOWNLOAD_FILE_NAME);

        Log.i(SILENT_TAG, "download->" + path);
        Log.i(SILENT_TAG, "downloadto->" + filename);
        File file = new File(filename);
        final File lockfile = new File(filename + ".d");
        //文件不存在，或者文件存在且有断点记录，开始下载
        if (!file.exists()
                || (file.exists() && lockfile.exists())) {
            download = true;
            lastdownload = System.currentTimeMillis();

            try {
                lockfile.createNewFile();
            } catch (Exception e) {
                Log.e(SILENT_TAG, e.getMessage());
            }
            handler = new MyHandler() {
                int num = 0;
                int threadsize = thread_size;

                public void handleMessage(Message msg) {
                    num++;
                    if (num == threadsize) {
                        download = false;
                        for (int i = 0; i < thread_size; i++) {
                            AppPreferenceHelper.getInstance(getContext()).removeKey(filename + i);
                        }
                        lockfile.delete();
                        Log.i(SILENT_TAG, "download complete!");
                        callback(intent);
                    }
                }

                void setThreadSize(int size) {
                    threadsize = size;
                }

                ;

            };
            final boolean fileExist = file.exists();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    multiDown(path, filename, fileExist);
                }

            }).start();
        } else {
            callback(intent);
        }
    }

    /**
     * 获取下载文件大小，决定多线程下载, 如果无法获取文件大小，则单线程下载
     *
     * @param path
     * @param filename
     * @param fileExist
     */
    private void multiDown(String path, String filename, boolean fileExist) {
        try {
            Log.i(SILENT_TAG, "download start->" + path);
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "Keep-Alive");
            long length = conn.getContentLength();
            long size = length / thread_size + 1;
            Log.i(SILENT_TAG, "downloadstart->" + length);
            if (length > 0) {
                for (int i = 0; i < thread_size; i++) {
                    MyThread t = this.new MyThread(i, i * size, Math.min((i + 1) * size, length) - 1,
                            fileExist ? Long.parseLong(AppPreferenceHelper.getInstance(getContext()).getString(filename + i, "0")) : 0,
                            path, filename, handler);
                    new Thread(t).start();
                }
            } else {
                handler.setThreadSize(1);
                MyThread t = this.new MyThread(0, 0, -1,
                        fileExist ? Long.parseLong(AppPreferenceHelper.getInstance(getContext()).getString(filename + 0, "0")) : 0,
                        path, filename, handler);
                new Thread(t).start();
            }
        } catch (Exception e) {
            Log.e(SILENT_TAG, e.getMessage());
        }
    }

    /**
     * 下载结束发送系统广播，action和附加参数由调用方提供
     *
     * @param intent
     */
    private void callback(Intent intent) {
        String action = intent.getStringExtra(SilentConfig.CALLBACK_ACTION_NAME);
        if (action != null && action.trim().length() > 0) {
            Intent call = new Intent();
            call.setAction(action);
            String extra = intent.getStringExtra(SilentConfig.CALLBACK_EXTRA_NAME);
            call.putExtra(SilentConfig.CALLBACK_EXTRA_NAME, extra);
            call.putExtra(SilentConfig.DOWNLOAD_URL_NAME, intent.getStringExtra(SilentConfig.DOWNLOAD_URL_NAME));
            call.putExtra(SilentConfig.DOWNLOAD_FILE_NAME, intent.getStringExtra(SilentConfig.DOWNLOAD_FILE_NAME));
            this.getContext().sendBroadcast(call);
        }
    }

    /**
     * 下载线程
     *
     * @author Bie
     */
    private class MyThread implements Runnable {
        private int threadId;
        private long startPos;
        private long endPos;
        private long compeleteSize;
        private String urlstr;
        private String filename;
        private Handler handler;

        public MyThread(int threadId, long startPos, long endPos,
                        long compeleteSize, String urlstr, String filename, Handler handler) {
            this.threadId = threadId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.compeleteSize = compeleteSize;
            this.urlstr = urlstr;
            this.filename = filename;
            this.handler = handler;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream is = null;
            try {

                URL url = new URL(urlstr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                // 设置范围，格式为Range：bytes x-y;
                if (endPos > 0)
                    connection.setRequestProperty("Range", "bytes=" + (startPos + compeleteSize) + "-" + endPos);

                randomAccessFile = new RandomAccessFile(filename, "rwd");
                randomAccessFile.seek(startPos + compeleteSize);
                Log.d(threadId + "connection", "" + connection);
                // 将要下载的文件写到保存在保存路径下的文件
                is = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int length = -1;
                while ((length = is.read(buffer)) != -1) {
                    randomAccessFile.write(buffer, 0, length);
                    compeleteSize += length;
                    // LogHelper.showDebug(threadId+"connection complete", ""+compeleteSize);
                    // 更新数据库中的下载
                    AppPreferenceHelper.getInstance(getContext()).putString(filename + threadId, "" + compeleteSize);
                }
            } catch (Exception e) {
                Log.e(SILENT_TAG, e.getMessage());
            } finally {
                try {
                    is.close();
                    randomAccessFile.close();
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e(SILENT_TAG, e.getMessage());
                }
            }
            handler.sendMessage(new Message());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 提供下载的线程数
     *
     * @author Bie
     */
    private abstract class MyHandler extends Handler {

        abstract void setThreadSize(int size);
    }
}

