package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.HubPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubPostRepository extends MongoRepository<HubPost, String> {

    public HubPost findByKey(String key);

}
