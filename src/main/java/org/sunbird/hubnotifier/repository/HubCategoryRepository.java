package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.HubCategory;

@Repository
public interface HubCategoryRepository extends MongoRepository<HubCategory, String> {
	public HubCategory findByKey(String key);
}
