package com.eagle.hubnotifier.service.impl;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.model.NotificationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotifyHandlerServiceImpl {
	Logger logger = LogManager.getLogger(NotifyHandlerServiceImpl.class);

	@Autowired
	private Configuration config;

	@Autowired
	private OutboundRequestHandlerServiceImpl serviceRepo;

	/**
	 * Post to the Notification service
	 *
	 * @param nEvent
	 * @throws Exception
	 */
	public void sendNotification(NotificationEvent nEvent) {
		StringBuilder builder = new StringBuilder();
		builder.append("URL: ").append(config.getNotifyServiceHost()).append(config.getNotifyServicePath());
		builder.append(System.lineSeparator());
		builder.append("Event: ").append(nEvent);
//		if (logger.isDebugEnabled()) {
			logger.info("Sending Notification: {}", builder.toString());
//		}
		try {
			 serviceRepo.fetchResultUsingPost(builder, nEvent);
		} catch (Exception e) {
			logger.error("Exception while posting the data in notification service: ", e);
		}

	}
}
