package com.microbookcase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andea.microbook.R;
import com.microbookcase.service.WebSocketMessageBean;
import com.microbookcase.service.WebSocketService;
import com.microbookcase.service.WebSocketUtil;
import com.microbookcase.utils.MyImageView;
import com.microbookcase.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

public class WSActivity extends Activity implements android.view.View.OnTouchListener, View.OnClickListener {

    private Context mContext;
    private MyImageView ad_image;
    private Button main_button;
    //private Button second_button;
    long[] mHints = new long[5];

    private AlertDialog.Builder imageDialog;//= new AlertDialog.Builder(this);
    private Dialog dialog;
    private boolean isDialogShow = false;
    private static Timer dialogTimer = null;
    private static TimerTask dialogTimerTask = null;
    private TextView mBackV;
    private TextView mCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        String UNIQUE_STRING = "com.mobilepower.terminal.message";
        Intent intent = new Intent(UNIQUE_STRING);
        intent.putExtra("type", 18);
        sendBroadcast(intent);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ws);
        mContext = this;

        Intent intentService = new Intent(mContext, WebSocketService.class);
        startService(intentService);

        initView();
        mBackV = findViewById(R.id.ws_back);
        mBackV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
//                EventBus.getDefault().post("reconnect");
            }
        });
        mCheck = findViewById(R.id.ws_code_check);
        if (WebSocketUtil.IS_TEST) {
            mCheck.setText("当前ip地址：" + WebSocketUtil.TEST_IP);
        } else {
            mCheck.setText("当前ip地址：" + WebSocketUtil.IP);
        }
        mCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebSocketUtil.IS_TEST = !WebSocketUtil.IS_TEST;
                if (WebSocketUtil.IS_TEST) {
                    mCheck.setText("当前ip地址：" + WebSocketUtil.TEST_IP);
                } else {
                    mCheck.setText("当前ip地址：" + WebSocketUtil.IP);
                }
            }
        });
        findViewById(R.id.ws_code_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent codeintent = new Intent(WSActivity.this, CodeListActivity.class);
                startActivity(codeintent);
            }
        });
    }

    public void initView() {
        //second_button = findViewById(R.id.second_button);
        /*second_button.setOnTouchListener(new View.OnTouchListener() {
            //需要监听几次点击事件数组的长度就为几
            //如果要监听双击事件则数组长度为2，如果要监听3次连续点击事件则数组长度为3...

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.err.println("clicked");
                //将mHints数组内的所有元素左移一个位置
                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                //获得当前系统已经启动的时间
                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - mHints[0] < 600)
                    System.exit(0);
                return true;
            }
        });*/

        //wsd_editText = findViewById(R.id.wsd_editText);
        main_button = findViewById(R.id.main_button);
        main_button.setOnClickListener(this);

        //ad_image.setImageURL("https://img.zcool.cn/community/01caac5b8ec2cba8012017eecd66d0.jpg");

        imageDialog = new AlertDialog.Builder(this);
        imageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface di) {
                dialog = null;
                isDialogShow = false;
                stopTimer();
            }
        });

        ad_image = findViewById(R.id.ad_image);
        ad_image.setImageURL("http://www.fayshine.cn/launch/image/device1?t=" + Utils.currDate());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    if (ad_image == null) {
                        continue;
                    }
                    ad_image.setImageURL("http://www.fayshine.cn/launch/image/device1?t=" + Utils.currDate());
                    try {
                        Thread.sleep(30000);
                    } catch (Exception ex) {

                    }
                }
            }
        }, 10000);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (R.id.main_button == v.getId()) {
            EventBus.getDefault().post("reconnect");
            /**
             * 检查连接是否有效，如果有效，发送信息
             * **/
            if (WebSocketService.getMyWebSocket().isConnected()) {

//                long timestamp = System.currentTimeMillis();
//                String url = "http://www.fayshine.cn/device/bind/?device=device2&timestamp=" + timestamp
//                        + "&token=" + Utils.md5("device=device2&timestamp=" + timestamp);
//                showDialog(url);
//                String sendText = wsd_editText.getText().toString().trim();
//                if (!"".equals(sendText)) {
//                    WebSocketMessageBean webSocketMessageBean = new WebSocketMessageBean();
//
////                    webSocketMessageBean.setMessageType(MESSAGETYPE.USERCHAT);
////                    webSocketMessageBean.setSendUserId(FakeDataUtil.SENDUSERID);
////                    webSocketMessageBean.setMessage(sendText);
//
//                    WebSocketService.webSocketConnection.sendTextMessage(sendText);
//                } else {
//                    Toast.makeText(mContext, NoticeUtil.NOT_ALLOWED_EMP, Toast.LENGTH_LONG).show();
//                }

            } else {
                //Toast.makeText(mContext, NoticeUtil.NO_CONNECT, Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (isFastClick()) {
            return;
        }
        if (R.id.main_button == view.getId()) {
            EventBus.getDefault().post("reconnect");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(String message) {
        if ("open_redy".equals(message)) {
            long timestamp = System.currentTimeMillis();
            String url;
            if (!WebSocketUtil.IS_TEST) {
                url = "http://" + WebSocketUtil.IP + "/device/bind/?device=" + WebSocketUtil.DEVICE_NAME + "&timestamp=" + timestamp
                        + "&token=" + Utils.md5("device=" + WebSocketUtil.DEVICE_NAME + "&timestamp=" + timestamp);
            } else {
                url = "http://" + WebSocketUtil.TEST_IP + "/device/bind/?device=" + WebSocketUtil.DEVICE_NAME + "&timestamp=" + timestamp
                        + "&token=" + Utils.md5("device=" + WebSocketUtil.DEVICE_NAME + "&timestamp=" + timestamp);
            }
            showDialog(url);
        } else if ("jump_code".equals(message)) {
            Intent intent = new Intent(this, CodeListActivity.class);
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetClose(WebSocketMessageBean bean) {
        if ("handle_err_books".equals(bean.getAction())) {
            Intent intent = new Intent(this, CodeListActivity.class);
            intent.putExtra("open_type", 1);
            intent.putExtra("openId", bean.getOpenId());
            intent.putExtra("borrowedOrderList", bean.getBorrowedOrderList());
            intent.putExtra("notBarcode", bean.getNotBarcode());
            startActivity(intent);
        }
    }

    private synchronized void showDialog(String url) {
        if (!isDialogShow) {
            Bitmap bm = Utils.generateBitmap(url, 800, 800);//, msg.arg1, msg.arg2);
            if (bm != null) {
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View layout = inflater.inflate(R.layout.dialog, null);
                ImageView image = layout.findViewById(R.id.dialog_imageview);
                image.setImageBitmap(bm);//setImageDrawable(bm);
                imageDialog.setView(layout);
                imageDialog.create();
                dialog = imageDialog.show();
                isDialogShow = true;
                startTimer();
                //开灯
                //WebSocketService.getMyWebSocket().getLockUtils2().openLight();
            }
        }
    }

    private void startTimer() {
        dialogTimer = new Timer();
        dialogTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                    isDialogShow = false;
                }
            }
        };
        dialogTimer.schedule(dialogTimerTask, 15000);
    }

    private void stopTimer() {
        try {
            if (dialogTimer != null) {
                dialogTimer.cancel();
                dialogTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private long lastClickTime = 0;
    private final int CLICK_TIME = 500; //快速点击间隔时间

    // 判断按钮是否快速点击
    public boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < CLICK_TIME) {//判断系统时间差是否小于点击间隔时间
            return true;
        }
        lastClickTime = time;
        return false;
    }
}