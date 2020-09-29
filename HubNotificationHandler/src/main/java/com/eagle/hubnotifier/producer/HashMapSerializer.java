package com.eagle.hubnotifier.producer;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HashMapSerializer implements Serializer<Map<String, ?>> {
	Logger logger = LogManager.getLogger(HashMapSerializer.class);


	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// Do nothing.
	}

	@Override
	public byte[] serialize(String topic, Map<String, ?> data) {
		byte[] value = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			value = objectMapper.writeValueAsString(data).getBytes();
		} catch (JsonProcessingException e) {
			logger.error("Failed to serialize the data. Exception: ", e);
		}
		return value;
	}

	@Override
	public void close() {
		// Do nothing.
	}


}
