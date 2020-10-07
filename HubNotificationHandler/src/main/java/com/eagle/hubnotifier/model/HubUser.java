package com.eagle.hubnotifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "objects")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HubUser {
    @Id
    private ObjectId id;

    @Field("_key")
    private String key;

    @Field("email")
    private String email;

    @Field("username")
    private String username;

    @Field("uid")
    private Long uid;
}
