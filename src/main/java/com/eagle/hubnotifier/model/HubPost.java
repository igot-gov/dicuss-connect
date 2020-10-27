package com.eagle.hubnotifier.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "objects")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HubPost {
    @Id
    private ObjectId id;

    @Field("_key")
    private String key;

    @Field("content")
    private String content;

    @Field("pid")
    private Long pid;

    @Field("tid")
    private Long tid;

    @Field("uid")
    private Long uid;

    @Field("downvotes")
    private Long downvotes;

    @Field("upvotes")
    private Long upvotes;
}
