package com.microbookcase.service;

public class WebSocketUtil {
    private static final String WS = "ws://";
    public static final String IP = "www.fayshine.cn"; // 本地：2a943044t6.zicp.vip  线上：www.fayshine.cn  测试：www.qukandian573.com
    private static final String PORT = ":80";
    public static final String DEVICE_NAME = "device1";
    private static final String URL = "/device/" + DEVICE_NAME;
    public static final String ROOT_URL = WS + IP + PORT + URL;
    // 书本信息接口 http://www.qukandian573.com/admin/
    public static final String get_code_book = "http://admin.fayshine.cn/out/findProductByBarcode?devicename=" + DEVICE_NAME + "&barcode=";


    // 测试的接口  2a943044t6.zicp.vip
    public static final String TEST_IP = "www.fayshine.cn";
    public static final String get_code_book_test = "http://admin.fayshine.cn/out/findProductByBarcode?devicename=" + DEVICE_NAME + "&barcode=";
    public static final String ROOT_URL_TEST = WS + TEST_IP + PORT + URL;

    public static boolean IS_TEST = false;
}
