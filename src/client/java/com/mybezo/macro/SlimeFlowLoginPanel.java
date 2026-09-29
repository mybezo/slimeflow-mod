package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowLoginPanel {
	private static final int W = 252;
	private static final int LIST_H = 170;
	private static final int EDIT_H = 224;
	private static final int ROW_H = 22;
	private static final int VISIBLE_ROWS = 5;

	private static final int FIELD_NONE = 0;
	private static final int FIELD_NAME = 1;
	private static final int FIELD_ADDRESS = 2;
	private static final int FIELD_PASSWORD = 3;
	private static final int FIELD_TARGET = 4;

	private static int listScroll = 0;

	private SlimeFlowLoginPanel() {
	}

	public static int width() {
		return W;
	}

	public static int height() {
		return isEditingEntry() ? EDIT_H : LIST_H;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		int h = height();
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, h);

		SlimeFlowUi.drawPanel(graphics, x, y, W, h, SlimeFlowTheme.BORDER);

		if (isEditingEntry()) {
			renderEditor(client, graphics, x, y);
		} else {
			renderList(client, graphics, x, y);
		}
	}

	private static void renderList(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		clampScroll();
		graphics.text(client.font, "Auto Login", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		graphics.text(client.font, String.valueOf(SlimeFlowState.autoLoginEntries.size()), x + 72, y + 6, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 52, y + 4, 14, 12, "^", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 36, y + 4, 14, 12, "v", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 20, y + 4, 14, 12, "x", SlimeFlowTheme.RED);

		if (SlimeFlowState.autoLoginEntries.isEmpty()) {
			graphics.text(client.font, "No server profile", x + 8, y + 32, SlimeFlowTheme.MUTED, false);
			graphics.text(client.font, "Create one to use /login + /server.", x + 8, y + 44, SlimeFlowTheme.MUTED, false);
		} else {
			for (int visibleIndex = 0; visibleIndex < VISIBLE_ROWS; visibleIndex++) {
				int entryIndex = listScroll + visibleIndex;
				if (entryIndex >= SlimeFlowState.autoLoginEntries.size()) {
					break;
				}

				SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEntries.get(entryIndex);
				int rowY = y + 26 + visibleIndex * ROW_H;
				SlimeFlowUi.fill(graphics, x + 4, rowY, W - 8, ROW_H - 2, visibleIndex % 2 == 0 ? SlimeFlowTheme.CARD : SlimeFlowTheme.CARD_DARK);

				String label = (entryIndex + 1) + ". " + SlimeFlowUi.cut(entry.displayName(), 14);
				graphics.text(client.font, label, x + 8, rowY + 6, entry.enabled ? SlimeFlowTheme.TEXT : SlimeFlowTheme.MUTED, false);

				int bx = x + W - 125;
				SlimeFlowUi.drawButton(client, graphics, bx, rowY + 3, 34, 13, "Edit", SlimeFlowTheme.BLUE);
				SlimeFlowUi.drawButton(client, graphics, bx + 38, rowY + 3, 38, 13, entry.enabled ? "On" : "Off", entry.enabled ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
				SlimeFlowUi.drawButton(client, graphics, bx + 80, rowY + 3, 42, 13, "Delete", SlimeFlowTheme.RED);
			}
		}

		SlimeFlowUi.drawButton(client, graphics, x + 4, y + LIST_H - 22, W - 8, 16, "Create New", SlimeFlowTheme.BLUE);
	}

	private static void renderEditor(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;
		graphics.text(client.font, SlimeFlowState.autoLoginEditingIndex >= 0 ? "Edit Server" : "Create Server", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 20, y + 4, 14, 12, "x", SlimeFlowTheme.RED);

		renderTextField(client, graphics, x, y + 28, FIELD_NAME, "Name", entry.name, 25, false);
		renderTextField(client, graphics, x, y + 50, FIELD_ADDRESS, "Address", entry.address, 22, false);
		renderTextField(client, graphics, x, y + 72, FIELD_PASSWORD, "Password", entry.password, 22, true);
		renderTextField(client, graphics, x, y + 94, FIELD_TARGET, "Realm", entry.targetServer, 22, false);

		graphics.text(client.font, "Login Delay " + safeLoginDelay(entry) + "t", x + 8, y + 122, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 56, y + 118, 18, 14, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 32, y + 118, 18, 14, "+", SlimeFlowTheme.MUTED);

		graphics.text(client.font, "Server Delay " + safeServerDelay(entry) + "t", x + 8, y + 142, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 56, y + 138, 18, 14, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 32, y + 138, 18, 14, "+", SlimeFlowTheme.MUTED);

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + 160, 70, 14, entry.enabled ? "Enabled" : "Disabled", entry.enabled ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
		SlimeFlowUi.drawButton(client, graphics, x + 84, y + 160, 78, 14, entry.autoServer ? "Auto Server" : "Login Only", entry.autoServer ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 168, y + 160, 76, 14, SlimeFlowState.autoLoginShowPassword ? "Hide Pw" : "Show Pw", SlimeFlowTheme.BLUE);

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + 182, 76, 14, "Save", SlimeFlowTheme.GREEN);
		SlimeFlowUi.drawButton(client, graphics, x + 88, y + 182, 76, 14, "Clear", SlimeFlowTheme.ORANGE);
		SlimeFlowUi.drawButton(client, graphics, x + 168, y + 182, 76, 14, "Back", SlimeFlowTheme.MUTED);

		graphics.text(client.font, "Run: /login password -> /server realm", x + 8, y + 204, SlimeFlowTheme.MUTED, false);
	}

	private static void renderTextField(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int field, String label, String value, int maxCut, boolean password) {
		boolean active = SlimeFlowState.autoLoginEditField == field;
		SlimeFlowUi.fill(graphics, x + 6, y, W - 12, 18, active ? 0x5520442C : SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 6, y, W - 12, 18, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BORDER_SOFT);

		String shown = value == null ? "" : value;
		if (password && !SlimeFlowState.autoLoginShowPassword) {
			shown = shown.isEmpty() ? "Click to type password" : mask(shown);
		} else if (shown.isEmpty()) {
			shown = "Click to type " + label.toLowerCase();
		}

		graphics.text(client.font, label + ": " + SlimeFlowUi.cut(shown, maxCut), x + 10, y + 5, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED, false);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int h = height();
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, h);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 20, y + 4, 14, 12)) {
			if (isEditingEntry()) {
				cancelEditing();
			} else {
				SlimeFlowOverlay.closeMenuOnly();
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 20)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		if (isEditingEntry()) {
			return clickEditor(client, x, y, mouseX, mouseY);
		}

		return clickList(client, x, y, mouseX, mouseY);
	}

	private static boolean clickList(Minecraft client, int x, int y, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 52, y + 4, 14, 12)) {
			listScroll = Math.max(0, listScroll - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 36, y + 4, 14, 12)) {
			listScroll++;
			clampScroll();
			return true;
		}

		for (int visibleIndex = 0; visibleIndex < VISIBLE_ROWS; visibleIndex++) {
			int entryIndex = listScroll + visibleIndex;
			if (entryIndex >= SlimeFlowState.autoLoginEntries.size()) {
				break;
			}

			SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEntries.get(entryIndex);
			int rowY = y + 26 + visibleIndex * ROW_H;
			int bx = x + W - 125;

			if (SlimeFlowUi.inside(mouseX, mouseY, bx, rowY + 3, 34, 13)) {
				beginEdit(entryIndex);
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 38, rowY + 3, 38, 13)) {
				entry.enabled = !entry.enabled;
				SlimeFlowAutoLogin.resetRuntime();
				SlimeFlowConfig.save();
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 80, rowY + 3, 42, 13)) {
				SlimeFlowState.autoLoginEntries.remove(entryIndex);
				clampScroll();
				SlimeFlowAutoLogin.resetRuntime();
				SlimeFlowConfig.save();
				return true;
			}
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 4, y + LIST_H - 22, W - 8, 16)) {
			beginCreate(client);
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, LIST_H);
	}

	private static boolean clickEditor(Minecraft client, int x, int y, double mouseX, double mouseY) {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;

		if (clickField(mouseX, mouseY, x, y + 28, FIELD_NAME)) return true;
		if (clickField(mouseX, mouseY, x, y + 50, FIELD_ADDRESS)) return true;
		if (clickField(mouseX, mouseY, x, y + 72, FIELD_PASSWORD)) return true;
		if (clickField(mouseX, mouseY, x, y + 94, FIELD_TARGET)) return true;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 56, y + 118, 18, 14)) {
			entry.loginDelayTicks = Math.max(20, safeLoginDelay(entry) - 10);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 32, y + 118, 18, 14)) {
			entry.loginDelayTicks = Math.min(200, safeLoginDelay(entry) + 10);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 56, y + 138, 18, 14)) {
			entry.serverDelayTicks = Math.max(20, safeServerDelay(entry) - 10);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 32, y + 138, 18, 14)) {
			entry.serverDelayTicks = Math.min(200, safeServerDelay(entry) + 10);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + 160, 70, 14)) {
			entry.enabled = !entry.enabled;
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 84, y + 160, 78, 14)) {
			entry.autoServer = !entry.autoServer;
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 168, y + 160, 76, 14)) {
			SlimeFlowState.autoLoginShowPassword = !SlimeFlowState.autoLoginShowPassword;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + 182, 76, 14)) {
			saveEditing();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 88, y + 182, 76, 14)) {
			clearEditing();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 168, y + 182, 76, 14)) {
			cancelEditing();
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, EDIT_H);
	}

	private static boolean clickField(double mouseX, double mouseY, int x, int y, int field) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 6, y, W - 12, 18)) {
			SlimeFlowState.autoLoginEditField = field;
			SlimeFlowState.autoLoginEditingPassword = field == FIELD_PASSWORD;
			return true;
		}
		return false;
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!isEditingEntry() || SlimeFlowState.autoLoginEditField == FIELD_NONE) {
			return false;
		}

		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			SlimeFlowState.autoLoginEditField = FIELD_NONE;
			SlimeFlowState.autoLoginEditingPassword = false;
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			SlimeFlowState.autoLoginEditField = FIELD_NONE;
			SlimeFlowState.autoLoginEditingPassword = false;
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			backspaceActiveField();
			return true;
		}

		return true;
	}

	public static boolean charTyped(char chr, int modifiers) {
		if (!isEditingEntry() || SlimeFlowState.autoLoginEditField == FIELD_NONE) {
			return false;
		}

		if (chr >= 32 && chr != 127) {
			appendActiveField(chr);
		}

		return true;
	}

	private static void beginEdit(int index) {
		if (index < 0 || index >= SlimeFlowState.autoLoginEntries.size()) {
			return;
		}

		SlimeFlowState.autoLoginEditingIndex = index;
		SlimeFlowState.autoLoginEditingEntry = SlimeFlowState.autoLoginEntries.get(index).copy();
		SlimeFlowState.autoLoginEditField = FIELD_NONE;
		SlimeFlowState.autoLoginEditingPassword = false;
	}

	private static void beginCreate(Minecraft client) {
		String address = SlimeFlowAutoLogin.currentServerAddress(client);
		SlimeFlowState.autoLoginEditingIndex = -1;
		SlimeFlowState.autoLoginEditingEntry = new SlimeFlowAutoLoginEntry(makeDefaultName(address), address);
		SlimeFlowState.autoLoginEditingEntry.enabled = true;
		SlimeFlowState.autoLoginEditingEntry.loginDelayTicks = 60;
		SlimeFlowState.autoLoginEditingEntry.serverDelayTicks = 40;
		SlimeFlowState.autoLoginEditField = FIELD_NAME;
		SlimeFlowState.autoLoginEditingPassword = false;
	}

	private static void saveEditing() {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;
		if (entry == null) {
			return;
		}

		entry.name = SlimeFlowAutoLoginEntry.clean(entry.name);
		entry.address = SlimeFlowAutoLoginEntry.clean(entry.address);
		entry.password = entry.password == null ? "" : entry.password.trim();
		entry.targetServer = SlimeFlowAutoLogin.normalizeTargetServer(entry.targetServer);
		entry.loginDelayTicks = safeLoginDelay(entry);
		entry.serverDelayTicks = safeServerDelay(entry);
		if (entry.name.isEmpty()) {
			entry.name = makeDefaultName(entry.address);
		}

		if (SlimeFlowState.autoLoginEditingIndex >= 0 && SlimeFlowState.autoLoginEditingIndex < SlimeFlowState.autoLoginEntries.size()) {
			SlimeFlowState.autoLoginEntries.set(SlimeFlowState.autoLoginEditingIndex, entry.copy());
		} else {
			SlimeFlowState.autoLoginEntries.add(entry.copy());
			listScroll = Math.max(0, SlimeFlowState.autoLoginEntries.size() - VISIBLE_ROWS);
		}

		SlimeFlowAutoLogin.resetRuntime();
		SlimeFlowConfig.save();
		cancelEditing();
	}

	private static void cancelEditing() {
		SlimeFlowState.autoLoginEditingIndex = -1;
		SlimeFlowState.autoLoginEditingEntry = null;
		SlimeFlowState.autoLoginEditField = FIELD_NONE;
		SlimeFlowState.autoLoginEditingPassword = false;
	}

	private static void clearEditing() {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;
		if (entry == null) {
			return;
		}

		entry.password = "";
		entry.targetServer = "";
		entry.autoServer = false;
		SlimeFlowState.autoLoginEditField = FIELD_NONE;
		SlimeFlowState.autoLoginEditingPassword = false;
	}

	private static void backspaceActiveField() {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;
		if (entry == null) {
			return;
		}

		if (SlimeFlowState.autoLoginEditField == FIELD_NAME) {
			entry.name = backspace(entry.name);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_ADDRESS) {
			entry.address = backspace(entry.address);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_PASSWORD) {
			entry.password = backspace(entry.password);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_TARGET) {
			entry.targetServer = backspace(entry.targetServer);
		}
	}

	private static void appendActiveField(char chr) {
		SlimeFlowAutoLoginEntry entry = SlimeFlowState.autoLoginEditingEntry;
		if (entry == null) {
			return;
		}

		if (SlimeFlowState.autoLoginEditField == FIELD_NAME) {
			entry.name = append(entry.name, chr, 32);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_ADDRESS) {
			entry.address = append(entry.address, chr, 96);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_PASSWORD) {
			entry.password = append(entry.password, chr, 64);
		} else if (SlimeFlowState.autoLoginEditField == FIELD_TARGET) {
			entry.targetServer = append(entry.targetServer, chr, 64);
		}
	}

	private static String append(String value, char chr, int max) {
		value = value == null ? "" : value;
		if (value.length() >= max) {
			return value;
		}
		return value + chr;
	}

	private static String backspace(String value) {
		value = value == null ? "" : value;
		return value.isEmpty() ? value : value.substring(0, value.length() - 1);
	}

	private static boolean isEditingEntry() {
		return SlimeFlowState.autoLoginEditingEntry != null;
	}

	private static int safeLoginDelay(SlimeFlowAutoLoginEntry entry) {
		return Math.max(20, entry.loginDelayTicks <= 0 ? 60 : entry.loginDelayTicks);
	}

	private static int safeServerDelay(SlimeFlowAutoLoginEntry entry) {
		return Math.max(20, entry.serverDelayTicks <= 0 ? 40 : entry.serverDelayTicks);
	}

	private static void clampScroll() {
		int max = Math.max(0, SlimeFlowState.autoLoginEntries.size() - VISIBLE_ROWS);
		if (listScroll > max) {
			listScroll = max;
		}
		if (listScroll < 0) {
			listScroll = 0;
		}
	}

	private static String makeDefaultName(String address) {
		String value = address == null ? "" : address.trim();
		if (!value.isEmpty()) {
			int colon = value.indexOf(':');
			if (colon > 0) {
				value = value.substring(0, colon);
			}
			return value.isEmpty() ? "Server " + (SlimeFlowState.autoLoginEntries.size() + 1) : value;
		}
		return "Server " + (SlimeFlowState.autoLoginEntries.size() + 1);
	}

	private static String mask(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		int count = Math.min(18, text.length());
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append('*');
		}
		if (text.length() > count) {
			builder.append("...");
		}
		return builder.toString();
	}
}
