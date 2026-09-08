package com.mybezo.macro;

import java.util.ArrayList;
import java.util.List;

public class SlimeFlowProfile {
	public static final int MIN_ACTIONS_PER_TICK = 1;
	public static final int MAX_ACTIONS_PER_TICK = 50;

	public String name = "macro";
	public boolean autoRun = false;
	public String guiTitle = "";
	public int speed = 0;
	public int actionsPerTick = MIN_ACTIONS_PER_TICK;
	public boolean outputCollectLoop = true;

	public boolean backpackRefillEnabled = false;
	public List<Integer> backpackSlots = new ArrayList<>();
	public String backpackRefillItemName = "";
	public List<String> backpackRefillKeywords = new ArrayList<>();

	public List<Row> rows = new ArrayList<>();

	public SlimeFlowProfile() {
	}

	public SlimeFlowProfile(String name, String guiTitle) {
		this.name = name;
		this.guiTitle = guiTitle;
		this.autoRun = false;
		this.speed = 0;
		this.actionsPerTick = MIN_ACTIONS_PER_TICK;
	}

	public static int normalizeActionsPerTick(int value) {
		return Math.max(MIN_ACTIONS_PER_TICK, Math.min(MAX_ACTIONS_PER_TICK, value));
	}

	public enum RowType {
		MOVE,
		ITEM,
		CLICK,
		MULTI,
		OUTPUT,
		DROP
	}

	public enum DropScope {
		INVENTORY,
		GUI,
		BOTH
	}

	public static class Row {
		public RowType type = RowType.MOVE;

		public int fromSlot = -1;
		public int toSlot = -1;
		public int amount = 1;

		public int clickSlot = -1;
		public int clickButton = 0;
		public int clickTimes = 1;

		public String itemName = "";
		public int itemTargetSlot = -1;
		public int itemAmount = 1;

		public List<String> multiItemNames = new ArrayList<>();
		public int multiTargetSlot = -1;
		public int multiAmount = 1;

		public int outputSlot = -1;

		public String dropItemName = "";
		public DropScope dropScope = DropScope.BOTH;
		/** 0 means drop the whole stack every time it's found. */
		public int dropAmount = 0;

		public int delay = 0;

		public Row() {
		}

		public static Row drop(String itemName, DropScope scope, int amount) {
			Row row = new Row();

			row.type = RowType.DROP;
			row.dropItemName = itemName == null ? "" : itemName;
			row.dropScope = scope == null ? DropScope.BOTH : scope;
			row.dropAmount = Math.max(0, amount);
			row.delay = 0;

			return row;
		}

		public static Row move(int fromSlot, int toSlot, int amount) {
			Row row = new Row();

			row.type = RowType.MOVE;
			row.fromSlot = fromSlot;
			row.toSlot = toSlot;
			row.amount = Math.max(1, amount);
			row.delay = 0;

			return row;
		}

		public static Row click(int clickSlot, int clickButton, int clickTimes) {
			Row row = new Row();

			row.type = RowType.CLICK;
			row.clickSlot = clickSlot;
			row.clickButton = clickButton;
			row.clickTimes = Math.max(1, clickTimes);
			row.delay = 0;

			return row;
		}

		public static Row item(String itemName, int itemTargetSlot, int itemAmount) {
			Row row = new Row();

			row.type = RowType.ITEM;
			row.itemName = itemName == null ? "" : itemName;
			row.itemTargetSlot = itemTargetSlot;
			row.itemAmount = Math.max(1, itemAmount);
			row.delay = 0;

			return row;
		}

		public static Row multi(List<String> itemNames, int targetSlot, int amount) {
			Row row = new Row();

			row.type = RowType.MULTI;
			if (itemNames != null) {
				for (String name : itemNames) {
					if (name != null && !name.isEmpty() && !row.multiItemNames.contains(name)) {
						row.multiItemNames.add(name);
					}
				}
			}
			row.multiTargetSlot = targetSlot;
			row.multiAmount = Math.max(1, amount);
			row.delay = 0;

			return row;
		}

		public static Row output(int outputSlot) {
			Row row = new Row();

			row.type = RowType.OUTPUT;
			row.outputSlot = outputSlot;
			row.delay = 0;

			return row;
		}
	}
}
