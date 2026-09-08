package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowListPanel {
	private static final int LIST_W = 190;
	private static final int LIST_H = 180;
	private static final int LIST_VISIBLE_ROWS = 5;
	private static final int ROW_H = 16;
	private static final int ROW_START_Y = 20;
	private static final int STACK_H = 50;

	private static int listScroll = 0;

	private SlimeFlowListPanel() {
	}

	public static int width() {
		return LIST_W;
	}

	public static int height() {
		return LIST_H;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, LIST_W, LIST_H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, LIST_W, LIST_H);

		clampScroll();

		SlimeFlowUi.drawPanel(graphics, x, y, LIST_W, LIST_H, SlimeFlowTheme.BORDER);

		renderHeader(client, graphics, x, y);
		renderMacroList(client, graphics, x, y);
		renderStackControls(client, graphics, x, y);
		renderFooter(client, graphics, x, y);
	}

	private static void renderHeader(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		graphics.text(client.font, "Macro", x + 5, y + 4, SlimeFlowTheme.TEXT, false);

		SlimeFlowUi.drawButton(client, graphics, x + LIST_W - 40, y + 2, 12, 11, "^", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + LIST_W - 27, y + 2, 12, 11, "v", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + LIST_W - 14, y + 2, 12, 11, "x", SlimeFlowTheme.RED);
	}

	private static void renderMacroList(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		if (SlimeFlowState.profiles.isEmpty()) {
			graphics.text(client.font, "Empty", x + 6, y + ROW_START_Y + 4, SlimeFlowTheme.MUTED, false);
			return;
		}

		for (int visibleIndex = 0; visibleIndex < LIST_VISIBLE_ROWS; visibleIndex++) {
			int profileIndex = listScroll + visibleIndex;

			if (profileIndex >= SlimeFlowState.profiles.size()) {
				break;
			}

			SlimeFlowProfile profile = SlimeFlowState.profiles.get(profileIndex);
			int rowY = y + ROW_START_Y + visibleIndex * ROW_H;

			SlimeFlowUi.fill(graphics, x + 3, rowY, LIST_W - 6, ROW_H - 2, visibleIndex % 2 == 0 ? SlimeFlowTheme.CARD : SlimeFlowTheme.CARD_DARK);
			graphics.text(client.font, (profileIndex + 1) + "." + SlimeFlowUi.cut(profile.name, 8), x + 5, rowY + 3, SlimeFlowTheme.TEXT, false);

			int bx = x + LIST_W - 88;
			SlimeFlowUi.drawButton(client, graphics, bx, rowY + 2, 18, 11, "Run", SlimeFlowTheme.GREEN);
			SlimeFlowUi.drawButton(client, graphics, bx + 20, rowY + 2, 15, 11, "Ed", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, bx + 37, rowY + 2, 15, 11, "Cp", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, bx + 54, rowY + 2, 22, 11, profile.autoRun ? "Auto" : "Off", profile.autoRun ? SlimeFlowTheme.GREEN : SlimeFlowTheme.RED);
			SlimeFlowUi.drawButton(client, graphics, bx + 78, rowY + 2, 10, 11, "x", SlimeFlowTheme.RED);
		}
	}

	private static void renderStackControls(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		int footerY = y + LIST_H - 18;
		int stackY = footerY - STACK_H;

		SlimeFlowUi.fill(graphics, x + 3, stackY, LIST_W - 6, STACK_H, SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 3, stackY, LIST_W - 6, STACK_H, SlimeFlowTheme.BORDER_SOFT);

		String mode = SlimeFlowState.stackClickMode ? "Click" : "Open";
		String button = SlimeFlowState.stackClickLeft ? "L" : "R";

		graphics.text(client.font, "Stack", x + 6, stackY + 3, SlimeFlowTheme.BLUE, false);

		SlimeFlowUi.drawButton(client, graphics, x + 6, stackY + 15, 32, 11, mode, SlimeFlowTheme.YELLOW);
		SlimeFlowUi.drawButton(client, graphics, x + 40, stackY + 15, 16, 11, button, SlimeFlowTheme.YELLOW);
		SlimeFlowUi.drawButton(client, graphics, x + 58, stackY + 15, 26, 11, "H" + SlimeFlowState.stackLimit, SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 86, stackY + 15, 28, 11, selectionStatus(), SlimeFlowTheme.MUTED);

		SlimeFlowUi.drawButton(client, graphics, x + 6, stackY + 29, 15, 11, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 23, stackY + 29, 15, 11, "+", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 40, stackY + 29, 32, 11, "Clear", SlimeFlowTheme.ORANGE);
		SlimeFlowUi.drawButton(client, graphics, x + 74, stackY + 29, 33, 11, "Start", SlimeFlowState.stackRunning ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, x + 109, stackY + 29, 33, 11, "Stop", SlimeFlowTheme.RED);
	}

	private static void renderFooter(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		int footerY = y + LIST_H - 18;

		SlimeFlowUi.drawButton(client, graphics, x + 3, footerY, LIST_W - 6, 14, "Create", SlimeFlowTheme.BLUE);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, LIST_W, LIST_H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, LIST_W, LIST_H);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + LIST_W - 40, y + 2, 12, 11)) {
			listScroll = Math.max(0, listScroll - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + LIST_W - 27, y + 2, 12, 11)) {
			listScroll++;
			clampScroll();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + LIST_W - 14, y + 2, 12, 11)) {
			SlimeFlowOverlay.closeMenuOnly();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, LIST_W, ROW_START_Y)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		for (int visibleIndex = 0; visibleIndex < LIST_VISIBLE_ROWS; visibleIndex++) {
			int profileIndex = listScroll + visibleIndex;

			if (profileIndex >= SlimeFlowState.profiles.size()) {
				break;
			}

			SlimeFlowProfile profile = SlimeFlowState.profiles.get(profileIndex);
			int rowY = y + ROW_START_Y + visibleIndex * ROW_H;

			int bx = x + LIST_W - 88;

			if (SlimeFlowUi.inside(mouseX, mouseY, bx, rowY + 2, 18, 11)) {
				if (client.player != null && client.gameMode != null) {
					SlimeFlowRunner.scheduleProfile(client, profile, true);
				}
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 20, rowY + 2, 15, 11)) {
				SlimeFlowState.editingProfile = SlimeFlowUi.copyProfile(profile);
				SlimeFlowState.editingProfileIndex = profileIndex;
				SlimeFlowState.resetDraft();
				SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.EDIT;
				SlimeFlowEditPanel.resetScroll();
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 37, rowY + 2, 15, 11)) {
				duplicateProfile(profile);
				SlimeFlowConfig.save();
				clampScroll();
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 54, rowY + 2, 22, 11)) {
				profile.autoRun = !profile.autoRun;
				SlimeFlowConfig.save();
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, bx + 78, rowY + 2, 10, 11)) {
				stopBeforeProfileChange(client);
				SlimeFlowState.profiles.remove(profileIndex);
				clampScroll();
				SlimeFlowConfig.save();
				return true;
			}
		}


		if (clickStackControls(client, x, y, mouseX, mouseY)) {
			return true;
		}

		int footerY = y + LIST_H - 18;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 3, footerY, LIST_W - 6, 14)) {
			SlimeFlowProfile p = new SlimeFlowProfile(makeNewProfileName(), SlimeFlowState.getCurrentGuiTitle(client));

			SlimeFlowState.editingProfile = p;
			SlimeFlowState.editingProfileIndex = -1;
			SlimeFlowState.resetDraft();
			SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.EDIT;
			SlimeFlowEditPanel.resetScroll();
			return true;
		}


		return SlimeFlowUi.inside(mouseX, mouseY, x, y, LIST_W, LIST_H);
	}

	private static boolean clickStackControls(Minecraft client, int x, int y, double mouseX, double mouseY) {
		int footerY = y + LIST_H - 18;
		int stackY = footerY - STACK_H;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 6, stackY + 15, 32, 11)) {
			SlimeFlowState.stackClickMode = !SlimeFlowState.stackClickMode;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 40, stackY + 15, 16, 11)) {
			SlimeFlowState.stackClickLeft = !SlimeFlowState.stackClickLeft;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 6, stackY + 29, 15, 11)) {
			SlimeFlowState.stackLimit = Math.max(1, SlimeFlowState.stackLimit - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 23, stackY + 29, 15, 11)) {
			SlimeFlowState.stackLimit = Math.min(256, SlimeFlowState.stackLimit + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 40, stackY + 29, 32, 11)) {
			clearSelection();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 74, stackY + 29, 33, 11)) {
			if (client.player != null && client.gameMode != null) {
				SlimeFlowStackRunner.startFromCrosshair(client);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 109, stackY + 29, 33, 11)) {
			stopBeforeProfileChange(client);
			return true;
		}

		return false;
	}

	private static void stopBeforeProfileChange(Minecraft client) {
		if (client == null || client.player == null || client.gameMode == null) {
			SlimeFlowState.stopMacroRuntimeState();
			return;
		}

		SlimeFlowStackRunner.stop(client);
		SlimeFlowBackpackRefillRunner.stopFromButton(client);
		SlimeFlowState.stopMacroRuntimeState();
	}

	private static void duplicateProfile(SlimeFlowProfile source) {
		SlimeFlowProfile copy = SlimeFlowUi.copyProfile(source);
		copy.name = makeCopyName(source.name);
		copy.autoRun = false;

		SlimeFlowState.profiles.add(copy);
		scrollToBottom();
	}

	private static String makeNewProfileName() {
		int number = SlimeFlowState.profiles.size() + 1;
		String name = "m" + number;
		while (profileNameExists(name)) {
			number++;
			name = "m" + number;
		}
		return name;
	}

	private static String makeCopyName(String name) {
		String base = name == null || name.isEmpty() ? "Macro" : name;

		if (base.length() > 8) {
			base = base.substring(0, 8);
		}

		String copyName = base + "_c";
		int number = 2;

		while (profileNameExists(copyName)) {
			copyName = base + "_c" + number;
			number++;
		}

		return copyName;
	}

	private static boolean profileNameExists(String name) {
		for (SlimeFlowProfile profile : SlimeFlowState.profiles) {
			if (profile.name != null && profile.name.equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	private static String selectionStatus() {
		if (SlimeFlowState.hasStackRecordedTargets()) {
			return "M" + SlimeFlowState.stackTargets.size();
		}

		int status = 0;

		if (SlimeFlowState.stackSelectionPos1 != null) {
			status++;
		}

		if (SlimeFlowState.stackSelectionPos2 != null) {
			status++;
		}

		return status + "/2";
	}

	private static void clearSelection() {
		SlimeFlowState.stackSelectionPos1 = null;
		SlimeFlowState.stackSelectionPos2 = null;
		SlimeFlowState.stackTargets.clear();
		SlimeFlowConfig.save();
	}

	private static void clampScroll() {
		int max = Math.max(0, SlimeFlowState.profiles.size() - LIST_VISIBLE_ROWS);

		if (listScroll < 0) {
			listScroll = 0;
		}

		if (listScroll > max) {
			listScroll = max;
		}
	}

	public static void scrollToBottom() {
		listScroll = Math.max(0, SlimeFlowState.profiles.size() - LIST_VISIBLE_ROWS);
	}
}
