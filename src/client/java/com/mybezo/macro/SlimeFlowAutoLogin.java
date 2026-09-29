package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class SlimeFlowAutoLogin {
	private SlimeFlowAutoLogin() {
	}

	public static void tick(Minecraft client) {
		if (client == null) {
			resetRuntime(true);
			return;
		}

		String physicalServer = normalizeAddress(currentServerAddress(client));
		if (!physicalServer.isEmpty() && !physicalServer.equalsIgnoreCase(SlimeFlowState.autoLoginLastPhysicalServer)) {
			SlimeFlowState.autoLoginLastPhysicalServer = physicalServer;
			SlimeFlowState.autoLoginJoinedTargets.clear();
		}

		if (client.player == null || client.gameMode == null) {
			SlimeFlowState.autoLoginMissingPlayerTicks++;
			resetRuntime(SlimeFlowState.autoLoginMissingPlayerTicks > 200 || physicalServer.isEmpty());
			return;
		}
		SlimeFlowState.autoLoginMissingPlayerTicks = 0;

		SlimeFlowAutoLoginEntry entry = selectedEntry(client);
		if (entry == null) {
			return;
		}

		Object connection = client.player.connection;
		if (connection == null) {
			resetRuntime(false);
			return;
		}

		String password = entry.password == null ? "" : entry.password.trim();
		String target = normalizeTargetServer(entry.targetServer);
		if (!target.isEmpty() && !entry.autoServer) {
			entry.autoServer = true;
			SlimeFlowConfig.save();
		}

		boolean hasLogin = !password.isEmpty();
		boolean hasJoin = !target.isEmpty() && entry.autoServer;
		String joinKey = joinSessionKey(client, entry, target);
		boolean alreadyJoinedThisSession = hasJoin && SlimeFlowState.autoLoginJoinedTargets.contains(joinKey);

		if (SlimeFlowState.autoLoginConnectionRef != connection) {
			SlimeFlowState.autoLoginConnectionRef = connection;
			SlimeFlowState.autoLoginSent = alreadyJoinedThisSession;
			SlimeFlowState.autoLoginServerSent = alreadyJoinedThisSession;
			SlimeFlowState.autoLoginServerAttempts = 0;
			SlimeFlowState.autoLoginCountdown = hasLogin && !alreadyJoinedThisSession ? Math.max(20, entry.loginDelayTicks <= 0 ? 60 : entry.loginDelayTicks) : 0;
			SlimeFlowState.autoLoginServerCountdown = hasJoin && !hasLogin && !alreadyJoinedThisSession ? Math.max(20, entry.serverDelayTicks <= 0 ? 40 : entry.serverDelayTicks) : -1;
		}

		if (alreadyJoinedThisSession) {
			SlimeFlowState.autoLoginSent = true;
			SlimeFlowState.autoLoginServerSent = true;
			return;
		}

		if (!SlimeFlowState.autoLoginSent) {
			if (hasLogin) {
				if (SlimeFlowState.autoLoginCountdown > 0) {
					SlimeFlowState.autoLoginCountdown--;
					return;
				}

				sendCommand(client, "login " + password);
			}

			SlimeFlowState.autoLoginSent = true;
			if (hasJoin) {
				SlimeFlowState.autoLoginServerCountdown = Math.max(20, entry.serverDelayTicks <= 0 ? 40 : entry.serverDelayTicks);
			}
			if (hasLogin) {
				return;
			}
		}

		if (SlimeFlowState.autoLoginServerSent || !hasJoin) {
			return;
		}

		if (SlimeFlowState.autoLoginServerCountdown > 0) {
			SlimeFlowState.autoLoginServerCountdown--;
			return;
		}

		sendCommand(client, "server " + target);
		SlimeFlowState.autoLoginServerAttempts = 1;
		SlimeFlowState.autoLoginServerSent = true;
		SlimeFlowState.autoLoginJoinedTargets.add(joinKey);
	}

	static String currentServerAddress(Minecraft client) {
		try {
			if (client == null) {
				return "";
			}

			ServerData server = client.getCurrentServer();
			if (server == null || server.ip == null) {
				return "";
			}

			return server.ip.trim();
		} catch (Throwable ignored) {
			return "";
		}
	}

	static SlimeFlowAutoLoginEntry selectedEntry(Minecraft client) {
		String currentServer = currentServerAddress(client);
		SlimeFlowAutoLoginEntry blankFallback = null;

		for (SlimeFlowAutoLoginEntry entry : SlimeFlowState.autoLoginEntries) {
			if (entry == null || !entry.enabled) {
				continue;
			}

			String password = entry.password == null ? "" : entry.password.trim();
			String target = normalizeTargetServer(entry.targetServer);
			if (!target.isEmpty() && !entry.autoServer) {
				entry.autoServer = true;
			}
			if (password.isEmpty() && target.isEmpty()) {
				continue;
			}

			String savedServer = entry.address == null ? "" : entry.address.trim();
			if (savedServer.isEmpty()) {
				if (blankFallback == null) {
					blankFallback = entry;
				}
				continue;
			}

			if (sameServer(savedServer, currentServer)) {
				return entry;
			}
		}

		return blankFallback;
	}

	static boolean sameServer(String a, String b) {
		a = normalizeAddress(a);
		b = normalizeAddress(b);
		if (a.isEmpty() || b.isEmpty()) {
			return false;
		}

		return a.equalsIgnoreCase(b);
	}

	private static String normalizeAddress(String value) {
		value = value == null ? "" : value.trim();
		if (value.endsWith(".")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	static String normalizeTargetServer(String value) {
		value = value == null ? "" : value.trim();
		if (value.startsWith("/")) {
			value = value.substring(1).trim();
		}
		String lower = value.toLowerCase();
		if (lower.startsWith("server ")) {
			value = value.substring(7).trim();
		}
		return value;
	}

	private static String joinSessionKey(Minecraft client, SlimeFlowAutoLoginEntry entry, String target) {
		String currentServer = normalizeAddress(currentServerAddress(client));
		String savedServer = entry == null ? "" : normalizeAddress(entry.address);
		String name = entry == null || entry.name == null ? "" : entry.name.trim();
		String base = savedServer.isEmpty() ? currentServer : savedServer;
		if (base.isEmpty()) {
			base = name;
		}
		return base.toLowerCase() + "|" + normalizeTargetServer(target).toLowerCase();
	}

	private static void sendCommand(Minecraft client, String command) {
		try {
			client.player.connection.sendCommand(command);
		} catch (Throwable ignored) {
		}
	}

	public static void resetRuntime() {
		resetRuntime(true);
	}

	private static void resetRuntime(boolean clearSessionLock) {
		SlimeFlowState.autoLoginConnectionRef = null;
		SlimeFlowState.autoLoginSent = false;
		SlimeFlowState.autoLoginCountdown = -1;
		SlimeFlowState.autoLoginServerSent = false;
		SlimeFlowState.autoLoginServerCountdown = -1;
		SlimeFlowState.autoLoginServerAttempts = 0;
		if (clearSessionLock) {
			SlimeFlowState.autoLoginJoinedTargets.clear();
			SlimeFlowState.autoLoginLastPhysicalServer = "";
		}
	}
}
