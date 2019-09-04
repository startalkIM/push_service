#QTalk push服务

IM后端的push服务，支持IOS apns协议推送，android 小米，华为，魅族，oppo，vivo厂商推送
<br>push服务支持qtalk消息push，同时也支持接入自己的push服务
<br>##需要配置如下：
<br>###kafka配置
<br>ops.producer.broker.connect.params=
<br>ops.consumer.zookeeper.params=
<br>ops.kafka.aeskey=
<br>ops.hosts.groupid=
<br>ops.kafka.threadcount=
<br>###topic配置
<br>ops.spool.message.topic.qtalk_chat=单人消息topic
<br>ops.spool.message.topic.qtalk_group=群消息topic

##Android和IOS证书配置
<br>如果需要使用服务支持的push，Android需要自己去小米和华为开发平台注册自己应用的app_key,Ios需要生产签名证书，配置如下：
<br>###ios push 证书
<br>ios_push_cer_qtalk=线上证书
<br>ios_push_cer_qtalk_beta=beta证书
<br>###Android配置
<br>adr.qtalk.pkgname=应用包名
<br>adr.mipush.qtalk.key=mipush key
<br>adr.hwpush.qtalk.key=hwpush key

##私有化配置
<br>如果已经有了自己的push服务，那么可以自定义自己的接口，如果配置则会请求下面的url
<br>###私有化push接口
<br>private.push.url=
<br>###接口参数如下：
<br>map.put("From", ");//发送人/群
<br>map.put("To", "");//接收人
<br>map.put("Body", "");//消息内容(单人【消息内容】；群【说话人：消息内容】)
<br>map.put("Mtype", 1);//消息类型
<br>map.put("Message", "xml"");//原始消息，可自己解析自定义

#使用QTalk/Startalk客户端的情况如要使用push，需要联系客服配置下面信息
<br>qtalk_push_url=
<br>#使用星语push key
<br>qtalk_push_key=

## 问题反馈
**qchat@qunar.com（邮件）**
