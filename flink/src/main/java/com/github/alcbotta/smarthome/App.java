package com.github.alcbotta.smarthome;

import java.util.Properties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;


/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");
        FlinkKafkaConsumer<String> kafkaSource =
                new FlinkKafkaConsumer<>("myTopic", new SimpleStringSchema(), properties);
        DataStream<String> stream = see.addSource(kafkaSource);
        stream.print();
        see.execute("CustomerRegistrationApp");
    }
}
