package com.qunar.qchat.model;

/**
 * Created by qitmac000378 on 17/8/10.
 */
public class JsonBaseResult {
    private boolean ret;
    private int errcode;
    private int status;
    private String errmsg;

    public static final boolean SUCCESS = true;
    public static final boolean FAIL = false;

    public static final int SUCCESS_CODE = 0;

    public JsonBaseResult() {
    }

    public JsonBaseResult(boolean ret, int errcode, String errmsg) {
        this.ret = ret;
        this.errcode = errcode;
        this.errmsg = errmsg;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
