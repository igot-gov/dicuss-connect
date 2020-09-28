package com.eagle.hubnotifier.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotifyHookProducer {

	@Value("${kafka.topics.incoming.notify}")
	private String topic;
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	public void push(Object value) {
		kafkaTemplate.send(topic, value);
	}
}
