# QTalk push服务

IM后端的push服务，支持IOS apns协议推送，android 小米，华为，魅族，oppo厂商推送
push服务支持qtalk消息push，同时也支持接入自己的push服务


## 能力范围

* 如果您自己有build app的能力，那么请自己生成、部署app,并[自己部署push](## 私有化部署push)；
* 您有已经在应用的push服务器，需要[复用自己的push服务器](## 自有push系统);
* 如果您并不打算部署app，只想用开源版本的startalk，那么可以接入我们的[公共push服务器](## 共用push系统)

（因为app push证书在团队内），
此时您需要支付一定的服务器公摊费用，但是肯定比短信便宜的多！详情可以联系我们的客服人员。


## 共用push系统
对于已经私有化部署的用户来说比较简单，不需要下载，更新服务，只需要按照如下步骤执行即可.
全程只需要修改一个配置文件，重启下服务即可。
一共有四步：
```
第一步：
$ vim /startalk/tomcat/push_service/webapps/push_service/WEB-INF/classes/app.properties

第二步：
在配置文件中，找到:
qtalk_push_url=

将您收到的邮件或者信息中的公有url填入，填写完成之后应该看起来是这样的：
(没有空格)
qtalk_push_url=https://xxx.xx.com/xxx/push/sendPush.xxx

第三步：
在配置文件中，找到：
qtalk_push_key=

将您收到的邮件或者信息中的push key填入，填写完成之后应该看起来是这样的：
(没有空格)
qtalk_push_key=xxxxxxxx

第四步：
杀掉服务，并重启服务

$./startalk/tomcat/push_service/startup.sh

```

## 私有化部署push

全程只需要修改一个配置文件，重启下服务即可。

文件位置在：

/startalk/tomcat/push_service/webapps/push_service/WEB-INF/classes/app.properties

## Android和IOS证书配置

如果需要使用服务支持的push，Android需要自己去小米和华为开发平台注册自己应用的app_key,Ios需要生产签名证书，配置如下：

### ios push 证书

```
ios_push_cer_qtalk=线上证书所在路径
ios_push_cer_qtalk_beta=beta证书所在路径
```

### Android配置

```
adr.qtalk.pkgname=应用包名
adr.mipush.qtalk.key=mipush key(是个文本)
adr.hwpush.qtalk.key=hwpush key(是个文本)
```

## 私有化配置

如果要使用自己的push服务，那么请自定义一接口，配置到下面

## 自有push系统

全程只需要修改一个配置文件，重启下服务即可。

文件位置在：

/startalk/tomcat/push_service/webapps/push_service/WEB-INF/classes/app.properties

```
private.push.url=这里指向您的push服务的接收方法
```

此时，push服务将作为客户端，将需要发送的push内容推向你现有的push服务器。

你服务器中需要实现上面提到的方法。其中：

http request 的 Header中，

```
Content-Type:application/json
```

post body是个json,内容为类似这样的形式:

```
{
    "From":"ming.xiao",
    "To":"hong.xiao",
    "Body":"明天去哪儿玩儿？",
    "Mtype":1,
    "Message":"xml"
}

```


## 问题反馈

**qchat@qunar.com（邮件）**
