package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.TopicFollower;

@Repository
public interface TopicFollowerRepository extends MongoRepository<TopicFollower, String> {
	public TopicFollower findByKey(String key);
}
