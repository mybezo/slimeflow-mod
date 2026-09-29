package com.mybezo.macro;

import com.mybezo.macro.mixin.AbstractContainerScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlimeFlowAutoSell {
	private SlimeFlowAutoSell() {
	}

	public static void tick(Minecraft client) {
		if (client == null || client.player == null || client.gameMode == null) {
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowState.autoSellCommandSentWhileFull = false;
			return;
		}

		if (!SlimeFlowState.autoSellEnabled) {
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowState.autoSellCommandSentWhileFull = false;
			return;
		}

		String itemName = clean(SlimeFlowState.autoSellItemName);
		String command = normalizeCommand(SlimeFlowState.autoSellCommand);
		if (itemName.isEmpty() || command.isEmpty()) {
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowState.autoSellCommandSentWhileFull = false;
			return;
		}

		boolean full = isPickedItemFullInInventory(client, itemName);
		if (!full) {
			SlimeFlowState.autoSellCommandSentWhileFull = false;
			return;
		}

		if (SlimeFlowState.autoSellCooldownTicks > 0) {
			SlimeFlowState.autoSellCooldownTicks--;
			return;
		}

		// Send once when full, then repeat only after interval if inventory is still full.
		sendCommand(client, command);
		SlimeFlowState.autoSellCommandSentWhileFull = true;
		SlimeFlowState.autoSellCooldownTicks = intervalTicks();
	}

	static boolean isPickedItemFullInInventory(Minecraft client, String itemName) {
		if (client == null || client.player == null || client.player.containerMenu == null) {
			return false;
		}

		AbstractContainerMenu menu = client.player.containerMenu;
		if (menu.slots.isEmpty()) {
			return false;
		}

		String wanted = normalize(itemName);
		int start = Math.max(0, menu.slots.size() - 36);
		boolean hasTarget = false;
		boolean hasEmpty = false;
		boolean hasPartialTarget = false;

		for (int i = start; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();
			if (stack == null || stack.isEmpty()) {
				hasEmpty = true;
				continue;
			}

			String current = normalize(SlimeFlowItemFinder.getItemName(stack));
			if (!matches(current, wanted)) {
				continue;
			}

			hasTarget = true;
			if (stack.getCount() < stack.getMaxStackSize()) {
				hasPartialTarget = true;
			}
		}

		return hasTarget && !hasEmpty && !hasPartialTarget;
	}

	static boolean pickHoveredItem(Minecraft client) {
		if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
			return false;
		}

		Slot slot;
		try {
			slot = ((AbstractContainerScreenAccessor) screen).slimeflow$getHoveredSlot();
		} catch (Throwable ignored) {
			return false;
		}

		String item = SlimeFlowItemFinder.getSlotItemName(slot);
		if (item == null || item.trim().isEmpty()) {
			return true;
		}

		SlimeFlowState.autoSellItemName = item.trim();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
		SlimeFlowState.autoSellCooldownTicks = 0;
		SlimeFlowState.autoSellCommandSentWhileFull = false;
		SlimeFlowConfig.save();
		return true;
	}

	static int intervalTicks() {
		int amount = Math.max(1, SlimeFlowState.autoSellIntervalAmount);
		int unit = SlimeFlowState.autoSellIntervalUnit;

		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MS) {
			return Math.max(1, (int) Math.ceil(amount / 50.0));
		}
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MINUTES) {
			return Math.min(1728000, amount * 60 * 20);
		}
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_HOURS) {
			return Math.min(1728000, amount * 60 * 60 * 20);
		}
		return Math.min(1728000, amount * 20);
	}

	static String unitLabel() {
		int unit = SlimeFlowState.autoSellIntervalUnit;
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MS) return "ms";
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MINUTES) return "menit";
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_HOURS) return "jam";
		return "detik";
	}

	static void cycleUnit() {
		SlimeFlowState.autoSellIntervalUnit = (SlimeFlowState.autoSellIntervalUnit + 1) % 4;
		if (SlimeFlowState.autoSellIntervalUnit == SlimeFlowState.AUTO_SELL_UNIT_MS && SlimeFlowState.autoSellIntervalAmount < 50) {
			SlimeFlowState.autoSellIntervalAmount = 500;
		}
	}

	static void changeAmount(int direction) {
		int unit = SlimeFlowState.autoSellIntervalUnit;
		int step = unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 50 : 1;
		int min = unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 50 : 1;
		int max = unit == SlimeFlowState.AUTO_SELL_UNIT_HOURS ? 24 : (unit == SlimeFlowState.AUTO_SELL_UNIT_MINUTES ? 120 : (unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 60000 : 3600));
		SlimeFlowState.autoSellIntervalAmount = Math.max(min, Math.min(max, SlimeFlowState.autoSellIntervalAmount + direction * step));
	}

	static String normalizeCommand(String command) {
		command = clean(command);
		while (command.startsWith("/")) {
			command = command.substring(1).trim();
		}
		return command;
	}

	static void runCommandNow(Minecraft client) {
		String command = normalizeCommand(SlimeFlowState.autoSellCommand);
		if (client == null || client.player == null || command.isEmpty()) {
			return;
		}
		sendCommand(client, command);
	}

	static void sendCommand(Minecraft client, String command) {
		try {
			client.player.connection.sendCommand(command);
		} catch (Throwable ignored) {
		}
	}

	private static boolean matches(String current, String wanted) {
		return current.equals(wanted) || current.contains(wanted) || wanted.contains(current);
	}

	private static String normalize(String value) {
		return clean(value).toLowerCase();
	}

	static String clean(String value) {
		return value == null ? "" : value.trim();
	}
}
