package com.eagle.hubnotifier.telemetry.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EData {
    private String id;
    private String type;
    private String subType;
    private String pageid;
    private String target;
    private String topicName;
    private String categoryName;

    public String toString() {
        StringBuilder str = new StringBuilder("id:");
        str.append(id);
        str.append(",type:").append(type).append(",subType:").append(subType)
                .append(",pageid:").append(pageid);
        str.append(",target:").append(target).append(",topicName:").append(topicName);
        str.append(",categoryName:").append(categoryName);

        return str.toString();
    }
}
