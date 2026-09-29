package com.mybezo.macro;

import net.minecraft.client.Minecraft;

public final class SlimeFlowAutoCommand {
	private SlimeFlowAutoCommand() {
	}

	public static void tick(Minecraft client) {
		if (client == null || client.player == null || client.player.connection == null) {
			return;
		}

		for (SlimeFlowAutoCommandEntry entry : SlimeFlowState.autoCommandEntries) {
			if (entry == null || !entry.enabled) {
				continue;
			}

			String command = normalizeCommand(entry.command);
			if (command.isEmpty()) {
				continue;
			}

			if (entry.cooldownTicks > 0) {
				entry.cooldownTicks--;
				continue;
			}

			sendCommand(client, command);
			entry.cooldownTicks = intervalTicks(entry);
		}
	}

	static void runEntry(Minecraft client, SlimeFlowAutoCommandEntry entry) {
		if (client == null || client.player == null || entry == null) return;
		String command = normalizeCommand(entry.command);
		if (command.isEmpty()) return;
		sendCommand(client, command);
	}

	static void sendCommand(Minecraft client, String command) {
		try {
			client.player.connection.sendCommand(normalizeCommand(command));
		} catch (Throwable ignored) {
		}
	}

	static int intervalTicks(SlimeFlowAutoCommandEntry entry) {
		int amount = Math.max(1, entry == null ? 5 : entry.intervalAmount);
		int unit = entry == null ? SlimeFlowState.AUTO_SELL_UNIT_SECONDS : entry.intervalUnit;

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

	static String unitLabel(int unit) {
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MS) return "ms";
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MINUTES) return "menit";
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_HOURS) return "jam";
		return "detik";
	}

	static void cycleUnit(SlimeFlowAutoCommandEntry entry) {
		if (entry == null) return;
		entry.intervalUnit = (entry.intervalUnit + 1) % 4;
		if (entry.intervalUnit == SlimeFlowState.AUTO_SELL_UNIT_MS && entry.intervalAmount < 50) {
			entry.intervalAmount = 500;
		}
	}

	static void changeAmount(SlimeFlowAutoCommandEntry entry, int direction) {
		if (entry == null) return;
		int unit = entry.intervalUnit;
		int step = unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 50 : 1;
		int min = unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 50 : 1;
		int max = unit == SlimeFlowState.AUTO_SELL_UNIT_HOURS ? 24 : (unit == SlimeFlowState.AUTO_SELL_UNIT_MINUTES ? 120 : (unit == SlimeFlowState.AUTO_SELL_UNIT_MS ? 60000 : 3600));
		entry.intervalAmount = Math.max(min, Math.min(max, entry.intervalAmount + direction * step));
		entry.cooldownTicks = 0;
	}

	static String normalizeCommand(String command) {
		command = command == null ? "" : command.trim();
		while (command.startsWith("/")) {
			command = command.substring(1).trim();
		}
		return command;
	}

	static String displayName(SlimeFlowAutoCommandEntry entry, int index) {
		String name = entry == null || entry.name == null ? "" : entry.name.trim();
		if (name.isEmpty()) name = "Command " + (index + 1);
		return name;
	}
}
