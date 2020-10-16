package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.HubFollowers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubFollowersRepository extends MongoRepository<HubFollowers, String> {

    public HubFollowers findByKey(String key);

}
