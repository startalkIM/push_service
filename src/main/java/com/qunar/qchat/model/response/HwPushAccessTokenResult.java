package com.qunar.qchat.model.response;

public class HwPushAccessTokenResult {
//    access_token	要获取的Access Token。
//    expires_in	Access Token的有效期，以秒为单位。
//    scope	Access Token的访问范围，即用户实际授予的权限列表（用户在授权页面时，有可能会取消掉某些请求的权限）。
//    error	错误码。详细含义请参见下面“HTTP协议错误码”和“业务级错误码”
//    error_description	错误描述信息，用来帮助理解和解决发生的错误。

    public String access_token;
    public long expires_in;
    public String scope;
    /**
     *  1101 invalid request	请求非法
        1102 parameter_required	缺少必须的参数
        1104 unsupported response type	不支持的Response Type
        1105 unsupported grant type	不支持的Grant Type
        1107 access denied	用户或授权服务器拒绝授予数据访问权限
        1201 invalid ticket	非法的ticket
        1202 invalid sso_st	非法的sso_st
     */
    public String error;
    public String error_description;

    @Override
    public String toString() {
        return "HwPushAccessTokenResult{" +
                "access_token='" + access_token + '\'' +
                ", expires_in=" + expires_in +
                ", scope='" + scope + '\'' +
                ", error='" + error + '\'' +
                ", error_description='" + error_description + '\'' +
                '}';
    }
}
