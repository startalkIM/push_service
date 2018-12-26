package com.qunar.qchat.dao.model;

public class PushInfo {
    private String mac_key;
    private String platname;
    private String pkgname;
    private int push_flag;
    private String os;
    private String version;


    public String getMac_key() {
        return mac_key;
    }

    public void setMac_key(String mac_key) {
        this.mac_key = mac_key;
    }

    public String getPlatname() {
        return platname;
    }

    public void setPlatname(String platname) {
        this.platname = platname;
    }

    public String getPkgname() {
        return pkgname;
    }

    public void setPkgname(String pkgname) {
        this.pkgname = pkgname;
    }

    public int getPush_flag() {
        return push_flag;
    }

    public void setPush_flag(int push_flag) {
        this.push_flag = push_flag;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "PushInfo{" +
                "mac_key='" + mac_key + '\'' +
                ", platname='" + platname + '\'' +
                ", pkgname='" + pkgname + '\'' +
                ", push_flag=" + push_flag +
                ", os='" + os + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
