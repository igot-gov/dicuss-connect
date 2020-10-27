package com.eagle.hubnotifier.consumer;

import java.util.HashMap;

import org.springframework.kafka.support.serializer.JsonDeserializer;

public class HashMapDeserializer extends JsonDeserializer<HashMap<String, ?>> {
	public HashMapDeserializer() {
		super(HashMap.class);
	}
}