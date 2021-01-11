package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.HubTopic;

@Repository
public interface HubTopicRepository extends MongoRepository<HubTopic, String> {

    public HubTopic findByKey(String key);
}
