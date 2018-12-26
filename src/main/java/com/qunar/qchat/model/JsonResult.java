package com.qunar.qchat.model;


public class JsonResult<T> {
    private boolean ret;
    private int errcode;
    private String errmsg;
    private T data;

    public static final boolean SUCCESS = true;
    public static final boolean FAIL = false;

    public static final int SUCCESS_CODE = 0;

    public JsonResult() {
    }

    public JsonResult(boolean ret, int errcode, String errmsg, T data) {
        this.ret = ret;
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.data = data;
    }

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
