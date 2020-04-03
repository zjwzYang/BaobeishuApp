/**
 * 
 */
package com.wellcom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * @author cloudborn
 *
 */
public class GfpUtils
{
	
	private int fd = 0;
	private int havePermission = 0;
	private int vid = 0x2796; 
	private	int pid = 0x0300;//AXF//0x8195; //0x0201; //ARF
	private Context context;
	private UsbManager mManager =null;
	private UsbDeviceConnection mDeviceConnection =null;

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private static final String USBTAG = "wellcom";


	static {
		System.loadLibrary("WelGfpUtils");
	}
	

	public GfpUtils(Context context) {
		this.context = context;
	}
	
	// ����ָ���豸
	private int FindUSBdevice() {
		Log.i(USBTAG, "start getSystemService");
		this.mManager = ((UsbManager) context.getSystemService(context.USB_SERVICE));
		if (this.mManager == null) {
			return -1;
		}

		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		if (deviceList != null && deviceList.size() != 0) 
		{
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
					context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			context.registerReceiver(this.mUsbReceiver, filter);
			while (deviceIterator.hasNext()) 
			{
				UsbDevice device = deviceIterator.next();
				
				if(device.getVendorId()==vid&&device.getProductId()==pid)
				{
					if (!mManager.hasPermission(device)) 
					{
						mManager.requestPermission(device, mPermissionIntent);
						Log.i(USBTAG, "mManager.requestPermission");
						return -2;
						
					}
					mDeviceConnection = mManager.openDevice(device);

					if (mDeviceConnection != null) {
						fd = mDeviceConnection.getFileDescriptor();
						Log.i(USBTAG, "getFileDescriptor:" + String.valueOf(fd));
					}
				}
				
			}
		} 
		else 
		{
			Log.i(USBTAG, "no usb");
			return -1;
		}
		return 0;
	}

	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			
			if (action.equals(ACTION_USB_PERMISSION)||action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					mDeviceConnection = mManager.openDevice(dev);

					if (mDeviceConnection != null) {
						fd = mDeviceConnection.getFileDescriptor();
						Log.i(USBTAG, "mUsbReceiver"+fd);
					}
					Log.i(USBTAG, "mUsbReceiver");
				}
			}
		}
	};

	public String[] deviceDetect()
	{
		int iRet =0;
		String[] str=new String[2];
		
		iRet = openWelDevice();
		if(iRet !=0)
		{
			str[0]="-1";
			str[1] ="no open";
			return str;
		}
		else
		{
			str[0]="0";
			str[1] ="deviceDetect ok";
			closeWelDevice();
			return str;
		}
		
	}
	public int openWelDevice()
	{
		int iRet =0;
		iRet = FindUSBdevice();
		if(iRet ==0)
			return open(fd, vid,pid);
		else
			return iRet;
	}
	
	public int closeWelDevice()
	{
		close();
		
		if (mDeviceConnection != null) {
			mDeviceConnection.close();
			mDeviceConnection = null;
		}
		return 0;
	}
	
	/*
	 * open
	 * ��(�����в���֮ǰ������ø÷���)
	 * int fd :���(��usb��ȡȨ�޺�õ�)
	 * int vid :usb �豸vid
	 * int pid ��usb �豸pid
	 * *����
	 * int 0 �ɹ� ��0 ʧ��
	 * */
	public native int open(int fd, int vid,int pid);
	
	/*
	 * close
	 * �ر�(���������������ø÷���)
	 * *���� 
	 * int 0 �ɹ� ��0 ʧ��
	 * */
	public native int close();
	
	/*
	 * getVersion
	 * ����豸
	 * * ���� 
	 * String[0] �������
	 * String[1] �汾��Ϣ/������Ϣ
	 * */
	public native String[] getVersion();
	
	
	/*
	 * captureImage
	 * �ɼ�ͼ��
	 * int iTime ��ʱʱ��
	 * * ���� 
	 * String[0] ������� -5 �ɼ�ͼ��ʧ�ܣ�-6 �ϴ�ͼ����λ��ʧ��
	 * String[1] ͼ����Ϣ (ע�ⷵ�ص�ΪBASE64���BMPͼ��)/������Ϣ
	 * */
	public native String[] captureImage(int iTime);
	
	/*
	 * getImageQuality
	 * ��ȡͼ������
	 * String strB64Image :��ͼ��Ϊbmpͼ��BASE64
	 * * ���� 
	 * String[0] �������
	 * String[1] ͼ������/������Ϣ
	 * */
	public native String[]  getImageQuality(String strB64Image);
	
	/*
	 * FPI_fingerDetect
	 * ��ȡ��ָ״̬
	 * * ���� 
	 * String[0] �������
	 * String[1] ״̬��Ϣ 0��̧��  1������/������Ϣ
	 * */
	public native String[] fingerDetect();
	
	/*
	 * ImageToFeature
	 * ͼ��ת����
	 * String strB64Image :��ͼ��Ϊbmpͼ��BASE64
	 * * ���� 
	 * String[0] �������
	 * String[1] ��������  ������ϢBASE64/������Ϣ
	 * */
	public native String[] ImageToFeature(String strB64Image);
	
	/*
	 * getFeatureQuality
	 * ��ȡ��������
	 * String strB64Ftr :������ϢBASE64
	 * * ���� 
	 * String[0] �������
	 * String[1] ��������  0--100/������Ϣ
	 * */
	public native String[]  getFeatureQuality(String strB64Ftr);
	
	/*
	 * FeatureToTemplate
	 * ����תģ��(��3������)
	 * String strB64Ftr1
	 * String strB64Ftr2
	 * String strB64Ftr3
	 * * ���� 
	 * String[0] �������
	 * String[1] ģ����Ϣ/������Ϣ
	 * */
	public native String[] FeatureToTemplate(String strB64Ftr1,String strB64Ftr2,String strB64Ftr3);
	
	/*
	 * getFeature
	 * �ɼ�����
	 * int iTime ��ʱʱ��
	 * * ���� 
	 * String[0] �������
	 * String[1] ������Ϣ ������ϢBASE64/������Ϣ
	 * */
	public native String[] getFeature(int iTime);
	
	/*
	 * getTemplate
	 * �ɼ�ģ��
	 * int iTime ��ʱʱ��
	 * * ���� 
	 * String[0] �������
	 * String[1] ģ����Ϣ       ģ����ϢBASE64/������Ϣ
	 * */
	public native String[] getTemplate(int iTime);
	
	/*
	 * match
	 * �ȶ� (1:1)
	 * String strB64Ftr1
	 * String strB64Ftr2
	 * int ilevel �ȶԵȼ�
	 * * ���� 
	 * String[0] �������
	 * String[1] �ȶԷ���/������Ϣ
	 * */
	public native String[] match(String strB64Ftr1,String strB64Ftr2,int ilevel);
	
	/*
	 * searchMatch
	 * �����ȶ�(1:n)
	 * String strB64Ftr :���ȶԵ�����
	 * int mbCnt  		:ģ�����
	 * String strB64MBGroup :ģ��ռ�
	 * * ���� 
	 * String[0] :�������
	 * String[1] :�ȶԽ�����/������Ϣ
	 * */
	//public native String[] searchMatch(String strB64Ftr,int mbCnt, String strB64MBGroup);
		
}
