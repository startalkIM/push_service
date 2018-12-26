package com.qunar.qchat.model.response;

public class HwPushSendResult {

    /**
     *
     80000000 成功
     80000003 终端不在线
     80000004 应用已卸载
     80000005 响应超时
     80000006 无路由，终端未连接过push
     80000007 终端在其他大区，不在中国大陆使用push
     80000008 路由不正确，可能终端切换push服务器
     80100000 参数检查，部分参数错误，正确token已下发。返回的JSON体样例如下：
     {"success": 1,
     "failure": 1,
     "illegal_tokens": [
     "1f1f1f1f1f1f1f1f0000002095000001"
     ]}
     注意：以上内容以字符串的形式填充到应答的msg中。
     80100002 不合法的token列表
     80100003 不合法的payload
     80100004 不合法的超时时间
     80300002 无权限下发消息给参数中的token列表
     81000001 内部错误
     */
    public int code;
    public String msg;

    public String requestId;
    public String ext;

    @Override
    public String toString() {
        return "HwPushSendResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", requestId='" + requestId + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }
}
