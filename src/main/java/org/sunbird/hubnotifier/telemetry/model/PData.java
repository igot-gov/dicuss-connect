package org.sunbird.hubnotifier.telemetry.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PData {
	private String id;
	private String ver;
	private String pid;

	public String toString() {
		return "id:" + id + ",ver:" + ver + ",pid:" + pid;
	}
}
