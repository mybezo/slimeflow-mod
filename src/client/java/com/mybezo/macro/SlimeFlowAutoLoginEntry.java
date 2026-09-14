package com.mybezo.macro;

public final class SlimeFlowAutoLoginEntry {
	String name = "";
	String address = "";
	String password = "";
	String targetServer = "";
	boolean enabled = true;
	boolean autoServer = false;
	int loginDelayTicks = 60;
	int serverDelayTicks = 40;

	SlimeFlowAutoLoginEntry() {
	}

	SlimeFlowAutoLoginEntry(String name, String address) {
		this.name = clean(name);
		this.address = clean(address);
	}

	SlimeFlowAutoLoginEntry copy() {
		SlimeFlowAutoLoginEntry entry = new SlimeFlowAutoLoginEntry();
		entry.name = clean(name);
		entry.address = clean(address);
		entry.password = password == null ? "" : password;
		entry.targetServer = clean(targetServer);
		entry.enabled = enabled;
		entry.autoServer = autoServer;
		entry.loginDelayTicks = Math.max(20, loginDelayTicks <= 0 ? 60 : loginDelayTicks);
		entry.serverDelayTicks = Math.max(20, serverDelayTicks <= 0 ? 40 : serverDelayTicks);
		return entry;
	}

	String displayName() {
		String value = clean(name);
		if (!value.isEmpty()) {
			return value;
		}

		value = clean(address);
		return value.isEmpty() ? "Server" : value;
	}

	static String clean(String value) {
		return value == null ? "" : value.trim();
	}
}
