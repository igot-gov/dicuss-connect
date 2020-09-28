package com.eagle.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.eagle.hubnotifier.model.HubUser;

public interface HubUserRepository extends MongoRepository<HubUser, String> {

	public HubUser findByKey(String key);
}
