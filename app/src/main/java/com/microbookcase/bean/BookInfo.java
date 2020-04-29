package com.microbookcase.bean;

/**
 * Created on 2020/4/28 14:20
 * .
 *
 * @author yj
 * @org 浙江房超信息科技有限公司
 */
public class BookInfo {


    /**
     * code : 200
     * msg : 请求成功
     * data : {"barcode":"9787558122811","name":"妈妈，我要和你在一起","status":false}
     */

    private int code;
    private String errorCode;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * barcode : 9787558122811
         * name : 妈妈，我要和你在一起
         * status : false
         */

        private String barcode;
        private String rfId;
        private String name;
        private boolean status;

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getRfId() {
            return rfId;
        }

        public void setRfId(String rfId) {
            this.rfId = rfId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }
}
