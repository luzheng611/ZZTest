package demo.lz.com.test;

import android.app.Application;

/**
 * 作者：luZheng on 2018/07/12 10:22
 */
public class IApplication extends Application {
   private static IApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public static  IApplication getInstance(){
        return instance;
    }
}
