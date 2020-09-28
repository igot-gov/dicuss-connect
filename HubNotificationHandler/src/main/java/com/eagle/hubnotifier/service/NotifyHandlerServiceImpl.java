package com.eagle.hubnotifier.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.model.NotificationEvent;

@Service
public class NotifyHandlerServiceImpl {

	@Autowired
	private Configuration config;
	
	
	private RestTemplate restTemplate;
	
	public NotifyHandlerServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	// TODO -- This method should be updated with parameters
	public void sendNotification(NotificationEvent nEvent) {
		// TODO send out rest request to NotificationService
		restTemplate.postForObject( config.getNotifyServiceHost() + config.getNotifyServicePath(), nEvent, ResponseEntity.class);
	}
}
