package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.HubTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubTopicRepository extends MongoRepository<HubTopic, String> {

    public HubTopic findByKey(String key);
}
