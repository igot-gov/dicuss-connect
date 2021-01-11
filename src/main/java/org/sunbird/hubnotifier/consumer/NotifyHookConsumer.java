package org.sunbird.hubnotifier.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.sunbird.hubnotifier.service.interfaces.NotifyHookService;

import java.util.Map;

@Service
public class NotifyHookConsumer {

    Logger logger = LogManager.getLogger(NotifyHookConsumer.class);

    @Autowired
    private NotifyHookService notifyHook;


    @KafkaListener(id = "id0", groupId = "notifyHookTopic-consumer", topicPartitions = {@TopicPartition(topic = "${kafka.topics.incoming.notify}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        ObjectMapper mapper = new ObjectMapper();
        String message = String.valueOf(data.value());
        Map<String, Object> objectMap;
        try {
            objectMap = mapper.readValue(message, Map.class);
            notifyHook.handleNotifyKafkaTopicRequest(objectMap);
        } catch (Exception ex) {
            logger.error("Error while deserialization the object value", ex);
        }
    }
}
