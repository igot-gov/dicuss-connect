package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.HubPost;

@Repository
public interface HubPostRepository extends MongoRepository<HubPost, String> {

	public HubPost findByKey(String key);

}
