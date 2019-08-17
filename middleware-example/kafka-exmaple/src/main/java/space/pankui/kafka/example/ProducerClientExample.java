package space.pankui.kafka.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author pankui
 * @date 2019-08-16
 * <pre>
 *      Kafka生产端
 * </pre>
 */
public class ProducerClientExample {

    public static void main(String[] args) {
        Properties properties = new Properties();
        // kafka broker 列表
        properties.put("bootstrap.servers","172.23.3.19:9092,172.23.3.19:9093");
        //acks=1表示Broker接收到消息成功写入本地log文件后向Producer返回成功接收的信号，不需要等待所有的Follower全部同步完消息后再做回应
        properties.put("acks","1");
        //key和value的字符串序列化类
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(properties);
        //用户产生随机数，模拟消息生成
        Random rand = new Random();
        for (int i = 0 ;i < 20; i++) {
            //通过随机数产生一个ip地址作为key发送出去
            String ip = "127.0.0.1" + rand.nextInt(255);
            long runtime = Instant.now().getEpochSecond();
            //组装一条消息内容
            String msg = runtime + "---" + ip;
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("send to kafka->key:" + ip + " value:" + msg);
            //向kafka topictest1主题发送消息
            producer.send(new ProducerRecord<>("topic_new_test_1",ip,msg));

        }
        producer.close();

    }
}
