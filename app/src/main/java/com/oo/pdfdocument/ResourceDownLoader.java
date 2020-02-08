package com.oo.pdfdocument;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by 朱晓龙 2020/2/8 10:48 PM
 * 资源下载
 */
public class ResourceDownLoader {
    private static final String TAG = "PdfBuilder";
    private Context context;

    public ResourceDownLoader(Context context) {
        this.context = context;
    }

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    //cache
    private final HashMap<String, String> urlPath = new HashMap<>();
    //任务列表
    private final ArrayList<String> taskArrayList = new ArrayList<>();
    //线程池
    private ExecutorService workThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //
    private DownLoadListener downLoadListener;

    /**
     * @param urls 要下载的资源地址
     */
    public void setResUrls(ArrayList<String> urls) {
        Log.i("PdfBuilder", "setResUrls: ");
        urlPath.clear();
        for (String url : urls) {
            urlPath.put(url, null);
        }
    }

    public HashMap<String, String> getUrlPath() {
        // TODO: 2020/2/9 处理
        return urlPath;
    }

    private void noticeMain(){

    }
    public void startDownload() {
        Log.i(TAG, "startDownload: ");
        if (urlPath.isEmpty()) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (downLoadListener != null) {
                        downLoadListener.finished(urlPath);
                    }
                }
            });
            return ;
        }
        for (final String url : urlPath.keySet()) {
            DownLoadTask downLoadTask = new DownLoadTask(context,url, new DownLoadCallback() {
                @Override
                public void onSuccess(String url, String path) {
                    Log.i(TAG, "onSuccess: "+taskArrayList.size());
                    urlPath.put(url,path);
                    synchronized (taskArrayList){
                        if (!taskArrayList.contains(url)) {
                            return;
                        }
                        taskArrayList.remove(url);
                        if (taskArrayList.size()==0) {
                            //如果任务执行完了回调
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (downLoadListener != null) {
                                        downLoadListener.finished(urlPath);
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(String url, String path) {
                    Log.i(TAG, "onFailure: "+taskArrayList.size());
                    urlPath.put(url,path);
                    synchronized (taskArrayList){
                        taskArrayList.remove(url);
                        if (taskArrayList.size()==0) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (downLoadListener != null) {
                                        downLoadListener.finished(urlPath);
                                    }
                                }
                            });
                        }
                    }
                }
            });
            taskArrayList.add(url);
            workThreadPool.submit(downLoadTask);
        }
    }


    public void setDownLoadListener(DownLoadListener downLoadListener) {
        this.downLoadListener = downLoadListener;
    }

    public interface DownLoadListener {
        void finished(HashMap<String, String> results);
    }

    public interface DownLoadCallback{
        void onSuccess(String url,String path);
        void onFailure(String url,String path);
    }
    /**
     * 下载任务
     */
    public static class DownLoadTask implements Runnable {
        private Context context;
        private final String url;
        private String path;
        private final DownLoadCallback callback;

        public DownLoadTask(Context context,String url, DownLoadCallback callback) {
            this.context  = context;
            this.url = url;
            this.callback = callback;
        }

        private HttpURLConnection buildConnection(String url) throws IOException {
            HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
            connect.setConnectTimeout(15 * 1000);
            connect.setReadTimeout(20 * 1000);
            connect.setRequestMethod("GET");
            return connect;
        }


        @Override
        public void run() {
            Log.i(TAG, "run: "+url);
            try {
                HttpURLConnection httpURLConnection = buildConnection(this.url);
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //这里的名字需要注意 目前临时使用 连接时间作为名字
                    File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    File localFile = new File(dir,System.currentTimeMillis()+".png");
                    if (localFile.exists()) {
                        localFile.deleteOnExit();
                    }
                    boolean newFile = localFile.createNewFile();
                    if (!newFile) {//创建文件失败
                        if (callback != null) {
                            callback.onSuccess(url,null);
                        }
                        return;
                    }

                    //获取输入流
                    BufferedInputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    byte[] bytes = new byte[1024 * 10];
                    //获取输出流
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                    int l = 0;
                    while ((l = inputStream.read(bytes)) != -1) {
                        bufferedOutputStream.write(bytes, 0, l);
                    }
                    bufferedOutputStream.flush();
                    inputStream.close();
                    bufferedOutputStream.close();
                    //设置返回结果
                    if (callback != null) {
                        callback.onSuccess(url,localFile.getPath());
                    }

                } else {//网络请求失败
                    if (callback != null) {
                        callback.onFailure(url,null);
                    }
                }

            } catch (IOException e) {
                //网络请求异常
                e.printStackTrace();
                if (callback != null) {
                    callback.onFailure(url,null);
                }
            }
        }
    }

}
