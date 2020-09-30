package com.eagle.hubnotifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "objects")
@JsonIgnoreProperties(ignoreUnknown = true)
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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getMainPid() {
        return mainPid;
    }

    public void setMainPid(Long mainPid) {
        this.mainPid = mainPid;
    }

    public Long getPostcount() {
        return postcount;
    }

    public void setPostcount(Long postcount) {
        this.postcount = postcount;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getViewcount() {
        return viewcount;
    }

    public void setViewcount(Long viewcount) {
        this.viewcount = viewcount;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
