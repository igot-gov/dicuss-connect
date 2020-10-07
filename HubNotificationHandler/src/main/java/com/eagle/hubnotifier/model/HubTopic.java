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
public class HubTopic {
    @Id
    private ObjectId id;

    @Field("_key")
    private String key;

    @Field("cid")
    private Long cid;

    @Field("mainPid")
    private Long mainPid;

    @Field("postcount")
    private Long postcount;

    @Field("slug")
    private String slug;

    @Field("tid")
    private Long tid;

    @Field("title")
    private String title;


    @Field("viewcount")
    private Long viewcount;


    @Field("uid")
    private Long uid;
}
