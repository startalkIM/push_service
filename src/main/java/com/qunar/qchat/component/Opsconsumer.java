package com.qunar.qchat.component;

import com.qunar.qchat.constants.Config;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.Properties;

public final class Opsconsumer {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Opsconsumer.class);
    private static volatile ConsumerConnector consumer = null;

    private Opsconsumer() {
    }

    static {
        Properties prop = new Properties();

        prop.put("zookeeper.connect", Config.OPS_CONSUMER_ZOOKEEPER_PARAMS);
//        prop.put("zookeeper.connect", "127.0.0.1:2345");
        //组id
        prop.put("group.id", Config.OPS_HOSTS_GROUPID);
        //自动提交消费情况间隔时间
        prop.put("auto.commit.interval.ms", "1000");

        prop.put("fetch.message.max.bytes","5242880");

        prop.put("auto.offset.reset","largest");
        ConsumerConfig consumerConfig = new ConsumerConfig(prop);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
    }

    public static ConsumerConnector getConsumer() {
        if (consumer == null) {
            synchronized (Opsconsumer.class) {
                if (consumer == null) {
                    new Opsconsumer();
                }
            }
        }
        return consumer;
    }
}
