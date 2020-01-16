package com.oo.pdfdocument;

import android.app.Application;

import com.oo.pdfdocument.pdfbuilder.ImageRender;

/**
 * @Description TODO
 * @Author wfunny
 * @Date 2020-01-16 20:55
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageRender.getInstance().init(this);

    }
}
