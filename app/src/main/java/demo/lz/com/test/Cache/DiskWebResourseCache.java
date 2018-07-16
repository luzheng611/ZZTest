package demo.lz.com.test.Cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.lz.com.test.IApplication;

/**
 * 作者：luZheng on 2018/07/11 18:00
 */
public class DiskWebResourseCache {
    private static final String TAG = "DiskWebResourseCache";

    public static final long DEFAULT_MaxCacheTime = 24 * 3600;//一天

    private static final String[] resourceSuffix = new String[]{
            "html", "htm", "js", "ico", "css", "png", "jpg", "jpeg"
            , "gif", "bmp", "ttf", "woff", "woff2", "otf", "eot", "svg", "xml", "swf", "txt", "text", "conf"
    };

    private ArrayList<String > currentDownloads;

    private        ExecutorService      downloadService;
    private static DiskWebResourseCache instance;

    private File cacheDir;

    public static DiskWebResourseCache getInstance() {
        if (instance == null) {
            synchronized (DiskWebResourseCache.class) {
                if (instance == null) {
                    instance = new DiskWebResourseCache();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化cache类
     *
     */
    public DiskWebResourseCache() {
        //线程池  下载静态资源
//        downloadService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
        currentDownloads=new ArrayList<>();
        cacheDir = getDiskCacheDir(IApplication.getInstance(), "webResource");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

    }

    /**
     *
     * @param url
     * @param resFileName
     * @param mimeType
     * @param encoding
     * @param default_maxCacheTime
     * @return 加载webResource  未缓存则缓存  已缓存则取出
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse loadWebResource(String url, String resFileName, String mimeType, String encoding, long default_maxCacheTime) {
        final CacheEntity cacheEntity = new CacheEntity(url, resFileName, mimeType, encoding, default_maxCacheTime);
        final File        cacheFile   = new File(cacheDir, resFileName);
        if (cacheFile.exists()) {
            //缓存文件存在
            Log.e(TAG, "loadWebResource: 使用缓存" );
            if (checkTime(cacheEntity, cacheFile)) //检查缓存时间
                return null;
            if(currentDownloads.contains(cacheEntity.url)){
                //正在下载
                return null;
            }
            try {
                return new WebResourceResponse(cacheEntity.mimeType, cacheEntity.encoding
                        , new FileInputStream(cacheFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "loadWebResource: 缓存获取失败" + e);
            }

        } else {
            if (downloadService == null) {
                //初始化线程池
                downloadService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
            }
            downloadService.submit(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "loadWebResource: 下载" );
                    //缓存文件不存在
                    downloadFile(cacheFile, cacheEntity);
                }
            });

        }

        return null;
    }

    /**
     * 下载静态资源
     *
     * @param cacheFile 目标文件
     * @param entity    资源信息
     * @return 是否下载完毕
     */
    private boolean downloadFile(File cacheFile, CacheEntity entity) {
        if(!isNetConnection(IApplication.getInstance())){
            return false;
        }
        currentDownloads.add(entity.url);//添加url
        BufferedInputStream inputStream       = null;
        FileOutputStream    fileOutputStream  = null;
        HttpURLConnection   httpURLConnection = null;
        try {
            URL url = new URL(entity.url);
//            trustAllHosts();
            httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(httpURLConnection.getInputStream(), 8 * 1024);

            fileOutputStream = new FileOutputStream(cacheFile);
            int len;
            while ((len = inputStream.read()) != -1) {
                fileOutputStream.write(len);
            }
            fileOutputStream.flush();


            Log.e(TAG,  "   缓存成功    "+cacheFile.length());

            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            currentDownloads.remove(entity.url);
            //关闭流资源
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @param cacheEntity
     * @param cacheFile
     * @return 检查缓存时间
     */
    private boolean checkTime(CacheEntity cacheEntity, File cacheFile) {
        long cacheEntryDelta = System.currentTimeMillis() - cacheFile.lastModified();
        if (cacheEntryDelta > cacheEntity.maxCacheTime) {
//            cacheFile.delete();
            return true;
        }
        return false;
    }


    public File getDiskCacheDir(Context context, String customDirName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + customDirName);
    }

    public class CacheEntity {

        public String url;
        public String fileName;
        public String encoding;
        public long   maxCacheTime;
        public String mimeType;

        private CacheEntity(String url, String fileName,
                            String mimeType, String encoding, long maxCacheTime) {
            this.url = url;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.encoding = encoding;
            this.maxCacheTime = maxCacheTime;
        }
    }

    /**
     *
     * @param mContext
     * @return
     */
    public static boolean isNetConnection(Context mContext) {
        if (mContext!=null){
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo         networkInfo         = connectivityManager.getActiveNetworkInfo();
            boolean             connected           = networkInfo.isConnected();
            if (networkInfo!=null&&connected){
                if (networkInfo.getState()== NetworkInfo.State.CONNECTED){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

}
