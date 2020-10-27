package com.eagle.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.eagle.hubnotifier.model.HubPost;

@Repository
public interface HubPostRepository extends MongoRepository<HubPost, String> {

	public HubPost findByKey(String key);

}
