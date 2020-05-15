package com.microbookcase.service;


import android.text.TextUtils;

public class WebSocketMessageBean {
    private String message;
    private String openId;
    private String action;
    private Integer boxId;
    private String[] data;
    private String barcodeList;
    private String status;
    private String borrowedOrderList;
    private String notBarcode;

    public WebSocketMessageBean() {
    }

    public String getBorrowedOrderList() {
        return borrowedOrderList;
    }

    public void setBorrowedOrderList(String borrowedOrderList) {
        this.borrowedOrderList = borrowedOrderList;
    }

    public String getNotBarcode() {
        return notBarcode;
    }

    public void setNotBarcode(String notBarcode) {
        this.notBarcode = notBarcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getBoxId() {
        return this.boxId;
    }

    public void setBoxId(Integer boxId) {
        this.boxId = boxId;
    }

    public String[] getData() {
        return this.data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public String getBarcodeList() {
        return barcodeList;
    }

    public void setBarcodeList(String barcodeList) {
        this.barcodeList = barcodeList;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    public static WebSocketMessageBean parse(String msg) throws JSONException {
//        JSONObject obj = (JSONObject) JSON.parse(msg);
//        WebSocketMessageBean bean = new WebSocketMessageBean();
//        bean.setAction(obj.getString("action"));
//        bean.setBoxId(obj.getInteger("boxId"));
//        JSONArray arr = obj.getJSONArray("data");
//        if (arr != null) {
//            bean.setData(arr.toArray(new String[0]));
//        }
//        return bean;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (action != null) {
            sb.append("\"action\":\"").append(action).append("\",");
        }
        if (openId != null) {
            sb.append("\"openId\":\"").append(openId).append("\",");
        }
        if (boxId != null) {
            sb.append("\"boxId\":").append(boxId).append(",");
        }
        if (status != null) {
            sb.append("\"status\":\"").append(status).append("\",");
        }
        if (borrowedOrderList != null) {
            sb.append("\"borrowedOrderList\":\"").append(borrowedOrderList).append("\",");
        }
        if (notBarcode != null) {
            sb.append("\"notBarcode\":\"").append(notBarcode).append("\",");
        }
        if (data != null && data.length > 0) {
            sb.append("\"data\":[");
            for (int i = 0; i < data.length; i++) {
                sb.append("\"").append(data[i]).append("\"");
                if (i < data.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("],");
        }
        if (!TextUtils.isEmpty(barcodeList)) {
            sb.append(barcodeList);
        }

        return sb.substring(0, sb.length() - 1) + "}";
    }


    public static void main(String[] args) {
        WebSocketMessageBean bean = new WebSocketMessageBean();
        bean.setAction("open_box");
        bean.setBoxId(1);
        bean.setData(new String[]{"adf", "asdf", "asdfasf"});
        System.err.println(bean.toString());
    }
}
