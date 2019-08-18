package space.pankui.kafka.example;

import org.apache.kafka.clients.consumer.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pankui
 * @date 2019-08-18
 * <pre>
 *
 * </pre>
 */
public class ConsumerClientExample {

    /**
     * 手动提交偏移量
     */
    public static void manualCommintClient() {
        Properties props = new Properties();
        //kafka broker列表
        props.put("bootstrap.servers", "172.23.3.19:9092,172.23.3.19:9093");
        //consumer group id
        props.put("group.id", "yongliang");
        //手动提交offset
        props.put("enable.auto.commit", "false");
        //earliest表示从最早的偏移量开始拉取，latest表示从最新的偏移量开始拉取，none表示如果没有发现该Consumer组之前拉取的偏移量则抛异常。默认值latest。
        props.put("auto.offset.reset", "earliest");
        //key和value的字符串反序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        //consumer订阅topictest1主题，同时消费多个主题用逗号隔开
        consumer.subscribe(Arrays.asList("kafkatopic"));
        //每次最少处理10条消息后才提交
        final int minBatchSize = 10;
        //用于保存消息的list
        List<ConsumerRecord<String, String>> bufferList = new ArrayList<ConsumerRecord<String, String>>();
        while (true) {
            System.out.println("--------------start pull message---------------");
            long starttime = System.currentTimeMillis();
            //poll方法需要传入一个超时时间，当没有可以拉取的消息时先等待，
            //如果已到超时时间还没有可以拉取的消息则进行下一轮拉取，单位毫秒
            ConsumerRecords<String, String> records = consumer.poll(1000);
            long endtime = System.currentTimeMillis();
            long tm = (endtime - starttime) / 1000;
            System.out.println("--------------end pull message and times=" + tm + "s -------------");

            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("partition = %d, offset = %d, key = %s, value = %s%n", record.partition(), record.offset(), record.key(), record.value());
                bufferList.add(record);
            }
            System.out.println("--------------buffer size->" + bufferList.size());
            //如果读取到的消息满了10条, 就进行处理
            if (bufferList.size() >= minBatchSize) {
                System.out.println("******start deal message******");
                try {
                    //当前线程睡眠1秒钟，模拟消息处理过程
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("manual commint offset start...");
                //处理完之后进行提交
                consumer.commitSync();
                //清除list, 继续接收
                bufferList.clear();
                System.out.println("manual commint offset end...");
            }
        }
    }

    /**
     * 自动提交偏移量
     */
    public static void autoCommintClient() {
        Properties props = new Properties();
        //kafka broker列表
        props.put("bootstrap.servers", "192.168.43.22:9092,192.168.43.23:9092,192.168.43.24:9092");
        props.put("group.id", "newConsumerGroup");
        //自动提交
        props.put("enable.auto.commit", "true");
        //自动提交时间间隔1000毫秒
        props.put("auto.commit.interval.ms", "1000");
        //earliest表示从最早的偏移量开始拉取，latest表示从最新的偏移量开始拉取，none表示如果没有发现该Consumer组之前拉取的偏移量则抛异常。默认值latest。
        props.put("auto.offset.reset", "earliest");
        //key和value的字符串反序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        //consumer订阅topictest1主题，同时消费多个主题用逗号隔开
        consumer.subscribe(Arrays.asList("kafkatopic"));
        while (true) {
            //poll方法需要传入一个超时时间，当没有可以拉取的消息时先等待，
            //如果已到超时时间还没有可以拉取的消息则进行下一轮拉取，单位毫秒
            ConsumerRecords<String, String> records = consumer.poll(1000);
            //处理拉取过来的消息
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("partition = %d, offset = %d, key = %s, value = %s%n", record.partition(), record.offset(), record.key(), record.value());
            }

        }
    }

    public static void main(String[] args) {
        //自动提交offset
//        autoCommintClient();
        //手动提交offset
        manualCommintClient();
    }
}
