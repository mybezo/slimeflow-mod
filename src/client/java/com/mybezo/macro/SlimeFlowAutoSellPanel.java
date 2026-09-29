package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowAutoSellPanel {
	private static final int W = 252;
	private static final int H = 204;

	private SlimeFlowAutoSellPanel() {
	}

	public static int width() {
		return W;
	}

	public static int height() {
		return H;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H);

		SlimeFlowUi.drawPanel(graphics, x, y, W, H, SlimeFlowTheme.BORDER);
		graphics.text(client.font, "Auto Sell", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 20, y + 4, 14, 12, "x", SlimeFlowTheme.RED);

		renderTextField(client, graphics, x, y + 28, SlimeFlowState.AUTO_SELL_FIELD_ITEM, "Item", SlimeFlowState.autoSellItemName, 24);
		SlimeFlowUi.drawButton(client, graphics, x + W - 56, y + 51, 48, 14, SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM ? "Picking" : "Pick", SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BLUE);
		graphics.text(client.font, "Pick item, sell when inventory is full.", x + 8, y + 54, SlimeFlowTheme.MUTED, false);

		renderTextField(client, graphics, x, y + 72, SlimeFlowState.AUTO_SELL_FIELD_COMMAND, "Cmd", "/" + SlimeFlowAutoSell.normalizeCommand(SlimeFlowState.autoSellCommand), 25);

		String delay = "Delay: " + Math.max(1, SlimeFlowState.autoSellIntervalAmount) + " " + SlimeFlowAutoSell.unitLabel();
		graphics.text(client.font, delay, x + 8, y + 102, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 82, y + 98, 18, 14, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 60, y + 98, 18, 14, "+", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + W - 38, y + 98, 30, 14, SlimeFlowAutoSell.unitLabel(), SlimeFlowTheme.BLUE);

		String key = SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_AUTO_SELL ? "Press key..." : SlimeFlowKeyNames.name(SlimeFlowState.autoSellKeybind);
		graphics.text(client.font, "Key: " + key, x + 8, y + 126, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 104, y + 122, 48, 14, "Bind", SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, x + W - 52, y + 122, 44, 14, "Clear", SlimeFlowTheme.ORANGE);

		SlimeFlowUi.drawButton(client, graphics, x + 8, y + 148, 58, 14, SlimeFlowState.autoSellEnabled ? "Enabled" : "Disabled", SlimeFlowState.autoSellEnabled ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
		SlimeFlowUi.drawButton(client, graphics, x + 70, y + 148, 54, 14, "Save", SlimeFlowTheme.GREEN);
		SlimeFlowUi.drawButton(client, graphics, x + 128, y + 148, 54, 14, "Run", SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, x + 186, y + 148, 58, 14, "Clear", SlimeFlowTheme.ORANGE);

		String status = statusText();
		graphics.text(client.font, status, x + 8, y + 176, SlimeFlowTheme.MUTED, false);
		graphics.text(client.font, "Key is unbound by default. Bind it if needed.", x + 8, y + 188, SlimeFlowTheme.MUTED, false);
	}

	private static void renderTextField(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int field, String label, String value, int maxCut) {
		boolean active = SlimeFlowState.autoSellEditField == field;
		SlimeFlowUi.fill(graphics, x + 6, y, W - 12, 18, active ? 0x5520442C : SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 6, y, W - 12, 18, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BORDER_SOFT);
		String shown = value == null || value.isEmpty() ? "Click to type " + label.toLowerCase() : value;
		graphics.text(client.font, label + ": " + SlimeFlowUi.cut(shown, maxCut), x + 10, y + 5, active ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED, false);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 20, y + 4, 14, 12)) {
			SlimeFlowOverlay.closeMenuOnly();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 20)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		if (clickField(mouseX, mouseY, x, y + 28, SlimeFlowState.AUTO_SELL_FIELD_ITEM)) return true;
		if (clickField(mouseX, mouseY, x, y + 72, SlimeFlowState.AUTO_SELL_FIELD_COMMAND)) return true;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 56, y + 51, 48, 14)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM) {
				SlimeFlowState.finishPick(true);
			} else {
				SlimeFlowState.beginPick(SlimeFlowState.PickMode.AUTO_SELL_ITEM);
			}
			SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 82, y + 98, 18, 14)) {
			SlimeFlowAutoSell.changeAmount(-1);
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 60, y + 98, 18, 14)) {
			SlimeFlowAutoSell.changeAmount(1);
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 38, y + 98, 30, 14)) {
			SlimeFlowAutoSell.cycleUnit();
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 104, y + 122, 48, 14)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_AUTO_SELL;
			SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
			SlimeFlowState.finishPick(true);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 52, y + 122, 44, 14)) {
			SlimeFlowState.autoSellKeybind = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, y + 148, 58, 14)) {
			SlimeFlowState.autoSellEnabled = !SlimeFlowState.autoSellEnabled;
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 70, y + 148, 54, 14)) {
			SlimeFlowState.autoSellCommand = SlimeFlowAutoSell.normalizeCommand(SlimeFlowState.autoSellCommand);
			SlimeFlowState.autoSellItemName = SlimeFlowAutoSell.clean(SlimeFlowState.autoSellItemName);
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowConfig.save();
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 128, y + 148, 54, 14)) {
			SlimeFlowAutoSell.runCommandNow(client);
			return true;
		}
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 186, y + 148, 58, 14)) {
			SlimeFlowState.autoSellItemName = "";
			SlimeFlowState.autoSellCommand = "sell all";
			SlimeFlowState.autoSellEnabled = false;
			SlimeFlowState.autoSellKeybind = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.autoSellCooldownTicks = 0;
			SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowState.finishPick(true);
			SlimeFlowConfig.save();
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_AUTO_SELL) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				SlimeFlowState.autoSellKeybind = SlimeFlowState.KEYBIND_NONE;
			} else if (SlimeFlowKeyNames.canBind(keyCode)) {
				SlimeFlowState.autoSellKeybind = keyCode;
			}
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowConfig.save();
			return true;
		}

		if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_NONE) {
			return false;
		}

		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
			SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			backspaceActiveField();
			return true;
		}

		return true;
	}

	public static boolean charTyped(char chr, int modifiers) {
		if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_NONE) {
			return false;
		}

		if (chr >= 32 && chr != 127) {
			appendActiveField(chr);
		}
		return true;
	}

	static boolean insidePanel(int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H);
		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H);
	}

	private static boolean clickField(double mouseX, double mouseY, int x, int y, int field) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 6, y, W - 12, 18)) {
			SlimeFlowState.autoSellEditField = field;
			SlimeFlowState.finishPick(true);
			return true;
		}
		return false;
	}

	private static void backspaceActiveField() {
		if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_ITEM) {
			SlimeFlowState.autoSellItemName = backspace(SlimeFlowState.autoSellItemName);
		} else if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_COMMAND) {
			SlimeFlowState.autoSellCommand = backspace(SlimeFlowState.autoSellCommand);
		}
	}

	private static void appendActiveField(char chr) {
		if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_ITEM) {
			SlimeFlowState.autoSellItemName = append(SlimeFlowState.autoSellItemName, chr, 64);
		} else if (SlimeFlowState.autoSellEditField == SlimeFlowState.AUTO_SELL_FIELD_COMMAND) {
			SlimeFlowState.autoSellCommand = append(SlimeFlowState.autoSellCommand, chr, 96);
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

	private static String statusText() {
		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM) {
			return "Click item in inventory to pick.";
		}
		if (SlimeFlowState.autoSellItemName == null || SlimeFlowState.autoSellItemName.trim().isEmpty()) {
			return "Pick item first.";
		}
		if (SlimeFlowAutoSell.normalizeCommand(SlimeFlowState.autoSellCommand).isEmpty()) {
			return "Set sell command first.";
		}
		return "Will sell when picked item is full.";
	}
}
