package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.HubUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubUserRepository extends MongoRepository<HubUser, String> {

	public HubUser findByKey(String key);

	@Query("{_key:{$in:?0}}")
	public List<HubUser> findByUUIDS(List<String> uuids);
}
