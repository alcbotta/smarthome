package com.github.alcbotta.smarthome;

import java.lang.reflect.Type;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.elasticsearch.client.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.common.transport.TransportAddress;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");
        FlinkKafkaConsumer<String> kafkaSource =
                new FlinkKafkaConsumer<>("myTopic", new SimpleStringSchema(), properties);
        DataStream<String> stream = see.addSource(kafkaSource);
        DataStream<JsonObject> jsonStream = stream.map(new MapFunction<String, JsonObject>() {

            /**
            *
            */
            private static final long serialVersionUID = 6753530645754895146L;

            @Override
            public JsonObject map(String value) throws Exception {
                try {
                    return new Gson().fromJson(value, JsonObject.class);
                } catch (Exception e) {
                    return new JsonObject();
                }
            }

        });

        // https://ci.apache.org/projects/flink/flink-docs-stable/dev/stream/operators/
        // DataStream<JsonObject> keydStream = jsonStream.keyBy("sensorID");
        KeyedStream<JsonObject, String> keydStream =
                jsonStream.keyBy(new KeySelector<JsonObject, String>() {

                    /**
                    *
                    */
                    private static final long serialVersionUID = 8313418459965669825L;

                    @Override
                    public String getKey(JsonObject value) throws Exception {
                        if (value.has("sensorID")) {
                            return value.get("sensorID").getAsString();
                        }
                        return null;
                    }
                });

        keydStream.process(new KeyedProcessFunction<String, JsonObject, JsonObject>() {

            /**
            *
            */
            private static final long serialVersionUID = 1L;

            @Override
            public void processElement(JsonObject value,
                    KeyedProcessFunction<String, JsonObject, JsonObject>.Context ctx,
                    Collector<JsonObject> out) throws Exception {

                String msg = String.format("value = %s\n", value.toString());
                // log.info(msg);
                System.out.println(msg);
                out.collect(value);
            }

        });

        // RestHighLevelClient client =
        // new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http"),
        // new HttpHost("localhost", 9201, "http")));

        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost("127.0.0.1", 9200, "http"));
        httpHosts.add(new HttpHost("10.2.3.1", 9200, "http"));

        // use a ElasticsearchSink.Builder to create an ElasticsearchSink
        ElasticsearchSink.Builder<JsonObject> esSinkBuilder =
                new ElasticsearchSink.Builder<JsonObject>(httpHosts,
                        new ElasticsearchSinkFunction<JsonObject>() {
                            /**
                            *
                            */
                            private static final long serialVersionUID = -5778357994917069439L;

                            public IndexRequest createIndexRequest(JsonObject element) {
                                Type type = new TypeToken<Map<String, String>>() {
                                }.getType();
                                Map<String, String> myMap =
                                        new Gson().fromJson(new Gson().toJson(element), type);
                                return Requests.indexRequest()
                                        .index(element.get("sensorType").getAsString())
                                        .type("my-type").source(myMap);
                            }

                            @Override
                            public void process(JsonObject element, RuntimeContext ctx,
                                    RequestIndexer indexer) {
                                indexer.add(createIndexRequest(element));
                            }
                        });


        // configuration for the bulk requests; this instructs the sink to emit
        // after every element, otherwise they would be buffered
        esSinkBuilder.setBulkFlushMaxActions(1);


        // // provide a RestClientFactory for custom configuration on the internally created REST
        // client
        // esSinkBuilder.setRestClientFactory(
        // restClientBuilder -> {
        // restClientBuilder.setDefaultHeaders(...)
        // restClientBuilder.setMaxRetryTimeoutMillis(...)
        // restClientBuilder.setPathPrefix(...)
        // restClientBuilder.setHttpClientConfigCallback(...)
        // }
        // );

        keydStream.addSink(esSinkBuilder.build());


        see.execute("CustomerRegistrationApp");
    }
}
