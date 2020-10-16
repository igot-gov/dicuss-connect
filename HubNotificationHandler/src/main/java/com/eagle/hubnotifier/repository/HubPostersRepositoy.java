package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.HubPosters;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubPostersRepositoy extends MongoRepository<HubPosters, String> {

    public List<HubPosters> findByKey(String key);
}
