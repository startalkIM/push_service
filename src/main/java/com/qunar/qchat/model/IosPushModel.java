package com.qunar.qchat.model;

/**
 * create by hubo.hu (lex) at 2018/12/27
 */
public class IosPushModel {

    public String certPath;
    public String certPwd;

    public String token;
    public String teamId;
    public String tokenId;

    public IosPushModel(String certPath, String certPwd) {
        this.certPath = certPath;
        this.certPwd = certPwd;
    }

    public IosPushModel(String keyPath, String teamId, String keyId ){
        this.token = keyPath;
        this.teamId = teamId;
        this.tokenId = keyId;
    }

}
