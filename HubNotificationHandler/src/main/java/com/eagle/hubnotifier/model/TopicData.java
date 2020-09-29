package com.eagle.hubnotifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicData {
    private Long cid;

    private Long postcount;

    private Long tid;

    private String title;

    private Long uid;

    private Long upvotes;

    private Long downvotes;

    private List<PostData> posts;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getPostcount() {
        return postcount;
    }

    public void setPostcount(Long postcount) {
        this.postcount = postcount;
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

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Long upvotes) {
        this.upvotes = upvotes;
    }

    public Long getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Long downvotes) {
        this.downvotes = downvotes;
    }

    public List<PostData> getPosts() {
        return posts;
    }

    public void setPosts(List<PostData> posts) {
        this.posts = posts;
    }
}
