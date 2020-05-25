package com.microbookcase.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class WebSocketService extends Service {

    private static MyWebSocket myWebSocket;

    public static MyWebSocket getMyWebSocket() {
        return myWebSocket;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            EventBus.getDefault().register(this);
            /** 建立连接 **/
            myWebSocket = new MyWebSocket();

            myWebSocket.getLockUtils().initLock("/dev/ttyS0");
            myWebSocket.getLockUtils2().initLight("/dev/ttyS1");
            myWebSocket.run();
        } catch (Exception e) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(String message) {
        if ("reconnect".equals(message)) {
//            Log.i("12345678", "onGetMessage: 重启");
            myWebSocket.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
