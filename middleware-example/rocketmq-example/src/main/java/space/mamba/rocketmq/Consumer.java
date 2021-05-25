package space.mamba.rocketmq;


import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author pankui
 * http://rocketmq.apache.org/docs/simple-example/
 */
public class Consumer {

    /**
     * push模式是由consumer把轮询过程封装.
     * 并注册MessageListener监听器,取到消息后调用MessageListener的consumeMessage()来消费,
     * 对用户而言,感觉消息是被推送过来的.
     *
     * @param args
     * @throws MQClientException
     */
    public static void main(String args[]) throws MQClientException {
        /**
         * 一个应用创建一个Consumer,由应用来维护此对象,可以设置为全局对象或者单例
         * 注意：ConsumerGroupName需要由应用来保证唯一
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name");
        consumer.setNamesrvAddr("192.168.1.108:9876");
        consumer.setInstanceName("Consumer");

        /**
         * 订阅指定topic下tags分别等于TagA或TagC或TagD
         */
        consumer.subscribe("TopicTest1", "TagA || TagC || TagD");
        /**
         * 订阅指定topic下所有消息
         * 注意：一个consumer对象可以订阅多个topic
         */
        consumer.subscribe("TopicTest2", "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            /**
             * 默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
             */
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs.size());

                MessageExt msg = msgs.get(0);
                if (msg.getTopic().equals("TopicTest1")) {
                    // 执行TopicTest1的消费逻辑
                    if (msg.getTags() != null && msg.getTags().equals("TagA")) {
                        // 执行TagA的消费
                        System.out.println(new String(msg.getBody()));
                    } else if (msg.getTags() != null && msg.getTags().equals("TagC")) {
                        // 执行TagC的消费
                    } else if (msg.getTags() != null && msg.getTags().equals("TagD")) {
                        // 执行TagD的消费
                    }
                } else if (msg.getTopic().equals("TopicTest2")) {
                    System.out.println(new String(msg.getBody()));
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化,初始化一次即可
         */
        consumer.start();

        System.out.println("Consumer Started.");
    }
}