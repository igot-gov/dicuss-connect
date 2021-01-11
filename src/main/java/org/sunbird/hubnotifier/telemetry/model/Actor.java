package org.sunbird.hubnotifier.telemetry.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Actor {
	private String id;
	private String type;

	public String toString() {
		return "id:" + id + ",type:" + type;
	}
}
