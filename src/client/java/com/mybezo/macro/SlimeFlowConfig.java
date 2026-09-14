package com.mybezo.macro;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class SlimeFlowConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SlimeFlowConfig() {
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("slimeflow.json");
	}

	public static void load() {
		try {
			Path path = path();

			if (!Files.exists(path)) {
				return;
			}

			try (Reader reader = Files.newBufferedReader(path)) {
				Data data = GSON.fromJson(reader, Data.class);

				SlimeFlowState.profiles.clear();
				SlimeFlowState.autoLoginEntries.clear();
				SlimeFlowState.autoCommandEntries.clear();
				SlimeFlowState.stackTargets.clear();

				if (data == null) {
					return;
				}

				SlimeFlowState.autoLoginEnabled = data.autoLoginEnabled;
				SlimeFlowState.autoLoginPassword = data.autoLoginPassword == null ? "" : data.autoLoginPassword;
				SlimeFlowState.autoLoginDelayTicks = Math.max(20, data.autoLoginDelayTicks <= 0 ? 60 : data.autoLoginDelayTicks);
				SlimeFlowState.autoLoginPerServer = data.autoLoginPerServer;
				SlimeFlowState.autoLoginServerAddress = data.autoLoginServerAddress == null ? "" : data.autoLoginServerAddress;
				SlimeFlowState.autoLoginServerPassword = data.autoLoginServerPassword == null ? "" : data.autoLoginServerPassword;
				SlimeFlowState.keyOpenUi = normalizeKey(data.keyOpenUi, SlimeFlowState.DEFAULT_KEY_OPEN_UI);
				SlimeFlowState.keyStopAll = normalizeKey(data.keyStopAll, SlimeFlowState.DEFAULT_KEY_STOP_ALL);
				SlimeFlowState.keyStartStack = normalizeKey(data.keyStartStack, SlimeFlowState.DEFAULT_KEY_START_STACK);
				SlimeFlowState.autoSellEnabled = data.autoSellEnabled;
				SlimeFlowState.autoSellItemName = data.autoSellItemName == null ? "" : data.autoSellItemName;
				SlimeFlowState.autoSellCommand = SlimeFlowAutoSell.normalizeCommand(data.autoSellCommand == null ? "sell all" : data.autoSellCommand);
				SlimeFlowState.autoSellIntervalAmount = normalizeAutoSellAmount(data.autoSellIntervalAmount, data.autoSellIntervalUnit);
				SlimeFlowState.autoSellIntervalUnit = normalizeAutoSellUnit(data.autoSellIntervalUnit);
				SlimeFlowState.autoSellKeybind = normalizeOptionalKey(data.autoSellKeybind);

				if (data.profiles != null) {
					for (SlimeFlowProfile profile : data.profiles) {
						if (profile != null) {
							SlimeFlowState.profiles.add(normalizeProfile(profile));
						}
					}
				}

				if (data.stackTargets != null) {
					for (SlimeFlowStackTarget target : data.stackTargets) {
						if (target != null) {
							SlimeFlowState.stackTargets.add(target.copy());
						}
					}
				}

				if (data.autoLoginEntries != null) {
					for (SlimeFlowAutoLoginEntry entry : data.autoLoginEntries) {
						if (entry != null) {
							SlimeFlowState.autoLoginEntries.add(entry.copy());
						}
					}
				}

				if (data.autoCommandEntries != null) {
					for (SlimeFlowAutoCommandEntry entry : data.autoCommandEntries) {
						if (entry != null) {
							SlimeFlowState.autoCommandEntries.add(normalizeCommandEntry(entry.copy()));
						}
					}
				}

				migrateOldAutoLoginIfNeeded();
			}
		} catch (Exception ignored) {
		}
	}

	public static void save() {
		try {
			Path path = path();
			Files.createDirectories(path.getParent());

			Data data = new Data();
			for (SlimeFlowProfile profile : SlimeFlowState.profiles) {
				normalizeProfile(profile);
			}
			data.profiles = SlimeFlowState.profiles;
			data.stackTargets = new ArrayList<>();
			for (SlimeFlowStackTarget target : SlimeFlowState.stackTargets) {
				if (target != null) {
					data.stackTargets.add(target.copy());
				}
			}
			data.autoLoginEnabled = SlimeFlowState.autoLoginEnabled;
			data.autoLoginPassword = SlimeFlowState.autoLoginPassword == null ? "" : SlimeFlowState.autoLoginPassword;
			data.autoLoginDelayTicks = Math.max(20, SlimeFlowState.autoLoginDelayTicks);
			data.autoLoginPerServer = SlimeFlowState.autoLoginPerServer;
			data.autoLoginServerAddress = SlimeFlowState.autoLoginServerAddress == null ? "" : SlimeFlowState.autoLoginServerAddress;
			data.autoLoginServerPassword = SlimeFlowState.autoLoginServerPassword == null ? "" : SlimeFlowState.autoLoginServerPassword;
			data.keyOpenUi = normalizeKey(SlimeFlowState.keyOpenUi, SlimeFlowState.DEFAULT_KEY_OPEN_UI);
			data.keyStopAll = normalizeKey(SlimeFlowState.keyStopAll, SlimeFlowState.DEFAULT_KEY_STOP_ALL);
			data.keyStartStack = normalizeKey(SlimeFlowState.keyStartStack, SlimeFlowState.DEFAULT_KEY_START_STACK);
			data.autoSellEnabled = SlimeFlowState.autoSellEnabled;
			data.autoSellItemName = SlimeFlowState.autoSellItemName == null ? "" : SlimeFlowState.autoSellItemName.trim();
			data.autoSellCommand = SlimeFlowAutoSell.normalizeCommand(SlimeFlowState.autoSellCommand);
			data.autoSellIntervalAmount = normalizeAutoSellAmount(SlimeFlowState.autoSellIntervalAmount, SlimeFlowState.autoSellIntervalUnit);
			data.autoSellIntervalUnit = normalizeAutoSellUnit(SlimeFlowState.autoSellIntervalUnit);
			data.autoSellKeybind = normalizeOptionalKey(SlimeFlowState.autoSellKeybind);
			data.autoLoginEntries = new ArrayList<>();
			for (SlimeFlowAutoLoginEntry entry : SlimeFlowState.autoLoginEntries) {
				if (entry != null) {
					data.autoLoginEntries.add(entry.copy());
				}
			}

			data.autoCommandEntries = new ArrayList<>();
			for (SlimeFlowAutoCommandEntry entry : SlimeFlowState.autoCommandEntries) {
				if (entry != null) {
					data.autoCommandEntries.add(normalizeCommandEntry(entry.copy()));
				}
			}

			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(data, writer);
			}
		} catch (Exception ignored) {
		}
	}

	private static void migrateOldAutoLoginIfNeeded() {
		if (!SlimeFlowState.autoLoginEntries.isEmpty()) {
			return;
		}

		String password = SlimeFlowState.autoLoginPerServer ? SlimeFlowState.autoLoginServerPassword : SlimeFlowState.autoLoginPassword;
		password = password == null ? "" : password.trim();
		if (password.isEmpty()) {
			return;
		}

		String address = SlimeFlowState.autoLoginPerServer ? SlimeFlowState.autoLoginServerAddress : "";
		SlimeFlowAutoLoginEntry entry = new SlimeFlowAutoLoginEntry(defaultName(address), address);
		entry.password = password;
		entry.enabled = SlimeFlowState.autoLoginEnabled;
		entry.autoServer = false;
		entry.loginDelayTicks = Math.max(20, SlimeFlowState.autoLoginDelayTicks <= 0 ? 60 : SlimeFlowState.autoLoginDelayTicks);
		entry.serverDelayTicks = 40;
		SlimeFlowState.autoLoginEntries.add(entry);
	}

	private static String defaultName(String address) {
		String value = address == null ? "" : address.trim();
		if (value.isEmpty()) {
			return "Server 1";
		}

		int colon = value.indexOf(':');
		if (colon > 0) {
			value = value.substring(0, colon);
		}

		return value.isEmpty() ? "Server 1" : value;
	}

	private static int normalizeKey(int key, int fallback) {
		return key > 0 ? key : fallback;
	}

	private static int normalizeOptionalKey(int key) {
		return key > 0 ? key : SlimeFlowState.KEYBIND_NONE;
	}

	private static int normalizeAutoSellUnit(int unit) {
		if (unit < SlimeFlowState.AUTO_SELL_UNIT_MS || unit > SlimeFlowState.AUTO_SELL_UNIT_HOURS) {
			return SlimeFlowState.AUTO_SELL_UNIT_SECONDS;
		}
		return unit;
	}

	private static int normalizeAutoSellAmount(int amount, int unit) {
		unit = normalizeAutoSellUnit(unit);
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MS) {
			return Math.max(50, amount <= 0 ? 500 : amount);
		}
		return Math.max(1, amount <= 0 ? 5 : amount);
	}

	private static SlimeFlowProfile normalizeProfile(SlimeFlowProfile profile) {
		if (profile == null) {
			return new SlimeFlowProfile();
		}

		profile.name = profile.name == null || profile.name.trim().isEmpty() ? "macro" : profile.name.trim();
		profile.guiTitle = profile.guiTitle == null ? "" : profile.guiTitle;
		profile.speed = Math.max(0, Math.min(20, profile.speed));
		profile.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick);
		return profile;
	}

	private static SlimeFlowAutoCommandEntry normalizeCommandEntry(SlimeFlowAutoCommandEntry entry) {
		if (entry == null) return new SlimeFlowAutoCommandEntry();
		entry.name = entry.name == null || entry.name.trim().isEmpty() ? "Command" : entry.name.trim();
		entry.command = SlimeFlowAutoCommand.normalizeCommand(entry.command);
		entry.intervalUnit = normalizeAutoSellUnit(entry.intervalUnit);
		entry.intervalAmount = normalizeAutoSellAmount(entry.intervalAmount, entry.intervalUnit);
		entry.keybind = normalizeOptionalKey(entry.keybind);
		entry.cooldownTicks = 0;
		return entry;
	}

	private static final class Data {
		List<SlimeFlowProfile> profiles = new ArrayList<>();
		List<SlimeFlowStackTarget> stackTargets = new ArrayList<>();
		boolean autoLoginEnabled = false;
		String autoLoginPassword = "";
		int autoLoginDelayTicks = 60;
		boolean autoLoginPerServer = false;
		String autoLoginServerAddress = "";
		String autoLoginServerPassword = "";
		List<SlimeFlowAutoLoginEntry> autoLoginEntries = new ArrayList<>();
		int keyOpenUi = SlimeFlowState.DEFAULT_KEY_OPEN_UI;
		int keyStopAll = SlimeFlowState.DEFAULT_KEY_STOP_ALL;
		int keyStartStack = SlimeFlowState.DEFAULT_KEY_START_STACK;
		boolean autoSellEnabled = false;
		String autoSellItemName = "";
		String autoSellCommand = "sell all";
		int autoSellIntervalAmount = 5;
		int autoSellIntervalUnit = SlimeFlowState.AUTO_SELL_UNIT_SECONDS;
		int autoSellKeybind = SlimeFlowState.KEYBIND_NONE;
		List<SlimeFlowAutoCommandEntry> autoCommandEntries = new ArrayList<>();
	}
}
