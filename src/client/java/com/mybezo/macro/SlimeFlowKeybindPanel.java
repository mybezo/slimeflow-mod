package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowKeybindPanel {
	private static final int W = 252;
	private static final int H = 116;

	private SlimeFlowKeybindPanel() {
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
		graphics.text(client.font, "Keybind", x + 6, y + 6, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 20, y + 4, 14, 12, "x", SlimeFlowTheme.RED);

		renderRow(client, graphics, x, y, 0, "Open UI", SlimeFlowState.KEYBIND_OPEN_UI, SlimeFlowState.keyOpenUi);
		renderRow(client, graphics, x, y, 1, "Stop All", SlimeFlowState.KEYBIND_STOP_ALL, SlimeFlowState.keyStopAll);
		renderRow(client, graphics, x, y, 2, "Stack Start", SlimeFlowState.KEYBIND_START_STACK, SlimeFlowState.keyStartStack);

		String hint = SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_NONE
				? "Click a key box to change."
				: "Press key... Esc cancel";
		graphics.text(client.font, hint, x + 8, y + 98, SlimeFlowTheme.MUTED, false);
	}

	private static void renderRow(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int row, String label, int action, int key) {
		int rowY = y + 26 + row * 22;
		boolean editing = SlimeFlowState.keybindEditingAction == action;
		SlimeFlowUi.fill(graphics, x + 6, rowY, W - 12, 18, row % 2 == 0 ? SlimeFlowTheme.CARD : SlimeFlowTheme.CARD_DARK);
		graphics.text(client.font, label, x + 10, rowY + 5, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 78, rowY + 2, 68, 14, editing ? "Press..." : keyName(key), editing ? SlimeFlowTheme.YELLOW : SlimeFlowTheme.BLUE);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 20, y + 4, 14, 12)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			SlimeFlowOverlay.closeMenuOnly();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 20)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		if (clickRow(mouseX, mouseY, x, y, 0)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_OPEN_UI;
			return true;
		}

		if (clickRow(mouseX, mouseY, x, y, 1)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_STOP_ALL;
			return true;
		}

		if (clickRow(mouseX, mouseY, x, y, 2)) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_START_STACK;
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H);
	}

	private static boolean clickRow(double mouseX, double mouseY, int x, int y, int row) {
		int rowY = y + 26 + row * 22;
		return SlimeFlowUi.inside(mouseX, mouseY, x + W - 78, rowY + 2, 68, 14);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_NONE) {
			return false;
		}

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
			return true;
		}

		if (keyCode <= 0 || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
			return true;
		}

		if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_OPEN_UI) {
			SlimeFlowState.keyOpenUi = keyCode;
		} else if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_STOP_ALL) {
			SlimeFlowState.keyStopAll = keyCode;
		} else if (SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_START_STACK) {
			SlimeFlowState.keyStartStack = keyCode;
		}

		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
		SlimeFlowConfig.save();
		return true;
	}

	static String keyName(int keyCode) {
		String name = GLFW.glfwGetKeyName(keyCode, 0);
		if (name != null && !name.isBlank()) {
			return name.toUpperCase();
		}

		if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
			return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
		}

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) return "ESC";
		if (keyCode == GLFW.GLFW_KEY_TAB) return "TAB";
		if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) return "L-SHIFT";
		if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) return "R-SHIFT";
		if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) return "L-CTRL";
		if (keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) return "R-CTRL";
		if (keyCode == GLFW.GLFW_KEY_LEFT_ALT) return "L-ALT";
		if (keyCode == GLFW.GLFW_KEY_RIGHT_ALT) return "R-ALT";
		if (keyCode == GLFW.GLFW_KEY_SPACE) return "SPACE";
		if (keyCode == GLFW.GLFW_KEY_ENTER) return "ENTER";
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) return "BACK";
		if (keyCode == GLFW.GLFW_KEY_INSERT) return "INS";
		if (keyCode == GLFW.GLFW_KEY_DELETE) return "DEL";
		if (keyCode == GLFW.GLFW_KEY_HOME) return "HOME";
		if (keyCode == GLFW.GLFW_KEY_END) return "END";
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP) return "PGUP";
		if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) return "PGDN";
		if (keyCode == GLFW.GLFW_KEY_UP) return "UP";
		if (keyCode == GLFW.GLFW_KEY_DOWN) return "DOWN";
		if (keyCode == GLFW.GLFW_KEY_LEFT) return "LEFT";
		if (keyCode == GLFW.GLFW_KEY_RIGHT) return "RIGHT";

		return "KEY " + keyCode;
	}
}
