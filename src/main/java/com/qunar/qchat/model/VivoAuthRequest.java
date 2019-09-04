package com.qunar.qchat.model;

/**
 * create by hubo.hu (lex) at 2019/7/23
 */
public class VivoAuthRequest {
    /**
     * appId : 10004
     * sign : 8424f52fd5eaedc16474e4f702d230d2
     * appKey : 25509283-3767-4b9e-83fe-b6e55ac6243e
     * timestamp : 1501484120000
     */
    public int appId;//用户申请推送业务时生成的 appId
    public String sign;//签名 使用 MD5 算法，字符串 trim 后拼接( appId+appKey+timestamp+appSecret)，然后 通过 MD5 加密得到的值(字母小写)
    public String appKey;//用户申请推送业务时获得的 appKey
    public long timestamp;//Unix 时间戳 做签名用，单位:毫秒，且在 vivo 服务器当前 utc 时间戳前后十分钟区间内。

}
