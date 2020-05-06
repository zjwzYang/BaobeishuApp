package com.microbookcase.service;

public class WebSocketUtil {
    private static final String WS = "ws://";
    public static final String IP = "2a943044t6.zicp.vip"; // 本地：2a943044t6.zicp.vip  线上：www.fayshine.cn
    private static final String PORT = ":80";
    public static final String DEVICE_NAME = "device2";
    private static final String URL = "/device/" + DEVICE_NAME;
    public static final String ROOT_URL = WS + IP + PORT + URL;

    // 书本信息接口
    public static final String get_code_book = "http://192.168.124.56:9090/out/findProductByBarcode?devicename=" + DEVICE_NAME + "&barcode=";
}
