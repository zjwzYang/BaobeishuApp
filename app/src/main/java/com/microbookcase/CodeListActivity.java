package com.microbookcase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andea.microbook.R;
import com.microbookcase.adapter.CodeAdapter;
import com.microbookcase.bean.BookInfo;
import com.microbookcase.service.MyWebSocket;
import com.microbookcase.service.WebSocketMessageBean;
import com.microbookcase.service.WebSocketService;
import com.microbookcase.service.WebSocketUtil;

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
 * @org 浙江房超信息科技有限公司
 */
public class CodeListActivity extends Activity {

    private RecyclerView mRecyclerView;
    private CodeAdapter mAdapter;
    private String currCode = "";
    private int openType;

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

        findViewById(R.id.code_list_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SharedPreferences.Editor sp = getSharedPreferences("code_local", Context.MODE_PRIVATE).edit();
        sp.clear().commit();

        Intent intent = getIntent();
        openType = intent.getIntExtra("open_type", 0);
    }

    /**
     * 根据条形码获取书本信息
     *
     * @param code
     */
    private void getCodeBook(String code) {
        String url = WebSocketUtil.get_code_book + code;
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
                            BookInfo bookInfo = JSON.parseObject(result, BookInfo.class);
                            String errorCode = bookInfo.getErrorCode();
                            if (TextUtils.isEmpty(errorCode)) {
                                mAdapter.add(bookInfo.getData());

                                if (openType == 1) {
                                    sendCodeMessage(bookInfo.getData().getRfId());
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
                                        sb.append("\"").append(rfId).append("\"");
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
                getCodeBook("9787558122811"); // TODO: 2020/4/29 这是为了测试
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
            dialog.dismiss();
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

    private void sendCodeMessage(String barcode) {
        MyWebSocket myWebSocket = WebSocketService.getMyWebSocket();
        WebSocketMessageBean bean = new WebSocketMessageBean();
        bean.setAction("count_book");
        bean.setMessage("书本扫描完成");
        List<BookInfo.DataBean> allData = mAdapter.getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("\"barcodeList\":[");
        sb.append("\"").append(barcode).append("\"");
        sb.append("],");
        bean.setBarcodeList(sb.toString());
        myWebSocket.sendMessage(bean);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor sp = getSharedPreferences("code_local", Context.MODE_PRIVATE).edit();
        sp.clear().commit();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}