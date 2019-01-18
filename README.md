#QTalk push服务

IM后端的push服务，支持IOS apns协议推送，android 小米，华为，魅族，oppo厂商推送
push服务支持qtalk消息push，同时也支持接入自己的push服务
##需要配置如下：
###kafka配置
ops.producer.broker.connect.params=
<br>ops.consumer.zookeeper.params=
<br>ops.kafka.aeskey=
<br>ops.hosts.groupid=
<br>ops.kafka.threadcount=
###topic配置
ops.spool.message.topic.qtalk_chat=单人消息topic
<br>ops.spool.message.topic.qtalk_group=群消息topic

##Android和IOS证书配置
如果需要使用服务支持的push，Android需要自己去小米和华为开发平台注册自己应用的app_key,Ios需要生产签名证书，配置如下：
###ios push 证书
ios_push_cer_qtalk=线上证书
<br>ios_push_cer_qtalk_beta=beta证书
###Android配置
adr.qtalk.pkgname=应用包名
<br>adr.mipush.qtalk.key=mipush key
<br>adr.hwpush.qtalk.key=hwpush key

##私有化配置
如果要使用自己的push服务，那么请自定义一接口，配置到下面
###私有化push接口
private.push.url=
###接口参数如下：
map.put("From", ");//发送人/群
<br>map.put("To", "");//接收人
<br>map.put("Body", "");//消息内容(单人【消息内容】；群【说话人：消息内容】)
<br>map.put("Mtype", 1);//消息类型
<br>map.put("Message", "xml"");//原始消息，可自己解析自定义

## 问题反馈
**qchat@qunar.com（邮件）**