package com.eagle.hubnotifier.telemetry.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelemetryData {
	private String id;
	private String ver;
	private long ets;
	private List<Event> events;

	public String toString() {
		StringBuilder str = new StringBuilder("TelemetryData: {id:");
		str.append(id).append(",ver:").append(ver).append(",ets:").append(ets).append(",events:[");
		for (Event e : events) {
			str.append("{").append(e).append("},");
		}
		str.setLength(str.length() - 1);
		str.append("]}");
		return str.toString();
	}

	public Event getEvent() {
		return events.get(0);
	}
}
