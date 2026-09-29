package com.mybezo.macro;

public final class SlimeFlowAutoCommandEntry {
	public String name = "Command";
	public String command = "";
	public boolean enabled = false;
	public int intervalAmount = 5;
	public int intervalUnit = SlimeFlowState.AUTO_SELL_UNIT_SECONDS;
	public int keybind = SlimeFlowState.KEYBIND_NONE;
	public transient int cooldownTicks = 0;

	public SlimeFlowAutoCommandEntry() {
	}

	public SlimeFlowAutoCommandEntry(String name) {
		this.name = name;
	}

	public SlimeFlowAutoCommandEntry copy() {
		SlimeFlowAutoCommandEntry copy = new SlimeFlowAutoCommandEntry();
		copy.name = name == null ? "Command" : name;
		copy.command = command == null ? "" : command;
		copy.enabled = enabled;
		copy.intervalAmount = intervalAmount;
		copy.intervalUnit = intervalUnit;
		copy.keybind = keybind;
		return copy;
	}
}
