package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowAutoCommandPanel {
	private static final int W = 252;
	private static final int H_LIST = 160;
	private static final int H_EDIT = 218;

	private SlimeFlowAutoCommandPanel() {
	}

	public static int width() {
		return W;
	}

	public static int height() {
		return isEditing() ? H_EDIT : H_LIST;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		if (isEditing()) {
			renderEdit(client, graphics, screenWidth, screenHeight);
			return;
		}
		renderList(client, graphics, screenWidth, screenHeight);
	}

	private static void renderList(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H_LIST);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H_LIST);

		SlimeFlowUi.drawPanel(graphics, x, y, W, H_LIST, SlimeFlowTheme.BORDER);
		graphics.text(client.font, "Auto Command", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 20, y + 4, 14, 12, "x", SlimeFlowTheme.RED);

		int rowY = y + 28;
		int maxRows = 5;
		for (int i = 0; i < SlimeFlowState.autoCommandEntries.size() && i < maxRows; i++) {
			SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEntries.get(i);
			String label = (i + 1) + ". " + SlimeFlowUi.cut(SlimeFlowAutoCommand.displayName(entry, i), 14);
			graphics.text(client.font, label, x + 8, rowY + 4, SlimeFlowTheme.MUTED, false);
			SlimeFlowUi.drawButton(client, graphics, x + 110, rowY, 38, 14, "Edit", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, x + 152, rowY, 40, 14, entry != null && entry.enabled ? "On" : "Off", entry != null && entry.enabled ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
			SlimeFlowUi.drawButton(client, graphics, x + 196, rowY, 48, 14, "Delete", SlimeFlowTheme.ORANGE);
			rowY += 19;
		}

		if (SlimeFlowState.autoCommandEntries.isEmpty()) {
			graphics.text(client.font, "Belum ada command.", x + 8, y + 48, SlimeFlowTheme.MUTED, false);
		}

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + H_LIST - 24, W - 16, 16, "Create New", SlimeFlowTheme.GREEN);
	}

	private static void renderEdit(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEditingEntry;
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H_EDIT);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H_EDIT);

		SlimeFlowUi.drawPanel(graphics, x, y, W, H_EDIT, SlimeFlowTheme.BORDER);
		graphics.text(client.font, "Edit Command", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 42, y + 4, 36, 12, "Back", SlimeFlowTheme.RED);

		renderTextField(client, graphics, x, y + 28, SlimeFlowState.AUTO_COMMAND_FIELD_NAME, "Name", entry == null ? "" : entry.name, 24);
		renderTextField(client, graphics, x, y + 54, SlimeFlowState.AUTO_COMMAND_FIELD_COMMAND, "Cmd", "/" + SlimeFlowAutoCommand.normalizeCommand(entry == null ? "" : entry.command), 25);

		String delay = entry == null ? "Delay: 5 detik" : "Delay: " + Math.max(1, entry.intervalAmount) + " " + SlimeFlowAutoCommand.unitLabel(entry.intervalUnit);
		graphics.text(client.font, delay, x + 8, y + 86, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 82, y + 82, 18, 14, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 60, y + 82, 18, 14, "+", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 38, y + 82, 30, 14, entry == null ? "detik" : SlimeFlowAutoCommand.unitLabel(entry.intervalUnit), SlimeFlowTheme.BLUE);

		String key = SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_AUTO_COMMAND ? "Press key..." : SlimeFlowKeyNames.name(entry == null ? 0 : entry.keybind);
		graphics.text(client.font, "Key: " + key, x + 8, y + 112, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 104, y + 108, 48, 14, "Bind", SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, x + W - 52, y + 108, 44, 14, "Clear", SlimeFlowTheme.ORANGE);

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + 136, 76, 14, entry != null && entry.enabled ? "Enabled" : "Disabled", entry != null && entry.enabled ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
		SlimeFlowUi.drawButton(client, graphics, x + 88, y + 136, 76, 14, "Save", SlimeFlowTheme.GREEN);
		SlimeFlowUi.drawButton(client, graphics, x + 168, y + 136, 76, 14, "Run", SlimeFlowTheme.BLUE);

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + 160, W - 16, 16, "Delete", SlimeFlowTheme.ORANGE);

		graphics.text(client.font, "Key is unbound by default. Bind it if needed.", x + 8, y + 188, SlimeFlowTheme.MUTED, false);
		graphics.text(client.font, "Command runs on the set delay while enabled.", x + 8, y + 200, SlimeFlowTheme.MUTED, false);
	}

	private static void renderTextField(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int field, String label, String value, int maxCut) {
		boolean active = SlimeFlowState.autoCommandEditField == field;
		SlimeFlowUi.fill(graphics, x + 6, y, W - 12, 18, active ? 0x5520442C : SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 6, y, W - 12, 18, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BORDER_SOFT);
		String shown = value == null || value.isEmpty() ? "Click to type " + label.toLowerCase() : value;
		graphics.text(client.font, label + ": " + SlimeFlowUi.cut(shown, maxCut), x + 10, y + 5, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED, false);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		if (isEditing()) {
			return clickEdit(client, screenWidth, screenHeight, mouseX, mouseY);
		}
		return clickList(screenWidth, screenHeight, mouseX, mouseY);
	}

	private static boolean clickList(int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H_LIST);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H_LIST);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 20, y + 4, 14, 12)) {
			SlimeFlowOverlay.closeMenuOnly();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 20)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		int rowY = y + 28;
		int maxRows = Math.min(5, SlimeFlowState.autoCommandEntries.size());
		for (int i = 0; i < maxRows; i++) {
			if (SlimeFlowUi.inside(mouseX, mouseY, x + 110, rowY, 38, 14)) {
				edit(i);
				return true;
			}
			if (SlimeFlowUi.inside(mouseX, mouseY, x + 152, rowY, 40, 14)) {
				SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEntries.get(i);
				entry.enabled = !entry.enabled;
				entry.cooldownTicks = 0;
				SlimeFlowConfig.save();
				return true;
			}
			if (SlimeFlowUi.inside(mouseX, mouseY, x + 196, rowY, 48, 14)) {
				SlimeFlowState.autoCommandEntries.remove(i);
				SlimeFlowConfig.save();
				return true;
			}
			rowY += 19;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + H_LIST - 24, W - 16, 16)) {
			SlimeFlowAutoCommandEntry entry = new SlimeFlowAutoCommandEntry("Command " + (SlimeFlowState.autoCommandEntries.size() + 1));
			SlimeFlowState.autoCommandEntries.add(entry);
			edit(SlimeFlowState.autoCommandEntries.size() - 1);
			SlimeFlowConfig.save();
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H_LIST);
	}

	private static boolean clickEdit(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEditingEntry;
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H_EDIT);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H_EDIT);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 42, y + 4, 36, 12)) {
			backToList();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 20)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}
		if (entry == null) return true;

		if (clickField(mouseX, mouseY, x, y + 28, SlimeFlowState.AUTO_COMMAND_FIELD_NAME)) return true;
		if (clickField(mouseX, mouseY, x, y + 54, SlimeFlowState.AUTO_COMMAND_FIELD_COMMAND)) return true;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 82, y + 82, 18, 14)) {
			SlimeFlowAutoCommand.changeAmount(entry, -1);
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 60, y + 82, 18, 14)) {
			SlimeFlowAutoCommand.changeAmount(entry, 1);
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 38, y + 82, 30, 14)) {
			SlimeFlowAutoCommand.cycleUnit(entry);
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 104, y + 108, 48, 14)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_AUTO_COMMAND;
			SlimeFlowState.autoCommandEditingKeyIndex = SlimeFlowState.autoCommandEditingIndex;
			SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 52, y + 108, 44, 14)) {
			entry.keybind = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.autoCommandEditingKeyIndex = -1;
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + 136, 76, 14)) {
			entry.enabled = !entry.enabled;
			entry.cooldownTicks = 0;
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 88, y + 136, 76, 14)) {
			normalizeEntry(entry);
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 168, y + 136, 76, 14)) {
			SlimeFlowAutoCommand.runEntry(client, entry);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + 160, W - 16, 16)) {
			int index = SlimeFlowState.autoCommandEditingIndex;
			if (index >= 0 && index < SlimeFlowState.autoCommandEntries.size()) {
				SlimeFlowState.autoCommandEntries.remove(index);
			}
			backToList();
			SlimeFlowConfig.save();
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H_EDIT);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_AUTO_COMMAND) {
			SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEditingEntry;
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				if (entry != null) entry.keybind = SlimeFlowState.KEYBIND_NONE;
			} else if (SlimeFlowKeyNames.canBind(keyCode)) {
				if (entry != null) entry.keybind = keyCode;
			}
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.autoCommandEditingKeyIndex = -1;
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_NONE) {
			return false;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
			SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			backspaceActiveField();
			return true;
		}
		return true;
	}

	public static boolean charTyped(char chr, int modifiers) {
		if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_NONE || !isEditing()) {
			return false;
		}
		if (chr >= 32 && chr != 127) {
			appendActiveField(chr);
		}
		return true;
	}

	static boolean insidePanel(int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int h = height();
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, h);
		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, h);
	}

	private static boolean isEditing() {
		return SlimeFlowState.autoCommandEditingEntry != null;
	}

	private static void edit(int index) {
		if (index < 0 || index >= SlimeFlowState.autoCommandEntries.size()) return;
		SlimeFlowState.autoCommandEditingIndex = index;
		SlimeFlowState.autoCommandEditingEntry = SlimeFlowState.autoCommandEntries.get(index);
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
		SlimeFlowState.autoCommandEditingKeyIndex = -1;
	}

	private static void backToList() {
		SlimeFlowState.autoCommandEditingIndex = -1;
		SlimeFlowState.autoCommandEditingEntry = null;
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
		SlimeFlowState.autoCommandEditingKeyIndex = -1;
	}

	private static boolean clickField(double mouseX, double mouseY, int x, int y, int field) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 6, y, W - 12, 18)) {
			SlimeFlowState.autoCommandEditField = field;
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			return true;
		}
		return false;
	}

	private static void normalizeEntry(SlimeFlowAutoCommandEntry entry) {
		if (entry == null) return;
		entry.name = entry.name == null ? "Command" : entry.name.trim();
		if (entry.name.isEmpty()) entry.name = "Command";
		entry.command = SlimeFlowAutoCommand.normalizeCommand(entry.command);
		entry.intervalUnit = normalizeUnit(entry.intervalUnit);
		entry.intervalAmount = normalizeAmount(entry.intervalAmount, entry.intervalUnit);
		entry.keybind = Math.max(0, entry.keybind);
		entry.cooldownTicks = 0;
	}

	private static int normalizeUnit(int unit) {
		if (unit < SlimeFlowState.AUTO_SELL_UNIT_MS || unit > SlimeFlowState.AUTO_SELL_UNIT_HOURS) return SlimeFlowState.AUTO_SELL_UNIT_SECONDS;
		return unit;
	}

	private static int normalizeAmount(int amount, int unit) {
		unit = normalizeUnit(unit);
		if (unit == SlimeFlowState.AUTO_SELL_UNIT_MS) return Math.max(50, amount <= 0 ? 500 : amount);
		return Math.max(1, amount <= 0 ? 5 : amount);
	}

	private static void backspaceActiveField() {
		SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEditingEntry;
		if (entry == null) return;
		if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_NAME) {
			entry.name = backspace(entry.name);
		} else if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_COMMAND) {
			entry.command = backspace(entry.command);
		}
	}

	private static void appendActiveField(char chr) {
		SlimeFlowAutoCommandEntry entry = SlimeFlowState.autoCommandEditingEntry;
		if (entry == null) return;
		if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_NAME) {
			entry.name = append(entry.name, chr, 48);
		} else if (SlimeFlowState.autoCommandEditField == SlimeFlowState.AUTO_COMMAND_FIELD_COMMAND) {
			entry.command = append(entry.command, chr, 96);
		}
	}

	private static String append(String value, char chr, int max) {
		value = value == null ? "" : value;
		if (value.length() >= max) return value;
		return value + chr;
	}

	private static String backspace(String value) {
		value = value == null ? "" : value;
		return value.isEmpty() ? value : value.substring(0, value.length() - 1);
	}
}
