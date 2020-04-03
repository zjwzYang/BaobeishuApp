package com.microbookcase;

import android.app.Application;

/**
 * Created on 2020/4/3 10:21
 * .
 *
 * @author yj
 * @org 浙江房超信息科技有限公司
 */
public class WSApplication extends Application {

    public static WSApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
