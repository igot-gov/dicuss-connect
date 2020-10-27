package com.eagle.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.eagle.hubnotifier.model.HubCategory;

@Repository
public interface HubCategoryRepository extends MongoRepository<HubCategory, String> {
	public HubCategory findByKey(String key);
}
