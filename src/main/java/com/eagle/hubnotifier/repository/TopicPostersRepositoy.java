package com.eagle.hubnotifier.repository;

import com.eagle.hubnotifier.model.TopicPoster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicPostersRepositoy extends MongoRepository<TopicPoster, String> {

    public List<TopicPoster> findByKey(String key);
}
