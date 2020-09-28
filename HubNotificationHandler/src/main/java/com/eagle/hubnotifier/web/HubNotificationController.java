package com.eagle.hubnotifier.web;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eagle.hubnotifier.service.NotifyHookService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v1/")
public class HubNotificationController {
	Logger logger = LogManager.getLogger(HubNotificationController.class);

	@Autowired
	private NotifyHookService notifyService;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/handleNotification", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = {
			MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getUsersCoin(@RequestParam MultiValueMap<String, String> paramMap) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		notifyService.handleNotifiyRestRequest(mapper.convertValue(paramMap, Map.class));
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
}
