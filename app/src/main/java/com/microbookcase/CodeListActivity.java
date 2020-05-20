package com.microbookcase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andea.microbook.R;
import com.microbookcase.adapter.CodeAdapter;
import com.microbookcase.bean.BookInfo;
import com.microbookcase.service.MyWebSocket;
import com.microbookcase.service.WebSocketMessageBean;
import com.microbookcase.service.WebSocketService;
import com.microbookcase.service.WebSocketUtil;
import com.microbookcase.view.ArrowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created on 2020/4/26 13:33
 * .
 *
 * @author yj
 * @org 浙江趣看点科技有限公司
 */
public class CodeListActivity extends Activity {

    private RecyclerView mRecyclerView;
    private CodeAdapter mAdapter;
    private String currCode = "";
    private int openType;
    private TextView mBackV;
    private TextView mListTitle;
    private TextView mStepOne;
    private TextView mStepTwo;
    private TextView mStepTwoOne;
    private TextView mStepTwoTwo;
    private TextView mStepThree;
    private String openId;
    private String borrowedOrderList;
    private String notBarcode;

    private TextView mDownV;
    private CountDownTimer countDownTimer;

    private ArrowView arrowView1;
    private ArrowView arrowView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_code_list);
        EventBus.getDefault().register(this);

        mRecyclerView = findViewById(R.id.code_list_recy);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CodeAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mBackV = findViewById(R.id.code_list_back);
        mBackV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
                try {
                    getCodeBook("9787533264291"); // 9787540488819
                } catch (Exception e) {
                    Log.i("12345678", "onClick: " + e.toString());
                }
            }
        });
        mListTitle = findViewById(R.id.code_list_title);
        mStepOne = findViewById(R.id.code_step_one);
        mStepTwo = findViewById(R.id.code_step_two);
        mStepTwoOne = findViewById(R.id.code_step_two_one);
        mStepTwoTwo = findViewById(R.id.code_step_two_two);
        mStepThree = findViewById(R.id.code_step_three);
        mDownV = findViewById(R.id.code_list_down);
        arrowView1 = findViewById(R.id.code_step_arrow1);
        arrowView2 = findViewById(R.id.code_step_arrow2);
        SharedPreferences.Editor sp = getSharedPreferences("code_local", Context.MODE_PRIVATE).edit();
        sp.clear().commit();

        Intent intent = getIntent();
        openType = intent.getIntExtra("open_type", 0);
        openId = intent.getStringExtra("openId");
        borrowedOrderList = intent.getStringExtra("borrowedOrderList");
        notBarcode = intent.getStringExtra("notBarcode");

//        mBackV.setText("type:" + openType);

        if (openType == 1) {
            startDown();
        } else {
            mDownV.setText("返回");
            mDownV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    private void startDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(25000, 1000) {
            @Override
            public void onTick(long l) {
                mDownV.setText("倒计时 " + (l / 1000) + " 秒");
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int newSize = (int) (mBackV.getHeight() * 0.4);
            mBackV.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            mListTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);

            int stepSize = (int) (mStepThree.getHeight() * 0.2);
            mStepOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepSize);
            mStepTwo.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepSize);
            mStepThree.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepSize);

            int secondSize = (int) (stepSize / 1.5);
            mStepTwoOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepSize);
            mStepTwoTwo.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepSize);

            int arrowHeight = arrowView1.getHeight();
            Log.i("12345678", "onWindowFocusChanged: "+arrowHeight);
            ViewGroup.LayoutParams arrow1Param = arrowView1.getLayoutParams();
            ViewGroup.LayoutParams arrow2Param = arrowView2.getLayoutParams();
            arrow1Param.width = arrowHeight;
            arrow2Param.width = arrowHeight;
            arrowView1.setLayoutParams(arrow1Param);
            arrowView2.setLayoutParams(arrow2Param);
        }
    }

    /**
     * 根据条形码获取书本信息
     *
     * @param code
     */
    private void getCodeBook(String code) {
        String url;
        if (!WebSocketUtil.IS_TEST) {
            url = WebSocketUtil.get_code_book + code;
        } else {
            url = WebSocketUtil.get_code_book_test + code;
        }
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    CodeListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (openType == 1) {
                                startDown();
                            }
                            BookInfo bookInfo = JSON.parseObject(result, BookInfo.class);
//                            mBackV.setText(result);
                            String errorCode = bookInfo.getErrorCode();
                            if (TextUtils.isEmpty(errorCode)) {
                                mAdapter.add(bookInfo.getData());
                                mRecyclerView.scrollToPosition(mAdapter.getItemCount());

                                if (openType == 1) {
                                    sendCodeMessage(bookInfo.getData());
                                } else {
                                    // 把条形码数据存本地
                                    List<BookInfo.DataBean> allData = mAdapter.getAll();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("\"barcodeList\":[");
                                    for (int i = 0; i < allData.size(); i++) {
                                        String rfId = allData.get(i).getRfId();
                                        if (TextUtils.isEmpty(rfId)) {
                                            continue;
                                        }
                                        sb.append("{\"barcode\":\"").append(rfId).append("\",\"name\":\"\"}");
                                        if (i < allData.size() - 1) {
                                            sb.append(",");
                                        }
                                    }
                                    sb.append("],");
                                    SharedPreferences.Editor sp = getSharedPreferences("code_local", Context.MODE_PRIVATE).edit();
                                    sp.putString("code_list", sb.toString()).apply();
                                    Log.i("12345678", "run: " + sb.toString());
                                }

                            } else if ("10001".equals(errorCode)) {
                                Toast.makeText(CodeListActivity.this, bookInfo.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_0) {
                currCode += "0";
            } else if (keyCode == KeyEvent.KEYCODE_1) {
                currCode += "1";
            } else if (keyCode == KeyEvent.KEYCODE_2) {
                currCode += "2";
            } else if (keyCode == KeyEvent.KEYCODE_3) {
                currCode += "3";
            } else if (keyCode == KeyEvent.KEYCODE_4) {
                currCode += "4";
            } else if (keyCode == KeyEvent.KEYCODE_5) {
                currCode += "5";
            } else if (keyCode == KeyEvent.KEYCODE_6) {
                currCode += "6";
            } else if (keyCode == KeyEvent.KEYCODE_7) {
                currCode += "7";
            } else if (keyCode == KeyEvent.KEYCODE_8) {
                currCode += "8";
            } else if (keyCode == KeyEvent.KEYCODE_9) {
                currCode += "9";
            }
        } catch (Exception e) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (!mAdapter.hasContains(currCode)) {
                getCodeBook(currCode); // 9787540488819
            } else {
                Toast.makeText(this, "这边书已添加", Toast.LENGTH_SHORT).show();
            }
            currCode = "";
        }
        return super.onKeyUp(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetClose(String msg) {
        if ("close_scan_view".equals(msg)) {
            finish();
        } else if ("loading_code".equals(msg)) {
            showDialog();
        }
    }

    private ProgressDialog dialog;

    private void showDialog() {
        dialog = ProgressDialog.show(this, "盘点中", "拼命盘点中,请稍后..."
                , false, true);
    }

    private void sendCodeMessage(BookInfo.DataBean dataBean) {
        MyWebSocket myWebSocket = WebSocketService.getMyWebSocket();
        WebSocketMessageBean bean = new WebSocketMessageBean();
        bean.setAction("handle_err_books");
        bean.setMessage("书本扫描完成");
        bean.setOpenId(openId);
        List<BookInfo.DataBean> allData = mAdapter.getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("\"barcodeList\":[{");
        sb.append("\"barcode\":\"").append(dataBean.getRfId()).append("\",\"name\":\"").append(dataBean.getName());
        sb.append("\"}],");
        bean.setBarcodeList(sb.toString());
        bean.setBorrowedOrderList(borrowedOrderList);
        bean.setNotBarcode(notBarcode);
        myWebSocket.sendMessage(bean);

//        mBackV.setText(bean.toString());
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor sp = getSharedPreferences("code_local", Context.MODE_PRIVATE).edit();
        sp.clear().commit();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
