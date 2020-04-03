package com.microbookcase;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.rfid.api.ADReaderInterface;
import com.rfid.api.GFunction;
import com.rfid.api.ISO14443AInterface;
import com.rfid.api.ISO14443ATag;
import com.rfid.api.ISO15693Interface;
import com.rfid.api.ISO15693Tag;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;
import com.wellcom.GfpUtils;
import com.andea.lockuntils.LockUtils;
import com.andea.microbook.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements OnTouchListener {
	private Button btn_open_box_1, btn_open_box_2, btn_open_box_3, btn_open_box_4, btn_open_all_box;
	private Button btn_open_box_5, btn_open_box_6, btn_open_box_7, btn_open_box_8;
	private Button btn_check_box_1, btn_check_box_2, btn_check_box_3, btn_check_box_4, btn_check_bookcase;
	private Button btn_check_box_5, btn_check_box_6, btn_check_box_7, btn_check_box_8;
	private Button btn_open_light, btn_close_light, btn_open_sterilamp, btn_close_sterilamp;

	private TextView tv_read_lock_1, tv_read_lock_2, tv_read_lock_3, tv_read_lock_4;
	private TextView tv_read_lock_5, tv_read_lock_6, tv_read_lock_7, tv_read_lock_8;
	private TextView tv_check_box_1, tv_check_box_2, tv_check_box_3, tv_check_box_4, tv_check_bookcase;
	private TextView tv_check_box_5, tv_check_box_6, tv_check_box_7, tv_check_box_8;

	private TextView tv_run_log;
	private TextView tv_uid_val;

	private String ip = "192.168.1.11";
	private int port = 30000;

	private Spinner sn_serialPortLock = null;
	private Spinner sn_serialPortLight = null;
	private Spinner sn_serialPortBookCheck = null;
	private Spinner sn_serialPortUserIdCheck = null;

    LockUtils mLockUtils = new LockUtils();
    LockUtils mLockUtils2 = new LockUtils();
	public GfpUtils mgfp = null;
	String lockDev = null;
	String lightDev = null;
	String bookDev = null;
	String useridDev = null;
	String AutoLink=null;
	
	
	private Button btn_open_lock_serial=null;
	private Button btn_open_light_serial=null;
	private Button btn_open_book_serial=null;
	private Button btn_open_userid_serial=null;
	private CheckBox ck_starupOpenSerial=null;
	
	
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		  WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		super.onCreate(savedInstanceState);

//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//				.detectDiskReads().detectDiskWrites().detectNetwork()
//				.penaltyLog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//				.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
//				.build());

		setContentView(R.layout.activity_main);
		initView();
		initAdapter();
		initRfidReader();
		initServo();
		barcodeInputListen();
		initRFIDuser();

		if(AutoLink.equals("TRUE"))
		{
			int ret = mLockUtils.initLock(lockDev);
			if(ret<=0) 
			{
				appendRunLog("init lock com failed");
				
				btn_open_lock_serial.setText(R.string.closed);
			}else
			{
				btn_open_lock_serial.setText(R.string.opened);
			}
			
			ret = mLockUtils2.initLight(lightDev);//S1
			if(ret<=0) 
			{
				appendRunLog("init light com failed");
				btn_open_light_serial.setText(R.string.closed);
			}else
			{
				btn_open_light_serial.setText(R.string.opened);
			}
			
		}else
		{
			btn_open_lock_serial.setText(getString(R.string.closed));
			btn_open_light_serial.setText(getString(R.string.closed));
		}
	}
	
	void initAdapter() {

		lockDev = GetHistoryString(KEY_LOCK_DEV);
		if(lockDev!=null) {
			if(lockDev.length() == 0)
				lockDev = null;
		}
		lightDev = GetHistoryString(KEY_LIGHT_DEV);
		if(lightDev!=null) {
			if(lightDev.length() == 0)
				lightDev = null;
		}
		bookDev = GetHistoryString(KEY_BOOK_DEV);
		if(bookDev!=null) {
			if(bookDev.length() == 0)
				bookDev = null;
		}
		useridDev = GetHistoryString(KEY_USERID_DEV);
		if(useridDev!=null) {
			if(useridDev.length() == 0)
				useridDev = null;
		}
		
		AutoLink=GetHistoryString(KEY_AUTO_LINK);
		if (AutoLink!=null) 
		{
			if(AutoLink.length()==0)
				AutoLink="FALSE";
		}
		
		if(AutoLink.equals("FALSE"))
		{
			ck_starupOpenSerial.setChecked(false);
		}else
		{
			ck_starupOpenSerial.setChecked(true);
		}
		
		
		
		int[] dev_index = {0, 1, 2, 3};
		ArrayAdapter<CharSequence> m_adaComName = null;
		ArrayList<CharSequence> m_comNameList = new ArrayList<CharSequence>();
		String m_comList[] = ADReaderInterface.GetSerialPortPath();
		int i = 0;
		for (String s : m_comList)
		{
			m_comNameList.add(s);
			if(lockDev == null){
				if(s.equals("/dev/ttyS0")) {
					lockDev = s;
					dev_index[0] = i;
				} 
			} else if(s.equals(lockDev)) {
				dev_index[0] = i;
			}
			if(lightDev == null){
				if(s.equals("/dev/ttyS1")) {
					lightDev = s;
					dev_index[1] = i;
				} 
			} else if(s.equals(lightDev)) {
				dev_index[1] = i;
			}
			if(bookDev == null){
				if(s.equals("/dev/ttyS3")) {
					bookDev = s;
					dev_index[2] = i;
				} 
			} else if(s.equals(bookDev)) {
				dev_index[2] = i;
			}
			if(useridDev == null){
				if(s.equals("/dev/ttyS4")) {
					useridDev = s;
					dev_index[3] = i;
				} 
			} else if(s.equals(useridDev)) {
				dev_index[3] = i;
			}
			
			i++;
		}
		m_adaComName = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_dropdown_item, m_comNameList);

		sn_serialPortLock.setAdapter(m_adaComName);
		sn_serialPortLight.setAdapter(m_adaComName);
		sn_serialPortBookCheck.setAdapter(m_adaComName);
		sn_serialPortUserIdCheck.setAdapter(m_adaComName);

		sn_serialPortLock.setSelection(dev_index[0]);
		sn_serialPortLight.setSelection(dev_index[1]);
		sn_serialPortBookCheck.setSelection(dev_index[2]);
		sn_serialPortUserIdCheck.setSelection(dev_index[3]);

		sn_serialPortLock.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				saveHistory(KEY_LOCK_DEV, sn_serialPortLock.getSelectedItem().toString());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sn_serialPortLight.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				saveHistory(KEY_LIGHT_DEV, sn_serialPortLight.getSelectedItem().toString());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sn_serialPortBookCheck.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				saveHistory(KEY_BOOK_DEV, sn_serialPortBookCheck.getSelectedItem().toString());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		sn_serialPortUserIdCheck.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				saveHistory(KEY_USERID_DEV, sn_serialPortUserIdCheck.getSelectedItem().toString());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		ck_starupOpenSerial.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				// TODO Auto-generated method stub
				saveHistory(KEY_AUTO_LINK, ck_starupOpenSerial.isChecked()?"TRUE":"FALSE");
			}
		});


	}

	private void initView() {
		tv_read_lock_1 = (TextView) findViewById(R.id.tv_read_lock_1);
		tv_read_lock_2 = (TextView) findViewById(R.id.tv_read_lock_2);
		tv_read_lock_3 = (TextView) findViewById(R.id.tv_read_lock_3);
		tv_read_lock_4 = (TextView) findViewById(R.id.tv_read_lock_4);

		tv_read_lock_5 = (TextView) findViewById(R.id.tv_read_lock_5);
		tv_read_lock_6 = (TextView) findViewById(R.id.tv_read_lock_6);
		tv_read_lock_7 = (TextView) findViewById(R.id.tv_read_lock_7);
		tv_read_lock_8 = (TextView) findViewById(R.id.tv_read_lock_8);

		tv_check_box_1 = (TextView) findViewById(R.id.tv_check_box_1);
		tv_check_box_2 = (TextView) findViewById(R.id.tv_check_box_2);
		tv_check_box_3 = (TextView) findViewById(R.id.tv_check_box_3);
		tv_check_box_4 = (TextView) findViewById(R.id.tv_check_box_4);

		tv_check_box_5 = (TextView) findViewById(R.id.tv_check_box_5);
		tv_check_box_6 = (TextView) findViewById(R.id.tv_check_box_6);
		tv_check_box_7 = (TextView) findViewById(R.id.tv_check_box_7);
		tv_check_box_8 = (TextView) findViewById(R.id.tv_check_box_8);
		tv_check_bookcase = (TextView) findViewById(R.id.tv_check_bookcase);

		btn_check_box_1 = (Button) findViewById(R.id.btn_check_box_1);
		btn_check_box_1.setOnTouchListener(this);
		btn_check_box_2 = (Button) findViewById(R.id.btn_check_box_2);
		btn_check_box_2.setOnTouchListener(this);
		btn_check_box_3 = (Button) findViewById(R.id.btn_check_box_3);
		btn_check_box_3.setOnTouchListener(this);
		btn_check_box_4 = (Button) findViewById(R.id.btn_check_box_4);
		btn_check_box_4.setOnTouchListener(this);
		btn_check_box_5 = (Button) findViewById(R.id.btn_check_box_5);
		btn_check_box_5.setOnTouchListener(this);
		btn_check_box_6 = (Button) findViewById(R.id.btn_check_box_6);
		btn_check_box_6.setOnTouchListener(this);
		btn_check_box_7 = (Button) findViewById(R.id.btn_check_box_7);
		btn_check_box_7.setOnTouchListener(this);
		btn_check_box_8 = (Button) findViewById(R.id.btn_check_box_8);
		btn_check_box_8.setOnTouchListener(this);
		btn_check_bookcase = (Button) findViewById(R.id.btn_check_bookcase);
		btn_check_bookcase.setOnTouchListener(this);

		btn_open_box_1 = (Button) findViewById(R.id.btn_open_box_1);
		btn_open_box_1.setOnTouchListener(this);
		btn_open_box_2 = (Button) findViewById(R.id.btn_open_box_2);
		btn_open_box_2.setOnTouchListener(this);
		btn_open_box_3 = (Button) findViewById(R.id.btn_open_box_3);
		btn_open_box_3.setOnTouchListener(this);
		btn_open_box_4 = (Button) findViewById(R.id.btn_open_box_4);
		btn_open_box_4.setOnTouchListener(this);
		btn_open_box_5 = (Button) findViewById(R.id.btn_open_box_5);
		btn_open_box_5.setOnTouchListener(this);
		btn_open_box_6 = (Button) findViewById(R.id.btn_open_box_6);
		btn_open_box_6.setOnTouchListener(this);
		btn_open_box_7 = (Button) findViewById(R.id.btn_open_box_7);
		btn_open_box_7.setOnTouchListener(this);
		btn_open_box_8 = (Button) findViewById(R.id.btn_open_box_8);
		btn_open_box_8.setOnTouchListener(this);
		btn_open_all_box = (Button) findViewById(R.id.btn_open_all_box);
		btn_open_all_box.setOnTouchListener(this);
		tv_uid_val = (TextView) findViewById(R.id.tv_uid_val);

		btn_open_light = (Button) findViewById(R.id.btn_open_light);
		btn_open_light.setOnTouchListener(this);
		btn_close_light = (Button) findViewById(R.id.btn_close_light);
		btn_close_light.setOnTouchListener(this);
		btn_open_sterilamp = (Button) findViewById(R.id.btn_open_sterilamp);
		btn_open_sterilamp.setOnTouchListener(this);
		btn_close_sterilamp = (Button) findViewById(R.id.btn_close_sterilamp);
		btn_close_sterilamp.setOnTouchListener(this);
		
		
		btn_open_lock_serial=(Button)findViewById(R.id.btn_open_lock_serial);
		btn_open_lock_serial.setOnTouchListener(this);;
		
		 btn_open_light_serial=(Button)findViewById(R.id.btn_open_light_serial);
		 btn_open_light_serial.setOnTouchListener(this);
		 
		 
		btn_open_book_serial=(Button)findViewById(R.id.btn_open_book_serial);
		btn_open_book_serial.setOnTouchListener(this);
		
		 btn_open_userid_serial=(Button)findViewById(R.id.btn_open_userid_serial);
		 btn_open_userid_serial.setOnTouchListener(this);
		 
		  ck_starupOpenSerial=(CheckBox)findViewById(R.id.ck_starupOpenSerial);
		
		

		tv_run_log = (TextView) findViewById(R.id.tv_run_log);
		tv_run_log.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		LinearLayout test_layout_id = (LinearLayout)findViewById(R.id.test_layout_id);
		test_layout_id.setOnTouchListener(this);


		sn_serialPortLock = (Spinner) findViewById(R.id.sn_serialPortLock);
		sn_serialPortLight = (Spinner) findViewById(R.id.sn_serialPortLight);
		sn_serialPortBookCheck = (Spinner) findViewById(R.id.sn_serialPortBookCheck);
		sn_serialPortUserIdCheck = (Spinner) findViewById(R.id.sn_serialPortUserIdCheck);

	}

	byte caseNumb = 0x01;
	private int read_lock(int boxnum) {
		return mLockUtils.readLock(1,boxnum);
	}
	private int open_lock(int boxnum) {
		return mLockUtils.unlock(1,boxnum);
	}

	
	private int set_LED(int led, boolean state) {
		int result = -10;
		int opt = -1;
		if(led == 0) {
			if(state) 
				opt = 2;
			else 
				opt = 3;
		} else if(led == 1) {
			if(state) 
				opt = 1;
			else 
				opt = 0;
		}
		if(opt < 0)
			return result;
		return result;
	}


	@Override
	protected void onDestroy() {
		Log.d("andea", "onDestroy");
		saveHistory(KEY_USERID_DEV, sn_serialPortUserIdCheck.getSelectedItem().toString());
		saveHistory(KEY_BOOK_DEV, sn_serialPortBookCheck.getSelectedItem().toString());
		saveHistory(KEY_LIGHT_DEV, sn_serialPortLight.getSelectedItem().toString());
		saveHistory(KEY_LOCK_DEV, sn_serialPortLock.getSelectedItem().toString());
		
		saveHistory(KEY_AUTO_LINK, ck_starupOpenSerial.isChecked()?"TRUE":"FLASE");
		
		
		mLockUtils.destroyLock();
		
		CloseBookCheckDev();
		CloseUserIdScanfDev();
		
		if (servoThrd != null && servoThrd.isAlive()) {
			b_servoThrdRun = false;
			try {
				servoThrd.join();
				servoThrd=null;
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}
	
	
	


    private EditText et_code_val;
    void barcodeInputListen() {
    	et_code_val = (EditText) findViewById(R.id.et_code_val);
    	et_code_val.requestFocus();
        //ed_barcode.setInputType(InputType.TYPE_NULL);
    	et_code_val.addTextChangedListener(new TextWatcher() {
            String c = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String str = et_code_val.getText().toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String str = et_code_val.getText().toString();
                //String[] strArry = str.split("//\r");
                if(str.contains("\n"))
                {
                	Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                	et_code_val.setText("");
                	et_code_val.setHint(str.substring(0, str.length()-1));
                }
            }
        });
    	et_code_val.setFocusable(true);
    	et_code_val.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                } else {
                }
            }
        });
    	int inType = et_code_val.getInputType(); 
    	et_code_val.setInputType(InputType.TYPE_NULL);  
    	et_code_val.setInputType(inType); 

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //keyCode
        //Toast.makeText(MainActivity.this, ""+keyCode, Toast.LENGTH_SHORT).show();
    	et_code_val.requestFocus();
        return super.onKeyDown(keyCode, event);
    }


	private static final int INVENTORY_USER = 3;
	private Handler mHandler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity pt = mActivity.get();
			if (pt == null) {
				return;
			}
			switch (msg.what) {
        	case INVENTORY_USER:
        		if(msg.obj != null) {
            		pt.tv_uid_val.setText((String)msg.obj);

                	Toast.makeText(pt, (String)msg.obj, Toast.LENGTH_SHORT).show();
        		}
        		break;
			default:
				break;
			}
		}
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




	private final int BOX_COLUMN = 1;
	private final int BOX_AMOUNT = 8;
	private final int BIND_SERVICE = 0;
	private final int UNBIND_SERVICE = 1;
	private final int INIT_PARAMS = 2;
	private final int SET_BOX_NUMB = 3;
	private final int OPENDOOR = 4;
	private final int CHECK_ONE_DOOR = 5;
	private final int CHECK_ALL_DOOR = 6;
	private final int OPEN_ALL_DOOR = 7;
	private final int AFRESH = 8;
	private final int SETQRCODE_IMAGE = 9;
	private final static int SHOW_PROGRESS = 10;
	private final static int UPDATA_APK = 11;
	private final static int START_DOWNLOAD = 12;
	private final static int LOADER_URL = 13;
	private final static int SET_AD_TEXT_URL = 14;
	private final static int CN_RESLUT = 15;
	private final static int LED_RESLUT = 16;
	private final static int CLEAR_INVENTORY_INFO = 17;
	private final static int RUN_LOG = 18;
	private final static int ISSUED_LOG = 19;
	private final static int SET_LOCK_STATUS = 20;
	private final static int SET_TAG_CNT = 21;

    Handler handlerShow = new Handler() {
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	case SET_LOCK_STATUS:
        		String lockStatusStr = getString(R.string.opened);
        		if(msg.arg2 == 0)
        			lockStatusStr = getString(R.string.closed);
        		else if(msg.arg2 < 0)
        			lockStatusStr = getString(R.string.unknow_status);
        		switch(msg.arg1) {
        		case 1:tv_read_lock_1.setText(lockStatusStr);
        			break;
        		case 2:tv_read_lock_2.setText(lockStatusStr);
        			break;
        		case 3:tv_read_lock_3.setText(lockStatusStr);
        			break;
        		case 4:tv_read_lock_4.setText(lockStatusStr);
        			break;
        		case 5:tv_read_lock_5.setText(lockStatusStr);
	    			break;
	    		case 6:tv_read_lock_6.setText(lockStatusStr);
	    			break;
	    		case 7:tv_read_lock_7.setText(lockStatusStr);
	    			break;
	    		case 8:tv_read_lock_8.setText(lockStatusStr);
	    			break;
        		}
        		break;
        	case SET_TAG_CNT:
        		switch(msg.arg1) {
        		case 0:tv_check_bookcase.setText(""+msg.arg2);
	    			break;
        		case 1:tv_check_box_1.setText(""+msg.arg2);
	    			break;
	    		case 2:tv_check_box_2.setText(""+msg.arg2);
	    			break;
	    		case 3:tv_check_box_3.setText(""+msg.arg2);
	    			break;
	    		case 4:tv_check_box_4.setText(""+msg.arg2);
	    			break;
        		case 5:tv_check_box_5.setText(""+msg.arg2);
	    			break;
	    		case 6:tv_check_box_6.setText(""+msg.arg2);
	    			break;
	    		case 7:tv_check_box_7.setText(""+msg.arg2);
	    			break;
	    		case 8:tv_check_box_8.setText(""+msg.arg2);
	    			break;
        		}
        		break;
        	case RUN_LOG:
        		if(msg.obj != null) {
        			String str = tv_run_log.getText().toString();
        			if(str.length()<4096) {
        				tv_run_log.append((String)msg.obj);
        			} else {
            			str.substring(str.length()/2);
        				tv_run_log.setText(str.substring(str.length()/2)+(String)msg.obj);
        			}
        			int offset=tv_run_log.getLineCount()*tv_run_log.getLineHeight();
        			if(offset>tv_run_log.getHeight()){
        				tv_run_log.scrollTo(0,offset-tv_run_log.getHeight());
        			}
        		}
        		break;
        	default:
        		break;
        	}
            super.handleMessage(msg);  
        };  
    };


	private void initRfidReader()
	{
		
		if(AutoLink.equals("TRUE"))
		OpenDev();
		else
			btn_open_book_serial.setText(R.string.closed);
	}

	private Thread servoThrd = null;
	private boolean b_servoThrdRun = false;
	private void initServo() {
		
		servoThrd = new Thread(new ServoThrd());
		servoThrd.start();
	}

int lastAllDoorState = -1;
int disconnectCNT = 0;
private static final long EARLIEST_SUPPORTED_TIME = 1482899065689L;
private class ServoThrd implements Runnable {
		public void run() {
			b_servoThrdRun = true;
			while (b_servoThrdRun) {

				int currentAllDoorState;
				try {

					if (mLockUtils.isLockDevOpen()) 
					{
						currentAllDoorState = write_read_door(false, 0, BOX_AMOUNT);
						if (currentAllDoorState >= 0) {
							for (int i = 0; i < BOX_AMOUNT; i++) {
								Message msg = new Message();
								msg.what = SET_LOCK_STATUS;
								msg.arg1 = i + 1;
								msg.arg2 = 0;
								if (((lastAllDoorState >> i) & 1) == 1) {
									msg.arg2 = 1;
								}
								handlerShow.sendMessage(msg);
							}

							if (lastAllDoorState >= 0) {
								for (int i = 0; i < BOX_AMOUNT; i++) {
									if (((lastAllDoorState >> i) & 1) == 1) {
										if (((currentAllDoorState >> i) & 1) == 0) {
											checkBoxTags(i + 1);
										}
									}
								}
							}
							lastAllDoorState = currentAllDoorState;
						}

					}

				} catch (Exception e) {
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private int write_read_door(boolean wr, int boxnum, int boxTotal) {
		if(wr) {
			open_lock(boxnum);
		} else {
			if(boxnum == 0) {
				int status = 0;
				for(int i=0; i<boxTotal; i++) {
	                long t1 = System.currentTimeMillis();
					if(read_lock(i+1) == 0)
						status |= (1<<i);
	                long t2 = System.currentTimeMillis();
					//Log.i("mini", i+" status = "+status+" t1-t1="+(t2-t1));
				}
				return status;
			} else {
				return read_lock(boxnum);
			}
		}
		return -110;
	}
	
	private int openLight() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mLockUtils2.openLight();
			}
		}, 1);
		return 0;
	}
	private int closeLight() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mLockUtils2.closeLight();
			}
		}, 1);
		return 0;
	}
	private int openSterilamp() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mLockUtils2.openSterilamp();
			}
		}, 1);
		return 0;
	}
	private int closeSterilamp() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mLockUtils2.closeSterilamp();
			}
		}, 1);
		return 0;
	}
	private int openBox(final int boxnum) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				write_read_door(true, boxnum, BOX_AMOUNT);
			}
		}, 1);
		return 0;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void appendRunLog(String log){
		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("HH:mm:ss");     
		Date curDate =  new Date(System.currentTimeMillis());  

		String   str   =   formatter.format(curDate);
		Message msg = new Message();
		msg.what = RUN_LOG;
		msg.obj = str+" "+log+"\n";
		handlerShow.sendMessage(msg);
	}

	void checkBoxTags(final int number) {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

			    List<String> tagsList = new ArrayList<String>();
				gridIdInventory(number, tagsList);
				Message msg = new Message();
				msg.what = SET_TAG_CNT;
				msg.arg1 = number;
				msg.arg2 = tagsList.size();
				handlerShow.sendMessage(msg);

				appendRunLog("btn_check_box_"+number+" "+tagsList.size());
			}
		}, 0);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			switch (v.getId()) {
			case R.id.test_layout_id:
				break;

			default:
				try {
					((TextView)v).setTextColor(0xFFFFFFFF);
				} catch (Exception e) {
					// TODO: handle exception
				}
				v.setBackgroundResource(R.drawable.bgd_half_transparent_yuanjiao);
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
			v.setBackgroundResource(R.drawable.bgd_transparent_yuanjiao);
			try {
				((TextView)v).setTextColor(0xFF000000);
			} catch (Exception e) {
				// TODO: handle exception
			}
			switch (v.getId()) {
			case R.id.btn_open_box_1:
				openBox(1);
				break;
			case R.id.btn_open_box_2:
				openBox(2);
				break;
			case R.id.btn_open_box_3:
				openBox(3);
				break;
			case R.id.btn_open_box_4:
				openBox(4);
				break;
			case R.id.btn_open_box_5:
				openBox(5);
				break;
			case R.id.btn_open_box_6:
				openBox(6);
				break;
			case R.id.btn_open_box_7:
				openBox(7);
				break;
			case R.id.btn_open_box_8:
				openBox(8);
				break;
			case R.id.btn_open_all_box:
				openBox(1);
				openBox(2);
				openBox(3);
				openBox(4);
				openBox(5);
				openBox(6);
				openBox(7);
				openBox(8);
				break;
			case R.id.btn_check_box_1:
				tv_check_box_1.setText(R.string.start_check);
				checkBoxTags(1);
				break;
			case R.id.btn_check_box_2:
				tv_check_box_2.setText(R.string.start_check);
				checkBoxTags(2);
				break;
			case R.id.btn_check_box_3:
				tv_check_box_3.setText(R.string.start_check);
				checkBoxTags(3);
				break;
			case R.id.btn_check_box_4:
				tv_check_box_4.setText(R.string.start_check);
				checkBoxTags(4);
				break;
			case R.id.btn_check_box_5:
				tv_check_box_5.setText(R.string.start_check);
				checkBoxTags(5);
				break;
			case R.id.btn_check_box_6:
				tv_check_box_6.setText(R.string.start_check);
				checkBoxTags(6);
				break;
			case R.id.btn_check_box_7:
				tv_check_box_7.setText(R.string.start_check);
				checkBoxTags(7);
				break;
			case R.id.btn_check_box_8:
				tv_check_box_8.setText(R.string.start_check);
				checkBoxTags(8);
				break;
			case R.id.btn_check_bookcase:
				checkBoxTags(0);
				break;
			case R.id.btn_open_light:
				openLight();
				break;
			case R.id.btn_close_light:
				closeLight();
				break; 
			case R.id.btn_open_sterilamp:
				openSterilamp();
				break;
			case R.id.btn_close_sterilamp:
				closeSterilamp();
				break;
			
			case  R.id.btn_open_lock_serial:
				{
					if(mLockUtils.isLockDevOpen())
					{
						if (servoThrd != null && servoThrd.isAlive())
						{
							b_servoThrdRun = false;
							try {
								servoThrd.join();
								servoThrd=null;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						mLockUtils.destroyLock();
						btn_open_lock_serial.setText(R.string.closed);
						
					}else
					{
						
						int ret=mLockUtils.initLock(sn_serialPortLock.getSelectedItem().toString());
						
						if(ret>0)
						{
							initServo();
							btn_open_lock_serial.setText(R.string.opened);
						}
						else
						{
							btn_open_lock_serial.setText(R.string.closed);
						}
						
					}
				}
				break;
			case R.id.btn_open_light_serial:
				{
					if(mLockUtils2.isLightDevOpen())
					{
						mLockUtils2.destroyLight();
						
						btn_open_light_serial.setText(R.string.closed);
					}
					else
					{
						int ret=mLockUtils2.initLight(sn_serialPortLight.getSelectedItem().toString());
					
						if(ret>0)
						{
							btn_open_light_serial.setText(R.string.opened);
						}else
						{
							btn_open_light_serial.setText(R.string.closed);
						}
					}
					
				}
				break;
			case R.id.btn_open_book_serial:
				{
					if(MainActivity.m_reader.isReaderOpen())
					{
						CloseBookCheckDev();
					}else
					{
						OpenDev();
					}
				}
				break;
			case R.id.btn_open_userid_serial:	
				{
					
					if(MainActivity.m_reader2.isReaderOpen())
					{
						CloseUserIdScanfDev();
						
					}else
					{
						OpenUerIdScanRfidDev();
					}
				}
				break;

			default:
				break;
			}
	    	et_code_val.requestFocus();
			break;
		default:
			break;
		}
		return true;
	}
	

    boolean b_bookGridIdInventory_run = false;
    public int gridIdInventory(int boxnum, List<String> boxsInventoryList) {
        while(b_bookGridIdInventory_run);
        b_bookGridIdInventory_run = true;
        int ret = __gridIdInventory(boxnum, boxsInventoryList);

        b_bookGridIdInventory_run = false;
        return ret;
    }

    private int __gridIdInventory(int boxnum, List<String> boxsInventoryList) {
        byte useAnt[] = { 1, 2, 3, 4, 5, 6, 7, 8};
        if(boxnum > 0){
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
        	appendRunLog("open bookcase scanf successfully");
        	btn_open_book_serial.setText(getString(R.string.opened));
        	
        	
        } else 
        {
        	appendRunLog("open bookcase scanf failed");
        	btn_open_book_serial.setText(R.string.closed);
        	
        }
        return ret;
    }
    private void CloseBookCheckDev() {
		if (m_reader.isReaderOpen())
		{
			btn_open_book_serial.setText(getString(R.string.closed));
			m_reader.RDR_SetCommuImmeTimeout();
			m_reader.RDR_Close();
		}
    }

    private int inventoryTags(byte useAnt[], List<String> inventoryUidList) {
        if(m_reader == null) {
            //appendRunLog(new DebugInfo().line(), "rfid init failed");
            return ErrorCode.E_READER_NULL;
        } else if(!m_reader.isReaderOpen()){
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
        if(iret==ApiErrDefinition.NO_ERROR) {
            //Close RF if inventory is successful.
            m_reader.RDR_CloseRFTransmitter();
        }
        long t2 = System.currentTimeMillis();
        int inventory_time_consuming = (int) (t2-t1);
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
            List<InventoryReport> inventoryList = new ArrayList<InventoryReport>();
            inventoryList.clear();
            inventoryUidList.clear();
            for (ISO15693Tag t : tagsList) {
                boolean bTagExist = false;
                String uidStr = GFunction.encodeHexStr(t.uid);
                InventoryReport report = new InventoryReport(uidStr, ""+(int) t.ant_id);

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
        }else
            return iret;
    }

    private void initRFIDuser() {
    	if(AutoLink.equals("TRUE"))
    	{
    		OpenUerIdScanRfidDev();
    	}else
    	{
    		btn_open_userid_serial.setText(getString(R.string.closed));
    	}

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {            	
                if(isUerIdScanRfidDevOpen()) {
                    byte useAnt[] = { 1, 2, 3, 4, 5, 6, 7, 8};
                    List<String> inventoryUidList = new ArrayList<String>();
                    int retInt = inventoryUerRFID(useAnt, inventoryUidList);
                    if(retInt == ErrorCode.NONE_ERROR) {
                        int boxNumb = 0;
                        if(useAnt.length == 1)
                            boxNumb = useAnt[0];
                        if(inventoryUidList.size()>0) {
                            String userRfidStr = inventoryUidList.get(0);
    			            Message msg = mHandler.obtainMessage();
    			            msg.what = INVENTORY_USER;
    			            msg.obj = userRfidStr;
    			            msg.arg2 = boxNumb;
    			            mHandler.sendMessage(msg);
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
        	appendRunLog("open user id scanf successfully");
        	btn_open_userid_serial.setText(getString(R.string.opened));
            
        	return true;
        } else {
        	appendRunLog("open user id scanf failed");
        	btn_open_userid_serial.setText(getString(R.string.closed));
        	
        	return false;
        }
    }
    private void CloseUserIdScanfDev() {
    	if(m_reader2.isReaderOpen()) {
	    	m_reader2.RDR_SetCommuImmeTimeout();
	    	m_reader2.RDR_Close();
	    	btn_open_userid_serial.setText(R.string.closed);
    	}
    }
    
    public boolean isUerIdScanRfidDevOpen() {
        return m_reader2.isReaderOpen();
    }
    public int inventoryUerRFID(byte useAnt[], List<String> inventoryUidList) {
        if(m_reader2 == null) {
            return ErrorCode.E_USER_RFID_NULL;
        } else if(!m_reader2.isReaderOpen()) {
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
        int inventory_time_consuming = (int) (t2-t1);
        if (iret == ApiErrDefinition.NO_ERROR) {
            Object tagReport = m_reader2
                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
            while (tagReport != null) {
                Object obj = tag15693_1443a(tagReport);
                if(obj != null) {
                    tagsList.add(obj);
                }
                tagReport = m_reader2
                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
            }
            List<InventoryReport> inventoryList = new ArrayList<InventoryReport>();
            inventoryUidList.clear();
            for (Object t : tagsList) {
                String uidStr = null;
                int ant_id = 0;
                if (t instanceof ISO15693Tag) {
                    ISO15693Tag tagData = (ISO15693Tag) t;
                    uidStr = GFunction.encodeHexStr(tagData.uid);
                    ant_id = (int) tagData.ant_id;
                } else if (t instanceof ISO14443ATag)  {
                    ISO14443ATag tagData = (ISO14443ATag) t;
                    uidStr = GFunction.encodeHexStr(tagData.uid);
                    ant_id = (int) tagData.ant_id;
                }
                if(uidStr != null) {
                    boolean bTagExist = false;
                    InventoryReport report = new InventoryReport(uidStr, ""+ant_id);
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
        if (iret == ApiErrDefinition.NO_ERROR)
        {
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
    

    final String KEY_LOCK_DEV  = "KEY_LOCK_DEV"; 
    final String KEY_LIGHT_DEV  = "KEY_LIGHT_DEV"; 
    final String KEY_BOOK_DEV  = "KEY_BOOK_DEV"; 
    final String KEY_USERID_DEV  = "KEY_USERID_DEV"; 
    final String KEY_AUTO_LINK="KEY_AUTO_Link";
    
    
    
	private String GetHistoryString(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_WORLD_READABLE);
		return preferences.getString(sKey, "");
	}
	private void saveHistory(String sKey, String val)
	{
		@SuppressWarnings("deprecation")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(sKey, val);
		editor.commit();
	}
}
