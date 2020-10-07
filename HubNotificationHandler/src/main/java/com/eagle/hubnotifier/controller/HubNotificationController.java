package com.eagle.hubnotifier.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.eagle.hubnotifier.service.interfaces.NotifyHookService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/v1/")
public class HubNotificationController {
	Logger logger = LogManager.getLogger(HubNotificationController.class);

	@Autowired
	private NotifyHookService notifyService;

	
	@PostMapping(value = "/handleNotification", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = {
			MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<HttpStatus> handleHubNotification(@RequestParam MultiValueMap<String, String> paramMap){
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		notifyService.handleNotifiyRestRequest(mapper.convertValue(paramMap, Map.class));
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
}
