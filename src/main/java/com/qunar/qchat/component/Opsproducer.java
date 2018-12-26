package com.qunar.qchat.component;

import com.qunar.qchat.constants.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;


public final class Opsproducer {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Opsconsumer.class);
    private static volatile KafkaProducer<String, String> opsproducer = null;

    private Opsproducer() {
    }

    static {
        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.OPS_PRODUCER_CONNECT_PARAMS);
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024 * 1024 * 5);
        prop.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        opsproducer = new KafkaProducer<>(prop);
    }

    public static KafkaProducer<String, String> getProducer() {
        if (opsproducer == null) {
            synchronized (Opsproducer.class) {
                if (opsproducer == null) {
                    new Opsproducer();
                }
            }
        }
        return opsproducer;
    }
}
