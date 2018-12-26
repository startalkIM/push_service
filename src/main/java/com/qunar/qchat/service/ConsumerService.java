package com.qunar.qchat.service;

import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import com.qunar.qchat.component.Opsconsumer;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.constants.MessageType;
import com.qunar.qchat.constants.QMonitorConstants;
import com.qunar.qchat.consumeevent.MessageHandler;
import com.qunar.qchat.utils.ExecutorUtils;
import com.qunar.qchat.utils.JacksonUtils;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ConsumerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);
    private static volatile List<ConsumerParam> cps = new ArrayList<>();

    @Autowired
    private  SpoolMessageService spoolMessageService;

    public ConsumerService() {
        receiveChatSpoolQueue();
        receiveGroupSpoolQueue();
        receive(cps);
    }

    private void receiveChatSpoolQueue() {
        if(TextUtils.isEmpty(Config.OPS_SPOOL_MESSAGE_TOPIC_CHAT)) {
            return;
        }
        ConsumerParam cp = new ConsumerParam(Config.OPS_SPOOL_MESSAGE_TOPIC_CHAT, Config.OPS_KAFKA_THREADCOUNT, (key, msg) -> {
            try {
                if(isFilterMsg(key)){
                    return;
                }
                LOGGER.info("MQ消息key:[{}],msg:[{}] receiveChatSpoolQueue", key, msg);
                QMonitor.recordOne(QMonitorConstants.QTALK_CONSUME);
//                Map<String, Object> ∂ = JacksonUtils.string2Map(msg);
                Map<String, Object> chatMessage = JacksonUtils.string2Obj(msg, Map.class);
                if(chatMessage == null) return;
                spoolMessageService.processChatMessage(key, chatMessage);
            }catch (Exception e){
                LOGGER.error("receiveChatSpoolQueue Exception={} ", e);
                QMonitor.recordOne("ReceiveChatSpoolQueueException");
            }
        }, ExecutorUtils.newLimitedCachedThreadPool());
        cps.add(cp);
    }

    private void receiveGroupSpoolQueue() {
        if(TextUtils.isEmpty(Config.OPS_SPOOL_MESSAGE_TOPIC_GROUP)) {
            return;
        }
        ConsumerParam cp = new ConsumerParam(Config.OPS_SPOOL_MESSAGE_TOPIC_GROUP, Config.OPS_KAFKA_THREADCOUNT, (key, msg) -> {
            try {
                if(isFilterMsg(key)){
                    return;
                }
                LOGGER.info("MQ消息key:[{}],msg:[{}] receiveGroupSpoolQueue", key, msg);
                QMonitor.recordOne(QMonitorConstants.QTALK_GROUP_CONSUME);
//                Map<String, Object> ∂ = JacksonUtils.string2Map(msg);
                Map<String, Object> chatMessage = JacksonUtils.string2Obj(msg, Map.class);
                if(chatMessage == null) return;
                spoolMessageService.processChatMessage(key, chatMessage);
            }catch (Exception e){
                LOGGER.error("receiveGroupSpoolQueue Exception={} ", e);
                QMonitor.recordOne("ReceiveGroupSpoolQueueException");
            }
        }, ExecutorUtils.newLimitedCachedThreadPool());
        cps.add(cp);
    }

    private boolean isFilterMsg(String key) {
        if(MessageType.READMARK.equalsIgnoreCase(key)/* || MessageType.REVOKE.equalsIgnoreCase(key)*/
                || MessageType.MASK.equalsIgnoreCase(key)
                || MessageType.TRANS.equalsIgnoreCase(key)
                || key.startsWith(MessageType.READMARK)
                || key.startsWith(MessageType.MASK)){
            return true;
        }
        return false;
    }

    private void receive(List<ConsumerParam> consumerParamList) {
        if (CollectionUtils.isEmpty(consumerParamList)) {
            return;
        }
        //设置处理消息线程数，线程数应小于等于partition数量，若线程数大于partition数量，则多余的线程则闲置，不会进行工作
        //key:topic名称 value:线程数
        Map<String, Integer> topicCountMap = new HashMap<>();

        for (ConsumerParam cp : consumerParamList) {
            topicCountMap.put(cp.topic, cp.consumeThreadCount);
        }
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = Opsconsumer.getConsumer().createMessageStreams(topicCountMap);

        for (final ConsumerParam cp : consumerParamList) {
            //获取对应topic的消息队列
            List<KafkaStream<byte[], byte[]>> streamList = consumerMap.get(cp.topic);
            //创建线程池用于消费队列
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            LOGGER.info("ThreadId {}, topic ({}), stream list count: {}", Thread.currentThread().getId(), cp.topic, streamList.size());
            for (final KafkaStream<byte[], byte[]> stream : streamList) {
                executorService.execute(() -> {
                    LOGGER.info("executorService 线程内部开始, id: {}", Thread.currentThread().getId());
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    while(it.hasNext()) {
                        MessageAndMetadata<byte[], byte[]> mam = it.next();
                        byte[] keyBytes = mam.key();
                        final byte[] msgBytes = mam.message();
                        //
                        String key = StringUtils.newStringUtf8(keyBytes);
                        String msg = StringUtils.newStringUtf8(msgBytes);

                        //必须成功消费
                        successConsumeMsg(cp, key, msg);
//                        cp.executor.execute(() -> cp.messageHandler.handle(msgBytes));
                    }
                    LOGGER.info("executorService 线程内部结束, id: {}", Thread.currentThread().getId());
                });
            }
        }
    }

    /**
     * 必须成功消费
     * 解决线程池满导致的执行退出问题
     */
    private void successConsumeMsg(ConsumerParam cp, String key, String msg){
        boolean isSuccess = false;
        int count=0;
        do {
            try {

                QMonitor.recordOne("PUSH_RECIEVE_TOTAL_MSG");
                cp.executor.execute(() -> cp.messageHandler.handle(key, msg));
                isSuccess= true;
            } catch (Exception e) {
                QMonitor.recordOne(QMonitorConstants.THREA_POOL_EXCEPTION);
                LOGGER.warn("MQ消息topic:[{}], key:[{}],msg:[{}]执行异常，失败重试次数{} ", cp.topic,key, msg, ++count, e);
                try {
                    Thread.sleep(5000l);
                } catch (InterruptedException e1) {
                    //理论上不应该执行到这
                    LOGGER.error("sleep 竟然异常", e1);
                }
            }
        } while (!isSuccess);
    }

    private static class ConsumerParam {
        private String topic;
        private int consumeThreadCount;
        private MessageHandler messageHandler;
        private Executor executor;

        public ConsumerParam(String topic, int consumeThreadCount, MessageHandler messageHandler, Executor executor) {
            this.topic = topic;
            this.consumeThreadCount = consumeThreadCount;
            this.messageHandler = messageHandler;
            this.executor = executor;
        }
    }

}
