package com.microbookcase.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.microbookcase.WSApplication;
import com.microbookcase.service.WebSocketUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 2020/5/27 10:44
 * .
 *
 * @author yj
 * @org 浙江房超信息科技有限公司
 */
public class PowerUtil {
    public static int shutOn;
    public static String shutTime;
    public static boolean hasSetShut = false;

    public static void checkAutoPower() {
        if (shutOn == 1 && !TextUtils.isEmpty(shutTime)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                // 当前时间
                Date currDate0 = new Date();
                String currTime = sdf.format(currDate0);
                Date currDate = sdf.parse(currTime);

                // 关机时间
                Date shunDate = sdf.parse(shutTime);

                if (currDate.getTime() > shunDate.getTime()) { // 当前时间大于重启时间是不设置
                    hasSetShut = false;
                    return;
                } else if (shunDate.getTime() - currDate.getTime() < 2 * 60 * 1000 && !hasSetShut) { // 重启时间在当前时间2分钟前设置重启
                    hasSetShut = true;

                    // 开机时间
                    Date powerDate = new Date(shunDate.getTime() + 1 * 60 * 1000);
                    String powerTime = sdf.format(powerDate);

                    String ACTION_UBOX_SHUTDOWN = "com.ubox.auto_power_shut";
                    Intent intent = new Intent(ACTION_UBOX_SHUTDOWN);
                    // effective 字段说明：true 为启动自动重启功能,false 为关闭功能
                    intent.putExtra("effective", true);

                    // shut_time 设置关机时间
                    intent.putExtra("shut_time", shutTime);
                    // power_time 设置开机时间
                    intent.putExtra("power_time", powerTime);
                    WSApplication.app.sendBroadcast(intent);
                    String text = shutOn + "重启广播关机：" + shutTime + "  开机：" + powerTime;
//                    TestBean testBean = new TestBean();
//                    testBean.setInfoStr(text);
//                    EventBus.getDefault().post(testBean);
                    if (WebSocketUtil.IS_TEST) {
                        Toast.makeText(WSApplication.app, text, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    public static boolean isNetworkConnected(Context context) {
        // 获得网络状态管理器
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
            if (net_info != null) {
                for (int i = 0; i < net_info.length; i++) {
                    // 判断获得的网络状态是否是处于连接状态
                    if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
