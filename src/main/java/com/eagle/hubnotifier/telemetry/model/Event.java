package com.eagle.hubnotifier.telemetry.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
	private String eid;
	private long ets;
	private String ver;
	private String mid;
	private List<String> tags;
	private EData eData;
	private Actor actor;
	private Context context;

	public String toString() {
		StringBuilder str = new StringBuilder("eid:");
		str.append(eid).append(",ets:").append(ets);
		str.append(",ver:").append(ver).append(",mid:").append(mid);
		str.append(",eData:{").append(eData);
		str.append("},actor:{").append(actor);
		if (context != null) {
			str.append("},context:{").append(context);
		}
		if (tags != null && !tags.isEmpty()) {
			str.append("},tags:[");
			str.append(tags.toString());
		}
		str.append("]");
		return str.toString();
	}
}
