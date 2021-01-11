package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.HubUser;

import java.util.List;

@Repository
public interface HubUserRepository extends MongoRepository<HubUser, String> {

	public HubUser findByKey(String key);

	@Query("{_key:{$in:?0}}")
	public List<HubUser> findByUUIDS(List<String> uuids);
}
