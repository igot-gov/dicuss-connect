package org.sunbird.hubnotifier.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.sunbird.hubnotifier.model.TopicPoster;

import java.util.List;

@Repository
public interface TopicPostersRepositoy extends MongoRepository<TopicPoster, String> {

    public List<TopicPoster> findByKey(String key);
}
