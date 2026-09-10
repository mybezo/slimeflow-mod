package com.mybezo.macro;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;

/** Copy a profile to the clipboard as a shareable text code, and read one back. */
public final class SlimeFlowScriptShare {
	private static final Gson GSON = new GsonBuilder().create();
	private static final String PREFIX = "SLIMEFLOW1:";

	private SlimeFlowScriptShare() {
	}

	public static boolean copyToClipboard(Minecraft client, SlimeFlowProfile profile) {
		if (client == null || client.keyboardHandler == null || profile == null) {
			return false;
		}

		try {
			String json = GSON.toJson(profile);
			String encoded = PREFIX + Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			client.keyboardHandler.setClipboard(encoded);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	public static SlimeFlowProfile pasteFromClipboard(Minecraft client) {
		if (client == null || client.keyboardHandler == null) {
			return null;
		}

		try {
			String clipboard = client.keyboardHandler.getClipboard();
			if (clipboard == null) {
				return null;
			}

			clipboard = clipboard.trim();
			if (!clipboard.startsWith(PREFIX)) {
				return null;
			}

			byte[] bytes = Base64.getDecoder().decode(clipboard.substring(PREFIX.length()));
			String json = new String(bytes, StandardCharsets.UTF_8);
			SlimeFlowProfile profile = GSON.fromJson(json, SlimeFlowProfile.class);

			if (profile == null || profile.rows == null) {
				return null;
			}

			if (profile.name == null || profile.name.trim().isEmpty()) {
				profile.name = "pasted";
			}

			return profile;
		} catch (Exception ignored) {
			return null;
		}
	}
}
