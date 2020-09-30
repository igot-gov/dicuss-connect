package com.eagle.hubnotifier.util;

public class Constants {
    private Constants() { }
    public static final String INCOMING_KAFKA_LISTENER = "incomingKafkaListenerContainerFactory";

    public static final String ROOT_ORG_CONSTANT = "rootOrg";

    public static final String FILTER_TOPIC_CREATE = "filter:topic.create";

    public static final String FILTER_POST_CREATE = "filter:post.create";

    public static final String FILTER_TOPIC_REPLY = "filter:topic.reply";

    public static final String ACTION_POST_UPVOTE = "action:post.upvote";

    public static final String ACTION_POST_DOWNVOTE = "action:post.downvote";

    public static final String DISCUSSION_CREATION_EVENT_ID = "discussion_creation";

    public static final String DISCUSSION_CREATION_TOPIC_TAG = "#discussionTopic";

    public static final String DISCUSSION_CREATION_TARGET_URL = "#targetUrl";

    public static final String DISCUSSION_REPLY_EVENT_ID = "discussion_comment_creation";

    public static final String COMMENTED_BY_NAME_TAG = "#commentedByName";

    public static final String COMMENT_TAG = "#comment";

    public static final String COMMENTED_BY_TAG = "commentedBy";

    public static final String AUTHOR_ROLE = "author";

    public static final String USER_ROLE = "user";

    public static final String POST_ROLE = "post";

    public static final String PARAM_TOPIC_TITLE_CONSTANT = "params[topic][title]";

    public static final String PARAM_TOPIC_TID_CONSTANT = "params[topic][tid]";

    public static final String PARAM_TOPIC_UID_CONSTANT = "params[topic][uid]";

    public static final String PARAM_CONTENT_CONSTANT = "params[content]";

    public static final String PARAM_UID= "params[uid]";

    public static final String PARAM_TID= "params[tid]";

    public static final String PARAM_OWNER = "params[owner]";

    public static final String DISCUSSION_UPVOTE_EVENT = "discussion_upvote_event";

    public static final String PARAMS_PID = "params[pid]";

    public static final String UPVOTED_BY_NAME = "#upvotedByName";

    public static final String UPVOTED_BY = "upvotedBy";

    public static final String DISCUSSION_DOWNVOTE_EVENT = "discussion_downvote_event";

    public static final String DOWNVOTE_BY_NAME = "#downvotedByName";

    public static final String DOWNVOTE_BY = "downvotedBy";

    public static final String TOPIC_VALUE_CONSTANTS = "topic";




}
