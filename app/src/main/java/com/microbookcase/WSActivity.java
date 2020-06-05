package com.microbookcase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andea.microbook.R;
import com.microbookcase.bean.TestBean;
import com.microbookcase.bean.WidthBean;
import com.microbookcase.service.WebSocketMessageBean;
import com.microbookcase.service.WebSocketService;
import com.microbookcase.service.WebSocketUtil;
import com.microbookcase.utils.MyImageView;
import com.microbookcase.utils.PowerUtil;
import com.microbookcase.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
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

    private LinearLayout mTestLinear;
    private Intent intentService;

    private AssetManager assetManager;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (!WebSocketUtil.IS_TEST) {
            String UNIQUE_STRING = "com.mobilepower.terminal.message";
            Intent intent = new Intent(UNIQUE_STRING);
            intent.putExtra("type", 18);
            sendBroadcast(intent);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ws);
        try {
            mContext = this;

            intentService = new Intent(mContext, WebSocketService.class);
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
            mTestLinear = findViewById(R.id.ws_test_linear);
            mCheck = findViewById(R.id.ws_code_check);
            if (WebSocketUtil.IS_TEST) {
                mCheck.setText("当前ip地址：" + WebSocketUtil.TEST_IP);
                mTestLinear.setVisibility(View.VISIBLE);
            } else {
                mCheck.setText("当前ip地址：" + WebSocketUtil.IP);
                mTestLinear.setVisibility(View.GONE);
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
//                    PowerUtil.shutTime = "11:30";
//                    PowerUtil.checkAutoPower();
//                    Toast.makeText(mContext, "设置时间为" + PowerUtil.shutTime, Toast.LENGTH_SHORT).show();
//                    WebSocketService.getMyWebSocket().disconnect();
                }
            });
        } catch (Exception e) {

        }

        if (!PowerUtil.isNetworkConnected(this)) {
            Toast.makeText(mContext, "当前无网络可用！", Toast.LENGTH_SHORT).show();
        }

        assetManager = getResources().getAssets();
        player = new MediaPlayer();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (WebSocketUtil.IS_TEST) {
                    Toast.makeText(mContext, "正在准备", Toast.LENGTH_SHORT).show();
                }
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (WebSocketUtil.IS_TEST) {
                    Toast.makeText(mContext, "播放完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (WebSocketUtil.IS_TEST) {
                    Toast.makeText(mContext, "出错了i：" + i + "  i1:" + i1, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        findViewById(R.id.ws_code_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMp3("20200605_073309.m4a");
            }
        });
    }

    private void playMp3(String mp3Name) {
        try {
            if (player.isPlaying()) {
                return;
            }
            AssetFileDescriptor fileDescriptor = assetManager.openFd(mp3Name);
            player.reset();
            player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            Toast.makeText(mContext, "出错了：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void initView() throws Exception {
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
        if (!PowerUtil.isNetworkConnected(this)) {
            Toast.makeText(this, "当前无网络可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (R.id.main_button == view.getId()) {
            EventBus.getDefault().post("reconnect");
            playMp3("20200605_073309.m4a");
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
            playMp3("20200605_073507.m4a");
        } else if ("start_play_mp3".equals(message)) {
            playMp3("20200605_073606.m4a");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTestStr(TestBean testBean) {
        mBackV.setText(testBean.getInfoStr());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetImgWidth(WidthBean widthBean) {
        ad_image.post(new Runnable() {
            @Override
            public void run() {
                int dw = ad_image.getDrawable().getBounds().width();
                Matrix m = ad_image.getImageMatrix();
                float[] values = new float[10];
                m.getValues(values);
                float sx = values[0];
                int cw = (int) (dw * sx);
                ViewGroup.LayoutParams layoutParams = main_button.getLayoutParams();
                layoutParams.width = cw;
                main_button.setLayoutParams(layoutParams);
            }
        });
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
        if (intentService != null) {
            stopService(intentService);
        }
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