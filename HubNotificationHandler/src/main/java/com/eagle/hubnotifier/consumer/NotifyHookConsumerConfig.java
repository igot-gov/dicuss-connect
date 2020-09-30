package com.eagle.hubnotifier.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@PropertySource(value = { "classpath:application.properties" })
@Configuration
@EnableKafka
public class NotifyHookConsumerConfig {

//	@Value("${spring.kafka.bootstrap.servers}")
//	private String serverConfig;
//
//	@Value("${kafka.consumer.config.group_id}")
//	private String groupId;
//
//	public ConsumerFactory<String, Map> kafkaConsumerFactory() {
//		JsonDeserializer<Map> deserializer = new JsonDeserializer<>(Map.class);
//
//		deserializer.addTrustedPackages("*");
//		deserializer.setUseTypeMapperForKey(true);
//
//		Map<String, Object> props = new HashMap<>();
//		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverConfig);
//		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
//	}
//
//	@Bean
//	public ConcurrentKafkaListenerContainerFactory<String, Map> incomingKafkaListenerContainerFactory() {
//		ConcurrentKafkaListenerContainerFactory<String, Map> factory = new ConcurrentKafkaListenerContainerFactory<>();
//		factory.setConsumerFactory(kafkaConsumerFactory());
//		return factory;
//	}
}
