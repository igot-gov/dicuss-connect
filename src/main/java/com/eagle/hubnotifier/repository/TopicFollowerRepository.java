package com.eagle.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.eagle.hubnotifier.model.TopicFollower;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicFollowerRepository extends MongoRepository<TopicFollower, String> {
	public TopicFollower findByKey(String key);
}
