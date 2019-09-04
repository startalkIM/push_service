package com.qunar.qchat.model.response;

/**
 * create by hubo.hu (lex) at 2019/7/23
 */
public class VivoAuthTokenResult {


    /**
     * result : 0
     * authToken : 24ojds98fu3jqrioeu982134jieds9fq43u09uaf
     * desc : 请求成功
     *
     result  desc
     10200  appId 不能为空
     10201  appKey 不能为空
     10202  appKey 不合法
     10203  timestamp 不能为空
     10204  sign 不能为空
     10205  appId 不存在
     10206  sign 不正确
     10207  timestamp 不合法
     10250  认证接口超过调用次数限制
     */
    public int result;
    public String authToken;
    public String desc;

    @Override
    public String toString() {
        return "VivoAuthTokenResult{" +
                "result=" + result +
                ", authToken='" + authToken + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
