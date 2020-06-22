package com.microbookcase.service;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andea.lockuntils.LockUtils;
import com.microbookcase.ErrorCode;
import com.microbookcase.WSApplication;
import com.microbookcase.utils.PowerUtil;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.GFunction;
import com.rfid.api.ISO14443AInterface;
import com.rfid.api.ISO14443ATag;
import com.rfid.api.ISO15693Interface;
import com.rfid.api.ISO15693Tag;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MyWebSocket implements Runnable {
    private static final int BOX_AMOUNT = 8;

    private static final String TAG = "MyWebSocket";
    /**
     * 心跳包间隔
     **/
    private static final int BEATSPACE = 6000;
    /**
     * 重连间隔
     **/
    private static final int RECONNECTSPACE = 10000;
    /**
     * 最长的无连接时间
     **/
    private static final int BEAT_MAX_UNCON_SPACE = 30000;
    /**
     * 最长的无连接时间
     **/
    private static final int CLOSE_DOOR_SPACE = 2000;

    private static final int DELAY = 3000;

    private static boolean isNightLightOn = true;
    private static boolean isLightOpened = false;
    private static int LIGHT_BEGIN = 1800;
    private static int LIGHT_END = 2330;

    private static boolean isSterilampOn = false;
    private static boolean isSterilampOpened = false;
    private static int STERILAMP_BEGIN = 0130;
    private static int STERILAMP_END = 0200;

    private static boolean isDeviceInUse = false;

    /**
     * 上一次接受到心跳包反馈信息的时间
     **/
    private long lastBeatSuccessTime;
    /**
     * 心跳包定时
     **/
    private Timer beatTimer = null;

    /**
     * WebSocket重连
     **/
    private Timer wsReconnectTimer = null;

    /**
     * 关门检测
     **/
    private Timer checkDoorTimer = null;

    private LockUtils mLockUtils = new LockUtils();
    private LockUtils mLockUtils2 = new LockUtils();

    private WebSocketConnection webSocketConnection;

    private int doorFlag = 0;

    private boolean isOpenAll = false;

    private String AutoLink = "TRUE";

    private String bookDev = "/dev/ttyS3";
    private String useridDev = "/dev/ttyS2";

    private String[] arrOpenId = new String[BOX_AMOUNT + 1];

    public static void setIsNightLightOn(boolean isOn) {
        isNightLightOn = isOn;
    }

    public static void setLightBegin(int begin) {
        LIGHT_BEGIN = begin;
    }

    public static void setLightEnd(int end) {
        LIGHT_END = end;
    }

    public static void setIsSterilampOn(boolean isOn) {
        isSterilampOn = isOn;
    }

    public static void setSterilampBegin(int begin) {
        STERILAMP_BEGIN = begin;
    }

    public static void setSterilampEnd(int end) {
        STERILAMP_END = end;
    }

    @Override
    public void run() {
        try {
            webSocketConnection = new WebSocketConnection() {
                @Override
                public void sendTextMessage(String payload) {
                    super.sendTextMessage(payload);
                }
            };
            startReconnect();//重连检测
            initRfidReader();
            initRFIDuser();
            startCheckDoor();
        } catch (Exception e) {

        }
        // initRfidReader();
        //  initRFIDuser();
    }

    private void connect() {
        try {
            if (!WebSocketUtil.IS_TEST) {
                webSocketConnection.connect(WebSocketUtil.ROOT_URL, getHandler());
            } else {
                webSocketConnection.connect(WebSocketUtil.ROOT_URL_TEST, getHandler());
            }
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.webSocketConnection.isConnected();
    }

    public void sendMessage(WebSocketMessageBean bean) {
        this.webSocketConnection.sendTextMessage(bean.toString());
    }

    public WebSocketConnection getWebSocketConnection() {
        return this.webSocketConnection;
    }

    public LockUtils getLockUtils() {
        return this.mLockUtils;
    }

    public LockUtils getLockUtils2() {
        return this.mLockUtils2;
    }

    private WebSocketHandler getHandler() {
        return new WebSocketHandler() {
            @Override
            public void onOpen() {
                super.onOpen();
                /** 关闭重连 **/
                stopReconnect();
                /** 建立连接时开启心跳包 **/
                startBeat();
                Log.i(TAG, "onOpen: 开启");
                EventBus.getDefault().post("open_redy");
//                Toast.makeText(WSApplication.app, "onOpen: 开启", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClose(int code, String reason) {
                super.onClose(code, reason);
                Log.i(TAG, code + " , " + reason);
                // 暂时处理
                stopSendBeat();
                //startReconnect();
                // Toast.makeText(WSApplication.app, code + " , " + reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextMessage(String payLoad) {
                if (WebSocketUtil.IS_TEST) {
                    Toast.makeText(WSApplication.app, payLoad, Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "onTextMessage: " + payLoad + "  " + Thread.currentThread().getName());
                if (TextUtils.isEmpty(payLoad) || payLoad.equalsIgnoreCase("ping") || payLoad.equalsIgnoreCase("heartbeat")) {
                    PowerUtil.checkAutoPower();
                    //心跳
                    return;
                }

                // WebSocketMessageBean bean = WebSocketMessageBean.parse(payLoad);
                // Log.i(TAG, payLoad);
                WebSocketMessageBean bean = JSON.parseObject(payLoad, WebSocketMessageBean.class);
                //new TypeReference<WebSocketMessageBean>() {});
                //Toast.makeText(WSApplication.app, bean.getAction(), Toast.LENGTH_SHORT).show();

                /**
                 * 书柜主体业务逻辑
                 */
                if (bean.getAction().equalsIgnoreCase("config")) {
                    try {
                        String[] data = bean.getData();
                        Log.i(TAG, "config：" + bean.toString());//关门消息
                        if (data.length == 6) {
                            int nightOn = Integer.parseInt(data[0]);
                            if (nightOn == 0) {
                                setIsNightLightOn(false);
                            } else {
                                setIsNightLightOn(true);
                            }
                            setLightBegin(Integer.parseInt(data[1]));
                            setLightEnd(Integer.parseInt(data[2]));

                            int steriOn = Integer.parseInt(data[3]);
                            if (steriOn == 0) {
                                setIsSterilampOn(false);
                            } else {
                                setIsSterilampOn(true);
                            }
                            setSterilampBegin(Integer.parseInt(data[4]));
                            setSterilampEnd(Integer.parseInt(data[5]));
                        } else if (data.length == 8) {
                            int nightOn = Integer.parseInt(data[0]);
                            if (nightOn == 0) {
                                setIsNightLightOn(false);
                            } else {
                                setIsNightLightOn(true);
                            }
                            setLightBegin(Integer.parseInt(data[1]));
                            setLightEnd(Integer.parseInt(data[2]));

                            int steriOn = Integer.parseInt(data[3]);
                            if (steriOn == 0) {
                                setIsSterilampOn(false);
                            } else {
                                setIsSterilampOn(true);
                            }
                            setSterilampBegin(Integer.parseInt(data[4]));
                            setSterilampEnd(Integer.parseInt(data[5]));


                            // 定制开机关机时间
                            PowerUtil.shutOn = Integer.parseInt(data[6]);
                            PowerUtil.shutTime = data[7];
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return;
                } else if (bean.getAction().equals("open_box")) {
                    try {
                        if (doorFlag == 0) { //门全关的
                            if (bean.getBoxId() > 0) {
                                isOpenAll = false;
                                int openBoxStatus = openBox(bean.getBoxId());
                                arrOpenId[bean.getBoxId()] = bean.getOpenId();

                                if (openBoxStatus == 1) {
                                    bean.setStatus("success");
                                    EventBus.getDefault().post("jump_code");
                                    webSocketConnection.sendTextMessage(bean.toString());
                                } else {
                                    bean.setStatus("fail");
                                    webSocketConnection.sendTextMessage(bean.toString());
                                }
                            } else if (bean.getBoxId() == 0) {
                                isOpenAll = true;
                                arrOpenId[0] = bean.getOpenId();
                                int openBoxStatus = 0;
                                for (int i = 1; i <= BOX_AMOUNT; i++) {
                                    openBoxStatus += openBox(i);
                                    arrOpenId[i] = bean.getOpenId();
                                }

                                if (openBoxStatus == 8) {
                                    bean.setStatus("success");
                                    webSocketConnection.sendTextMessage(bean.toString());
                                } else {
                                    bean.setStatus("fail");
                                    bean.setMessage("有门未成功打开，请检查");
                                    webSocketConnection.sendTextMessage(bean.toString());
                                }
                            }
                        } else {
                            bean.setStatus("error");
                            bean.setMessage("有门未关好，请关好门后再开门");
                            webSocketConnection.sendTextMessage(bean.toString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("count_book")) {//管理员盘点图书
                    try {
                        if (doorFlag == 0) { //门全关的
                            //此处盘点书柜
                            checkBoxTags(bean.getBoxId(), bean.getOpenId());//盘点
                        } else {
                            bean.setStatus("error");
                            bean.setMessage("有门未关好，请关好门后再盘点书柜");
                            webSocketConnection.sendTextMessage(bean.toString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("open_light")) {//管理员盘点图书
                    try {
                        openLight();
                        bean.setStatus("success");
                        webSocketConnection.sendTextMessage(bean.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("close_light")) {//管理员盘点图书
                    try {
                        closeLight();
                        bean.setStatus("success");
                        webSocketConnection.sendTextMessage(bean.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("reconnect")) {//管理员盘点图书
                    try {
                        if (doorFlag == 0) { //门全关的
                            //此处盘点书柜
                            webSocketConnection.disconnect();
                            connect();
                        } else {
                            bean.setStatus("error");
                            bean.setMessage("有门未关好，请关好门后再盘点书柜");
                            webSocketConnection.sendTextMessage(bean.toString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("exit")) {//管理员盘点图书
                    System.err.println("exit");
                    System.exit(0);//正常退出
                } else if (bean.getAction().equals("close_light")) {//管理员盘点图书
                    try {
                        closeLight();
                        bean.setStatus("success");
                        webSocketConnection.sendTextMessage(bean.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("open_light")) {//管理员盘点图书
                    try {
                        openLight();
                        bean.setStatus("success");
                        webSocketConnection.sendTextMessage(bean.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (bean.getAction().equals("close_scan_view")) { // 关闭条形码扫描界面
                    EventBus.getDefault().post("close_scan_view");
                } else if (bean.getAction().equals("handle_err_books")) { // 开始处理异常，打开条形码扫描界面
//                    String openId = bean.getOpenId();
                    EventBus.getDefault().post(bean);
                }
            }
        };
    }

    private static int getNowTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        return hour * 100 + min;
    }


    private void startBeat() {
        try {
            /** 定时器 **/
            beatTimer = new Timer();
            TimerTask beatTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 心跳");
                    try {
                        // 夜间开灯
                        if (isLightOpened) {
                            if (!isDeviceInUse) {
                                int hour = getNowTime();
                                if (hour < LIGHT_BEGIN || hour > LIGHT_END) {
                                    closeLight();
                                }
                            }
                        } else if (isNightLightOn) {
                            int hour = getNowTime();
                            if (hour >= LIGHT_BEGIN && hour <= LIGHT_END) {
                                openLight();
                            }
                        }

                        //消毒灯
                        if (isSterilampOpened) {
                            int hour = getNowTime();
                            if (hour < STERILAMP_BEGIN || hour > STERILAMP_END) {
                                closeSterilamp();
                            }
                        } else if (isSterilampOn) {
                            int hour = getNowTime();
                            if (hour >= STERILAMP_BEGIN && hour <= STERILAMP_END) {
                                openSterilamp();
                            }
                        }

                        long thisBeatTime = new Date().getTime();
                        /** 只能发送String **/
                        if (webSocketConnection != null && webSocketConnection.isConnected()) {
                            webSocketConnection.sendTextMessage("{\"action\":\"heartbeat\"}");
                            lastBeatSuccessTime = thisBeatTime;
                        } else {
                            if (lastBeatSuccessTime != 0 && (thisBeatTime - lastBeatSuccessTime) > BEAT_MAX_UNCON_SPACE) {
                                /** 判断 当前时间 距离 上次连接时间 已经超过 设定的 最长未连接时间，开启重连**/
                                stopSendBeat();
                                startReconnect();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            beatTimer.schedule(beatTimerTask, DELAY, BEATSPACE);
        } catch (Exception e) {

        }
    }

    private void stopSendBeat() {
        try {
            if (beatTimer != null) {
                beatTimer.cancel();
                beatTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReconnect() {
        try {
            wsReconnectTimer = new Timer();
            TimerTask wsReconnectTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 重连");
                    connect();
                }
            };
            wsReconnectTimer.schedule(wsReconnectTimerTask, 0, RECONNECTSPACE);
        } catch (Exception e) {

        }
    }

    public void disconnect() {
        try {
            webSocketConnection.disconnect();
        } catch (Exception e) {

        }
    }

    private void stopReconnect() {
        lastBeatSuccessTime = new Date().getTime();
        try {
            if (wsReconnectTimer != null) {
                wsReconnectTimer.cancel();
                wsReconnectTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCheckDoor() throws Exception {
        checkDoorTimer = new Timer();
        TimerTask checkDoorTimerTask = new TimerTask() {
            @Override
            public void run() {
                for (int i = 1; i <= BOX_AMOUNT; i++) {
                    int status = read_lock(i);
                    if (status == 1) { //门关的，判断是否盘点
                        if (((doorFlag >> (i - 1)) & 1) == 1) {//之前门是开的
                            try {
                                doorFlag &= ~(1 << (i - 1)); //置0

                                if (!isOpenAll) {
                                    final String openId = arrOpenId[i];
                                    WebSocketMessageBean bean = new WebSocketMessageBean();
                                    bean.setAction("close_box");
                                    bean.setBoxId(i);
                                    bean.setOpenId(openId);
                                    bean.setStatus("success");
                                    webSocketConnection.sendTextMessage(bean.toString());
                                    //if (read_lock(i) == 1) {
                                    Log.i(TAG, "关门：" + bean.toString());//关门消息
                                    try {
                                        Thread.sleep(2500L);
                                    } catch (Exception ex) {
                                    }
                                    //全开门不盘点
                                    checkBoxTags(i, openId);//盘点
                                }
                                //closeLight();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        //Log.i(TAG, "box-" + i + ": 门未关闭");
                    }
                }
                if (isOpenAll) {
                    if (doorFlag == 0) {
                        isOpenAll = false;
                        final String openId = arrOpenId[0];
                        WebSocketMessageBean bean = new WebSocketMessageBean();
                        bean.setAction("close_box");
                        bean.setBoxId(0);
                        bean.setOpenId(openId);
                        bean.setStatus("success");
                        webSocketConnection.sendTextMessage(bean.toString());
                        try {
                            Thread.sleep(2500L);
                        } catch (Exception ex) {
                        }
                        checkBoxTags(0, openId);//盘点
                    }
                }
            }
        };
        checkDoorTimer.schedule(checkDoorTimerTask, DELAY, CLOSE_DOOR_SPACE);
    }

    /**
     * 设备API
     */
    private int openLight() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mLockUtils2.openLight();
            }
        }, 100);
        isLightOpened = true;
        return 0;
    }

    private int closeLight() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mLockUtils2.closeLight();
            }
        }, 100);
        isLightOpened = false;
        return 0;
    }

    //注意：接线接反了，开紫外灯和关紫外灯函数对调
    private int openSterilamp() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mLockUtils2.closeSterilamp();
            }
        }, 100);
        isSterilampOpened = true;
        return 0;
    }

    private int closeSterilamp() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mLockUtils2.openSterilamp();
            }
        }, 100);
        isSterilampOpened = false;
        return 0;
    }

    private int openBox(final int boxnum) {
        //new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
        if (!isLightOpened) {
            openLight();
        }
        int status = write_read_door(true, boxnum, BOX_AMOUNT);
        if (status == 1) {
            doorFlag |= 1 << (boxnum - 1);
        } else {
            doorFlag &= ~(1 << (boxnum - 1)); //置0
        }
//            }
        //}, 100);

        if (((doorFlag >> (boxnum - 1)) & 1) == 1) {//门是开的
            isDeviceInUse = true;
            return 1;
        } else {
            return -1;
        }
    }

    private int write_read_door(boolean wr, int boxnum, int boxTotal) {
        if (wr) {
            return open_lock(boxnum);
        } else {
            if (boxnum == 0) {
                int status = 0;
                for (int i = 0; i < boxTotal; i++) {
                    long t1 = System.currentTimeMillis();
                    if (read_lock(i + 1) == 0)
                        status |= (1 << i);
                    long t2 = System.currentTimeMillis();
                    //Log.i("mini", i+" status = "+status+" t1-t1="+(t2-t1));
                }
                return status;
            } else {
                return read_lock(boxnum);
            }
        }
    }

    //0是开，1是关，小于0失败
    private int read_lock(int boxnum) {
        if (!mLockUtils.isLockDevOpen())
            return -1;

        return mLockUtils.readLock(1, boxnum);
    }

    //1是开，小于0失败
    private int open_lock(int boxnum) {
        if (!mLockUtils.isLockDevOpen())
            return -1;
        return mLockUtils.unlock(1, boxnum);
    }


    /**
     * 盘点图书
     */
    void checkBoxTags(final int number, final String openId) {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                if (number == 0) {
                    for (int boxNum = 1; boxNum <= 8; boxNum++) {
                        List<String> tagsList = new ArrayList<>();
                        gridIdInventory(boxNum, tagsList);

                        WebSocketMessageBean bean = new WebSocketMessageBean();
                        bean.setAction("count_book");
                        bean.setBoxId(boxNum);
                        bean.setOpenId(openId);
                        bean.setData(tagsList.toArray(new String[0]));

                        webSocketConnection.sendTextMessage(bean.toString());

                        try {
                            Thread.sleep(2000);
                        } catch (Exception ex) {
                        }

                        Log.i(TAG, "web状态" + webSocketConnection.isConnected());
                        Log.i(TAG, "盘点: " + bean.toString());
                    }
                } else {
                    List<String> tagsList = new ArrayList<>();
                    gridIdInventory(number, tagsList);

                    WebSocketMessageBean bean = new WebSocketMessageBean();
                    bean.setAction("count_book");
                    bean.setBoxId(number);
                    bean.setOpenId(openId);
                    bean.setData(tagsList.toArray(new String[0]));
                    SharedPreferences sp = WSApplication.app.getSharedPreferences("code_local", Context.MODE_PRIVATE);
                    String barcodeList = sp.getString("code_list", "");
                    bean.setBarcodeList(barcodeList);

                    webSocketConnection.sendTextMessage(bean.toString());

                    EventBus.getDefault().post("close_scan_view");
                    EventBus.getDefault().post("start_play_mp3");
//                    EventBus.getDefault().post(bean);

                    Log.i(TAG, "web状态" + webSocketConnection.isConnected());
                    Log.i(TAG, "盘点: " + bean.toString());
                }
                if (isNightLightOn) {
                    int hour = getNowTime();
                    if (hour < LIGHT_BEGIN || hour > LIGHT_END) {
                        closeLight();
                    }
                } else {
                    closeLight();
                }
                isDeviceInUse = false;
            }
        }, 100);
    }

    boolean b_bookGridIdInventory_run = false;

    public int gridIdInventory(int boxnum, List<String> boxsInventoryList) {
        while (b_bookGridIdInventory_run) ;
        b_bookGridIdInventory_run = true;
        int ret = __gridIdInventory(boxnum, boxsInventoryList);

        b_bookGridIdInventory_run = false;
        return ret;
    }

    private int __gridIdInventory(int boxnum, List<String> boxsInventoryList) {
        byte useAnt[] = {1, 2, 3, 4, 5, 6, 7, 8};
        if (boxnum > 0) {
            useAnt = new byte[1];
            useAnt[0] = (byte) boxnum;
        }
        return inventoryTags(useAnt, boxsInventoryList);
    }

    public boolean isReaderOpen() {
        return m_reader.isReaderOpen();
    }

    public static ADReaderInterface m_reader = new ADReaderInterface();
    String conStrPara = "";

    public int OpenDev() {
        String devName = "";
        devName = "RD5100";
        conStrPara = String
                .format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255", devName, bookDev, "38400", "8E1");
        int ret = m_reader.RDR_Open(conStrPara);
        if (ret == ApiErrDefinition.NO_ERROR) {
        } else {
        }
        return ret;
    }

    private void CloseBookCheckDev() {
        if (m_reader.isReaderOpen()) {
            m_reader.RDR_SetCommuImmeTimeout();
            m_reader.RDR_Close();
        }
    }

    private int inventoryTags(byte useAnt[], List<String> inventoryUidList) {
        if (m_reader == null) {
            //appendRunLog(new DebugInfo().line(), "rfid init failed");
            return ErrorCode.E_READER_NULL;
        } else if (!m_reader.isReaderOpen()) {
            return ErrorCode.E_READER_NOTOPEN;
        }

        byte newAI = RfidDef.AI_TYPE_NEW;
        Object hInvenParamSpecList = ADReaderInterface
                .RDR_CreateInvenParamSpecList();
        ISO15693Interface.ISO15693_CreateInvenParam(hInvenParamSpecList,
                (byte) 0, false, (byte) 0x00, (byte) 0);
        Vector<ISO15693Tag> tagsList = new Vector<ISO15693Tag>();
        long t1 = System.currentTimeMillis();
        int iret = m_reader.RDR_TagInventory(newAI, useAnt, 0,
                hInvenParamSpecList);
        if (iret == ApiErrDefinition.NO_ERROR) {
            //Close RF if inventory is successful.
            m_reader.RDR_CloseRFTransmitter();
        }
        long t2 = System.currentTimeMillis();
        int inventory_time_consuming = (int) (t2 - t1);
        if (iret == ApiErrDefinition.NO_ERROR) {
            Object tagReport = m_reader
                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
            while (tagReport != null) {
                ISO15693Tag tag = new ISO15693Tag();
                iret = ISO15693Interface.ISO15693_ParseTagDataReport(
                        tagReport, tag);
                if (iret == ApiErrDefinition.NO_ERROR) {
                    tagsList.add(tag);
                }

                tagReport = m_reader
                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
            }
            List<InventoryReport> inventoryList = new ArrayList<>();
            inventoryList.clear();
            inventoryUidList.clear();
            for (ISO15693Tag t : tagsList) {
                boolean bTagExist = false;
                String uidStr = GFunction.encodeHexStr(t.uid);
                InventoryReport report = new InventoryReport(uidStr, "" + (int) t.ant_id);

                for (InventoryReport tag : inventoryList) {
                    if (tag.uidStr.equals(uidStr)) {
                        bTagExist = true;
                        break;
                    }
                }
                if (!bTagExist) {
                    inventoryList.add(report);
                    inventoryUidList.add(uidStr);
                }
            }
            return ApiErrDefinition.NO_ERROR;
        } else
            return iret;
    }

    public static class InventoryReport {
        private String uidStr;
        private String TagTypeStr;

        public InventoryReport() {
            super();
        }

        public InventoryReport(String uid, String tayType) {
            super();
            this.setUidStr(uid);
            this.setTagTypeStr(tayType);
        }

        public String getUidStr() {
            return uidStr;
        }

        public void setUidStr(String uidStr) {
            this.uidStr = uidStr;
        }

        public String getTagTypeStr() {
            return TagTypeStr;
        }

        public void setTagTypeStr(String tagTypeStr) {
            TagTypeStr = tagTypeStr;
        }
    }

    private void initRfidReader() {

        if (AutoLink.equals("TRUE"))
            OpenDev();
    }

    private void initRFIDuser() {
        if (AutoLink.equals("TRUE")) {
            OpenUerIdScanRfidDev();
        } else {
        }


        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                if (isUerIdScanRfidDevOpen()) {
                    byte useAnt[] = {1, 2, 3, 4, 5, 6, 7, 8};
                    List<String> inventoryUidList = new ArrayList<String>();
                    int retInt = inventoryUerRFID(useAnt, inventoryUidList);
                    if (retInt == ErrorCode.NONE_ERROR) {
                        int boxNumb = 0;
                        if (useAnt.length == 1)
                            boxNumb = useAnt[0];
                        if (inventoryUidList.size() > 0) {
//                            String userRfidStr = inventoryUidList.get(0);
//                            Message msg = mHandler.obtainMessage();
//                            msg.what = INVENTORY_USER;
//                            msg.obj = userRfidStr;
//                            msg.arg2 = boxNumb;
//                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }, 100, 200);
    }

    public static ADReaderInterface m_reader2 = new ADReaderInterface();

    public boolean OpenUerIdScanRfidDev() {
        String devName = "RL8000";
        String conStrPara2 = String
                .format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255", devName, useridDev, "38400", "8E1");
        if (m_reader2.RDR_Open(conStrPara2) == ApiErrDefinition.NO_ERROR) {
            return true;
        } else {
            return false;
        }

    }

    private void CloseUserIdScanfDev() {
        if (m_reader2.isReaderOpen()) {
            m_reader2.RDR_SetCommuImmeTimeout();
            m_reader2.RDR_Close();
        }
    }

    public boolean isUerIdScanRfidDevOpen() {
        return m_reader2.isReaderOpen();
    }

    public int inventoryUerRFID(byte useAnt[], List<String> inventoryUidList) {
        if (m_reader2 == null) {
            return ErrorCode.E_USER_RFID_NULL;
        } else if (!m_reader2.isReaderOpen()) {
            return ErrorCode.E_USER_RFID_CLOSE;
        }
        byte newAI = RfidDef.AI_TYPE_NEW;
        Object hInvenParamSpecList = ADReaderInterface
                .RDR_CreateInvenParamSpecList();
        ISO15693Interface.ISO15693_CreateInvenParam(hInvenParamSpecList,
                (byte) 0, false, (byte) 0x00, (byte) 0);
        ISO14443AInterface.ISO14443A_CreateInvenParam(
                hInvenParamSpecList, (byte) 0);
        Vector<Object> tagsList = new Vector<Object>();
        long t1 = System.currentTimeMillis();
        int iret = ~ApiErrDefinition.NO_ERROR;

        try {
            iret = m_reader2.RDR_TagInventory(newAI, useAnt, 0,
                    hInvenParamSpecList);
        } catch (Exception e) {

        }

        long t2 = System.currentTimeMillis();
        int inventory_time_consuming = (int) (t2 - t1);
        if (iret == ApiErrDefinition.NO_ERROR) {
            Object tagReport = m_reader2
                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
            while (tagReport != null) {
                Object obj = tag15693_1443a(tagReport);
                if (obj != null) {
                    tagsList.add(obj);
                }
                tagReport = m_reader2
                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
            }
            List<InventoryReport> inventoryList = new ArrayList<>();
            inventoryUidList.clear();
            for (Object t : tagsList) {
                String uidStr = null;
                int ant_id = 0;
                if (t instanceof ISO15693Tag) {
                    ISO15693Tag tagData = (ISO15693Tag) t;
                    uidStr = GFunction.encodeHexStr(tagData.uid);
                    ant_id = (int) tagData.ant_id;
                } else if (t instanceof ISO14443ATag) {
                    ISO14443ATag tagData = (ISO14443ATag) t;
                    uidStr = GFunction.encodeHexStr(tagData.uid);
                    ant_id = (int) tagData.ant_id;
                }
                if (uidStr != null) {
                    boolean bTagExist = false;
                    InventoryReport report = new InventoryReport(uidStr, "" + ant_id);
                    for (InventoryReport tag : inventoryList) {
                        if (tag.uidStr.equals(uidStr)) {
                            bTagExist = true;
                            break;
                        }
                    }
                    if (!bTagExist) {
                        inventoryList.add(report);
                        inventoryUidList.add(uidStr);
                    }
                }
            }
            return ErrorCode.NONE_ERROR;
        } else {
            return ErrorCode.E_USER_RFID_UNKNOW;
        }
    }

    Object tag15693_1443a(Object tagReport) {
        ISO15693Tag ISO15693TagData = new ISO15693Tag();
        int iret = ISO15693Interface.ISO15693_ParseTagDataReport(
                tagReport, ISO15693TagData);
        if (iret == ApiErrDefinition.NO_ERROR) {
            return ISO15693TagData;
        }

        ISO14443ATag ISO14444ATagData = new ISO14443ATag();
        iret = ISO14443AInterface.ISO14443A_ParseTagDataReport(
                tagReport, ISO14444ATagData);
        if (iret == ApiErrDefinition.NO_ERROR) {
            return ISO14444ATagData;
        }
        return null;
    }

}
