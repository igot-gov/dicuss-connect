package com.eagle.hubnotifier.telemetry.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {
	private String channel;
	private String env;
	private String sid;
	private String did;
	private PData pData;

	public String toString() {
		StringBuilder str = new StringBuilder("channel:").append(channel);
		str.append(",env:").append(env).append(",sid:").append(sid).append(",did:").append(did);
		str.append(",pData:{").append(pData).append("}");

		return str.toString();
	}
}
