package com.mybezo.macro;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class SlimeFlowOverlay {
	/** Everything SlimeFlow draws is rendered at this scale. */
	private static final float UI_SCALE = 0.8f;

	private static final int HUD_X = 4;
	private static final int HUD_Y = 4;
	private static final int SIDE_X = 3;
	private static final int SIDE_Y = 3;
	private static final int ICON_SIZE = 16;
	private static final int ICON_GAP = 2;
	private static final int ICON_COUNT = 5;
	private static final int SIDE_W = ICON_SIZE * ICON_COUNT + ICON_GAP * (ICON_COUNT - 1);
	private static final int SIDE_BUTTON_H = ICON_SIZE;
	private static final int PANEL_Y = HUD_Y + SIDE_BUTTON_H + 4;
	private static final int PICK_BAR_W = 286;
	private static final int PICK_BAR_H = 58;
	private static final String DISCORD_URL = "https://discord.gg/hZ6m9JgDBq";
	private static final String DISCORD_LABEL = "Join Discord";
	private static final String TOP_LABEL_LEFT = "SlimeFlow";
	private static final String TOP_LABEL_MID = " by Mybezo · ";
	private static final String DISCORD_COPIED = "Discord link copied";

	private static boolean hidden = true;
	private static boolean hudExpanded = true;
	private static boolean macroOpen = false;
	private static Field leftPosField;
	private static Field topPosField;
	private static boolean triedLeftPosField = false;
	private static boolean triedTopPosField = false;

	private SlimeFlowOverlay() {
	}

	private static int realScreenWidth(Minecraft client, int fallback) {
		try {
			if (client != null && client.getWindow() != null) {
				return Math.max(fallback, client.getWindow().getGuiScaledWidth());
			}
		} catch (Throwable ignored) {
		}
		return fallback;
	}

	private static int realScreenHeight(Minecraft client, int fallback) {
		try {
			if (client != null && client.getWindow() != null) {
				return Math.max(fallback, client.getWindow().getGuiScaledHeight());
			}
		} catch (Throwable ignored) {
		}
		return fallback;
	}

	public static void renderGameplayHud(
			Minecraft client,
			GuiGraphicsExtractor graphics,
			int screenWidth,
			int screenHeight
	) {
		// SlimeFlow 26.1.2: HUD only renders in click mode (F12) or over an open GUI.
		return;
	}

	public static void render(
			Minecraft client,
			GuiGraphicsExtractor graphics,
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY
	) {
		if (client == null || hidden || client.player == null) {
			return;
		}

		screenWidth = realScreenWidth(client, screenWidth);
		screenHeight = realScreenHeight(client, screenHeight);

		if (macroOpen && SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			// Real slot coordinates - stays outside the scale transform.
			renderSlotPickHighlights(client, graphics);
		}

		// Draw into a virtual canvas (screen size / UI_SCALE) so scaling down stays anchored to the same screen area.
		graphics.pose().pushMatrix();
		graphics.pose().scale(UI_SCALE, UI_SCALE);

		int vw = (int) (screenWidth / UI_SCALE);
		int vh = (int) (screenHeight / UI_SCALE);
		int vMouseX = (int) (mouseX / UI_SCALE);
		int vMouseY = (int) (mouseY / UI_SCALE);

		// While a slot pick is in progress, hide the menu/HUD. The slot overlay stays until Save/Back.
		if (shouldHideUiForPick()) {
			renderPickTopBox(client, graphics, vw, vh, vMouseX, vMouseY);
			graphics.pose().popMatrix();
			return;
		}

		renderSlimeFlowHud(client, graphics, vw, vh, vMouseX, vMouseY);

		if (!macroOpen) {
			graphics.pose().popMatrix();
			return;
		}

		SlimeFlowUi.updateDrag(
				client,
				vw,
				vh,
				currentPanelWidth(vh),
				currentPanelHeight(vh),
				vMouseX,
				vMouseY
		);

		renderCurrentPanel(client, graphics, vw, vh);

		graphics.pose().popMatrix();
	}

	private static void renderSlimeFlowHud(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
		boolean running = SlimeFlowState.stackRunning
				|| SlimeFlowBackpackRefillRunner.isRunning()
				|| SlimeFlowState.outputCollectActive
				|| !SlimeFlowState.clickQueue.isEmpty();
		boolean loginReady = isLoginReady();

		drawIconButton(client, graphics, iconX(0), SIDE_Y, "\u2723", running, isMode(SlimeFlowState.OverlayMode.LIST) || isMode(SlimeFlowState.OverlayMode.EDIT), mouseX, mouseY);
		drawIconButton(client, graphics, iconX(1), SIDE_Y, "\u25A6", loginReady, isMode(SlimeFlowState.OverlayMode.LOGIN), mouseX, mouseY);
		drawIconButton(client, graphics, iconX(2), SIDE_Y, "\u25C6", SlimeFlowState.autoSellEnabled, isMode(SlimeFlowState.OverlayMode.AUTO_SELL), mouseX, mouseY);
		drawIconButton(client, graphics, iconX(3), SIDE_Y, "\u25CE", false, isMode(SlimeFlowState.OverlayMode.KEYS), mouseX, mouseY);
		drawIconButton(client, graphics, iconX(4), SIDE_Y, "\u2699", false, isMode(SlimeFlowState.OverlayMode.MORE), mouseX, mouseY);

		int statusY = SIDE_Y + SIDE_BUTTON_H + 2;
		graphics.fill(SIDE_X, statusY, SIDE_X + SIDE_W, statusY + 1, running ? 0xAA3DDC97 : 0x55E0393F);
	}

	private static void drawIconButton(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, String icon, boolean ready, boolean selected, int mouseX, int mouseY) {
		boolean hover = mouseX >= 0 && SlimeFlowUi.inside(mouseX, mouseY, x, y, ICON_SIZE, ICON_SIZE);
		int fill = selected ? 0xD0182620 : (hover ? 0xB0111916 : 0x90101516);
		int border = selected ? 0xFFE0393F : (hover ? 0xEEE0393F : 0x55E0393F);

		SlimeFlowUi.fill(graphics, x, y, ICON_SIZE, ICON_SIZE, fill);
		SlimeFlowUi.border(graphics, x, y, ICON_SIZE, ICON_SIZE, border);

		int textX = x + Math.max(1, (ICON_SIZE - client.font.width(icon)) / 2);
		graphics.text(client.font, icon, textX, y + 4, selected ? 0xFFFFFFFF : SlimeFlowTheme.RED, false);

		if (ready) {
			graphics.fill(x + ICON_SIZE - 4, y + 1, x + ICON_SIZE - 1, y + 4, 0xCC3DDC97);
		}
	}

	private static int iconX(int index) {
		return SIDE_X + index * (ICON_SIZE + ICON_GAP);
	}

	private static boolean isMode(SlimeFlowState.OverlayMode mode) {
		return macroOpen && SlimeFlowState.overlayMode == mode;
	}

	private static boolean isMoreSelected() {
		return isMode(SlimeFlowState.OverlayMode.MORE);
	}

	private static boolean isLoginReady() {
		for (SlimeFlowAutoLoginEntry entry : SlimeFlowState.autoLoginEntries) {
			if (entry == null || !entry.enabled) {
				continue;
			}

			String password = entry.password == null ? "" : entry.password.trim();
			String target = SlimeFlowAutoLogin.normalizeTargetServer(entry.targetServer);
			if (!password.isEmpty() || (entry.autoServer && !target.isEmpty())) {
				return true;
			}
		}

		return false;
	}

	private static int currentPanelWidth(int screenHeight) {
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			return SlimeFlowEditPanel.width();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			return SlimeFlowLoginPanel.width();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.MORE) {
			return SlimeFlowMorePanel.width();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			return SlimeFlowAutoSellPanel.width();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			return SlimeFlowAutoCommandPanel.width();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.KEYS) {
			return SlimeFlowKeybindPanel.width();
		}
		return SlimeFlowListPanel.width();
	}

	private static int currentPanelHeight(int screenHeight) {
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			return SlimeFlowEditPanel.height(screenHeight);
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			return SlimeFlowLoginPanel.height();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.MORE) {
			return SlimeFlowMorePanel.height();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			return SlimeFlowAutoSellPanel.height();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			return SlimeFlowAutoCommandPanel.height();
		}
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.KEYS) {
			return SlimeFlowKeybindPanel.height();
		}
		return SlimeFlowListPanel.height();
	}

	private static void renderCurrentPanel(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			SlimeFlowEditPanel.render(client, graphics, screenWidth, screenHeight);
		} else if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			SlimeFlowLoginPanel.render(client, graphics, screenWidth, screenHeight);
		} else if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.MORE) {
			SlimeFlowMorePanel.render(client, graphics, screenWidth, screenHeight);
		} else if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			SlimeFlowAutoSellPanel.render(client, graphics, screenWidth, screenHeight);
		} else if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			SlimeFlowAutoCommandPanel.render(client, graphics, screenWidth, screenHeight);
		} else if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.KEYS) {
			SlimeFlowKeybindPanel.render(client, graphics, screenWidth, screenHeight);
		} else {
			SlimeFlowListPanel.render(client, graphics, screenWidth, screenHeight);
		}
	}

	private static void drawTopBrand(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int mouseX, int mouseY) {
		String full = TOP_LABEL_LEFT + TOP_LABEL_MID + DISCORD_LABEL;
		int totalW = client.font.width(full);
		int x = Math.max(4, (screenWidth - totalW) / 2);
		int y = 1;
		boolean hoverDiscord = mouseX >= 0 && insideDiscord(mouseX, mouseY, screenWidth, 0, client);

		int cursor = x;
		graphics.text(client.font, TOP_LABEL_LEFT, cursor, y, 0xCCFF4A52, false);
		cursor += client.font.width(TOP_LABEL_LEFT);
		graphics.text(client.font, TOP_LABEL_MID, cursor, y, 0x99EAF8FF, false);
		cursor += client.font.width(TOP_LABEL_MID);
		graphics.text(client.font, DISCORD_LABEL, cursor, y, hoverDiscord ? 0xFFFFFFFF : 0xBFEAF8FF, false);
		if (hoverDiscord) {
			graphics.fill(cursor, y + 9, cursor + client.font.width(DISCORD_LABEL), y + 10, 0x88FFFFFF);
		}

		if (SlimeFlowState.discordLinkVisibleTicks > 0) {
			String copied = DISCORD_COPIED;
			int copiedX = Math.max(4, (screenWidth - client.font.width(copied)) / 2);
			graphics.text(client.font, copied, copiedX, y + 10, 0xBFEAF8FF, false);
			SlimeFlowState.discordLinkVisibleTicks--;
		}
	}

	private static boolean insideDiscord(double mouseX, double mouseY, int screenWidth, int screenHeight, Minecraft client) {
		String full = TOP_LABEL_LEFT + TOP_LABEL_MID + DISCORD_LABEL;
		int totalW = client.font.width(full);
		int x = Math.max(4, (screenWidth - totalW) / 2);
		int discordX = x + client.font.width(TOP_LABEL_LEFT + TOP_LABEL_MID);
		return SlimeFlowUi.inside(mouseX, mouseY, discordX - 1, 0, client.font.width(DISCORD_LABEL) + 2, 12);
	}

	static void showDiscordLink(Minecraft client) {
		SlimeFlowState.discordLinkVisibleTicks = 160;
		try {
			client.keyboardHandler.setClipboard(DISCORD_URL);
		} catch (Throwable ignored) {
		}
	}


	private static boolean shouldHideUiForPick() {
		return macroOpen
				&& SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE
				&& (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT
						|| SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL);
	}


	private static void renderPickTopBox(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
		int x = pickBoxX(screenWidth);
		int y = pickBoxY();
		SlimeFlowUi.fill(graphics, x, y, PICK_BAR_W, PICK_BAR_H, 0xD40C1418);
		SlimeFlowUi.border(graphics, x, y, PICK_BAR_W, PICK_BAR_H, 0xCCE0393F);
		graphics.fill(x + 1, y + 1, x + PICK_BAR_W - 1, y + 2, 0x33FFFFFF);

		String title;
		String[] lines;
		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			title = "Pick Auto Sell Item";
			lines = new String[] { "Item: " + SlimeFlowUi.cut(SlimeFlowState.autoSellItemName, 24), "Click item slot, then Save", "" };
		} else {
			title = SlimeFlowEditPanel.pickTitle();
			lines = SlimeFlowEditPanel.pickListLines();
		}

		graphics.text(client.font, title == null || title.isEmpty() ? "Pick" : title, x + 7, y + 5, SlimeFlowTheme.BLUE, false);
		int lineY = y + 18;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line != null && !line.isEmpty()) {
				graphics.text(client.font, SlimeFlowUi.cut(line, 35), x + 7, lineY, i == 0 ? SlimeFlowTheme.TEXT : SlimeFlowTheme.MUTED, false);
				lineY += 10;
			}
		}

		SlimeFlowUi.drawButton(client, graphics, x + PICK_BAR_W - 92, y + PICK_BAR_H - 18, 40, 13, "Save", SlimeFlowTheme.GREEN);
		SlimeFlowUi.drawButton(client, graphics, x + PICK_BAR_W - 48, y + PICK_BAR_H - 18, 42, 13, "Cancel", SlimeFlowTheme.RED);
	}

	private static int pickBoxX(int screenWidth) {
		return Math.max(4, (screenWidth - PICK_BAR_W) / 2);
	}

	private static int pickBoxY() {
		return 6;
	}

	private static boolean handlePickTopBoxClick(int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = pickBoxX(screenWidth);
		int y = pickBoxY();
		if (!SlimeFlowUi.inside(mouseX, mouseY, x, y, PICK_BAR_W, PICK_BAR_H)) {
			return false;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + PICK_BAR_W - 92, y + PICK_BAR_H - 18, 40, 13)) {
			if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
				SlimeFlowState.finishPick(true);
				SlimeFlowConfig.save();
			} else {
				SlimeFlowEditPanel.finishPick(true);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + PICK_BAR_W - 48, y + PICK_BAR_H - 18, 42, 13)) {
			if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
				SlimeFlowState.finishPick(false);
				SlimeFlowConfig.save();
			} else {
				SlimeFlowEditPanel.finishPick(false);
			}
			return true;
		}

		return true;
	}

	private static void renderSlotPickHighlights(Minecraft client, GuiGraphicsExtractor graphics) {
		if (!(client.screen instanceof AbstractContainerScreen<?> screen) || client.player == null || client.player.containerMenu == null) {
			return;
		}

		SlimeFlowState.PickMode mode = SlimeFlowState.pickMode;
		if (mode == SlimeFlowState.PickMode.NONE && !hasAnyDraftMarker()) {
			return;
		}

		int left = readScreenInt(screen, "leftPos", true);
		int top = readScreenInt(screen, "topPos", false);
		AbstractContainerMenu menu = client.player.containerMenu;

		for (int slotId = 0; slotId < menu.slots.size(); slotId++) {
			Slot slot = menu.slots.get(slotId);
			int sx = left + slot.x;
			int sy = top + slot.y;

			boolean marked = drawSavedRowMarker(client, graphics, slot, slotId, sx, sy);

			if (SlimeFlowState.draftFromSlot == slotId) {
				drawSlotMarker(client, graphics, sx, sy, 0x55FFC96B, 0xFFFFC96B, "F");
				marked = true;
			}

			if (SlimeFlowState.draftToSlot == slotId || SlimeFlowState.draftItemTargetSlot == slotId || SlimeFlowState.draftMultiTargetSlot == slotId) {
				drawSlotMarker(client, graphics, sx, sy, 0x553D6BFF, 0xFF91A7FF, "T");
				marked = true;
			}

			if (SlimeFlowState.draftClickSlot == slotId) {
				drawSlotMarker(client, graphics, sx, sy, 0x55B45CFF, 0xFFB45CFF, "C");
				marked = true;
			}

			if (SlimeFlowState.draftOutputSlot == slotId) {
				drawSlotMarker(client, graphics, sx, sy, 0x55FFB13D, 0xFFFFD17A, "O");
				marked = true;
			}

			String slotItemName = SlimeFlowItemFinder.getSlotItemName(slot);
			if (!slotItemName.isEmpty()) {
				if (!SlimeFlowState.draftItemName.isEmpty() && slotItemName.equalsIgnoreCase(SlimeFlowState.draftItemName)) {
					drawSlotMarker(client, graphics, sx, sy, 0x5510261A, 0xFFB8F3CB, "I");
					marked = true;
				}

				int multiIndex = indexOfIgnoreCase(SlimeFlowState.draftMultiItemNames, slotItemName);
				if (multiIndex >= 0) {
					drawSlotMarker(client, graphics, sx, sy, 0x5510261A, 0xFF81D1A0, String.valueOf(Math.min(9, multiIndex + 1)));
					marked = true;
				}

				SlimeFlowProfile profile = SlimeFlowState.editingProfile;
				if (profile != null && profile.backpackRefillEnabled && SlimeFlowBackpackRefillRunner.isAutoBackpackName(profile, slotItemName)) {
					drawSlotMarker(client, graphics, sx, sy, 0x44FF6B7A, 0xFFFF6B7A, "B");
					marked = true;
				}
			}

			if (!marked && shouldSoftHintMode(mode)) {
				// No hover border per-slot to keep inventory uncluttered.
			}
		}
	}

	private static boolean drawSavedRowMarker(Minecraft client, GuiGraphicsExtractor graphics, Slot slot, int slotId, int sx, int sy) {
		SlimeFlowProfile profile = SlimeFlowState.editingProfile;
		if (profile == null || profile.rows == null || profile.rows.isEmpty()) {
			return false;
		}

		String slotItemName = SlimeFlowItemFinder.getSlotItemName(slot);
		boolean marked = false;

		for (SlimeFlowProfile.Row row : profile.rows) {
			if (row == null) {
				continue;
			}

			SlimeFlowProfile.RowType type = row.type == null ? SlimeFlowProfile.RowType.MOVE : row.type;
			if (type == SlimeFlowProfile.RowType.MOVE) {
				if (row.fromSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x3339B9FF, 0xAA39B9FF, "F");
					marked = true;
				}
				if (row.toSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x3347FF7C, 0xAA7CFF9B, "T");
					marked = true;
				}
				continue;
			}

			if (type == SlimeFlowProfile.RowType.ITEM) {
				if (row.itemTargetSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x3347FF7C, 0xAA7CFF9B, "T");
					marked = true;
				}
				if (slotItemName != null && !slotItemName.isEmpty() && row.itemName != null && slotItemName.equalsIgnoreCase(row.itemName)) {
					drawSlotMarker(client, graphics, sx, sy, 0x3339B9FF, 0xAA39B9FF, "I");
					marked = true;
				}
				continue;
			}

			if (type == SlimeFlowProfile.RowType.CLICK) {
				if (row.clickSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x33B45CFF, 0xAAB45CFF, "C");
					marked = true;
				}
				continue;
			}

			if (type == SlimeFlowProfile.RowType.OUTPUT) {
				if (row.outputSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x33FFD36B, 0xAAFFD36B, "O");
					marked = true;
				}
				continue;
			}

			if (type == SlimeFlowProfile.RowType.MULTI) {
				if (row.multiTargetSlot == slotId) {
					drawSlotMarker(client, graphics, sx, sy, 0x3347FF7C, 0xAA7CFF9B, "T");
					marked = true;
				}
				if (slotItemName != null && !slotItemName.isEmpty() && indexOfIgnoreCase(row.multiItemNames, slotItemName) >= 0) {
					drawSlotMarker(client, graphics, sx, sy, 0x3339B9FF, 0xAA39B9FF, "I");
					marked = true;
				}
				continue;
			}

			if (type == SlimeFlowProfile.RowType.DROP) {
				if (slotItemName != null && !slotItemName.isEmpty() && row.dropItemName != null
						&& (slotItemName.equalsIgnoreCase(row.dropItemName)
						|| slotItemName.toLowerCase().contains(row.dropItemName.toLowerCase()))) {
					drawSlotMarker(client, graphics, sx, sy, 0x33FF6B6B, 0xAAFF6B6B, "D");
					marked = true;
				}
			}
		}

		return marked;
	}

	private static boolean hasAnyDraftMarker() {
		return (SlimeFlowState.editingProfile != null && SlimeFlowState.editingProfile.rows != null && !SlimeFlowState.editingProfile.rows.isEmpty())
				|| SlimeFlowState.draftFromSlot >= 0
				|| SlimeFlowState.draftToSlot >= 0
				|| SlimeFlowState.draftClickSlot >= 0
				|| SlimeFlowState.draftOutputSlot >= 0
				|| SlimeFlowState.draftItemTargetSlot >= 0
				|| (SlimeFlowState.draftItemName != null && !SlimeFlowState.draftItemName.isEmpty())
				|| SlimeFlowState.draftMultiTargetSlot >= 0
				|| !SlimeFlowState.draftMultiItemNames.isEmpty()
				|| (SlimeFlowState.editingProfile != null && SlimeFlowState.editingProfile.backpackRefillEnabled);
	}

	private static boolean shouldSoftHintMode(SlimeFlowState.PickMode mode) {
		return mode == SlimeFlowState.PickMode.MOVE_FROM
				|| mode == SlimeFlowState.PickMode.MOVE_TO
				|| mode == SlimeFlowState.PickMode.CLICK_SLOT
				|| mode == SlimeFlowState.PickMode.ITEM_NAME
				|| mode == SlimeFlowState.PickMode.ITEM_TO
				|| mode == SlimeFlowState.PickMode.MULTI_ITEM
				|| mode == SlimeFlowState.PickMode.MULTI_TO
				|| mode == SlimeFlowState.PickMode.OUTPUT_SLOT;
	}

	private static int indexOfIgnoreCase(List<String> values, String text) {
		if (values == null || text == null || text.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < values.size(); i++) {
			String value = values.get(i);
			if (value != null && value.equalsIgnoreCase(text)) {
				return i;
			}
		}

		return -1;
	}

	private static void drawSlotMarker(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int fill, int border, String label) {
		graphics.fill(x, y, x + 16, y + 16, fill);
		graphics.outline(x, y, 16, 16, border);
		graphics.fill(x, y, x + 16, y + 1, 0x33FFFFFF);

		if (label != null && !label.isEmpty()) {
			graphics.text(client.font, label, x + 2, y + 2, 0xFFFFFFFF, true);
		}
	}

	private static int readScreenInt(AbstractContainerScreen<?> screen, String name, boolean left) {
		try {
			Field field = getScreenField(name, left);
			return field == null ? 0 : field.getInt(screen);
		} catch (Throwable ignored) {
			return 0;
		}
	}

	private static Field getScreenField(String name, boolean left) {
		try {
			if (left) {
				if (!triedLeftPosField) {
					triedLeftPosField = true;
					leftPosField = AbstractContainerScreen.class.getDeclaredField(name);
					leftPosField.setAccessible(true);
				}
				return leftPosField;
			}

			if (!triedTopPosField) {
				triedTopPosField = true;
				topPosField = AbstractContainerScreen.class.getDeclaredField(name);
				topPosField.setAccessible(true);
			}
			return topPosField;
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static void placeMenuUnderButton(int screenWidth, int screenHeight, int panelW, int panelH, SlimeFlowState.OverlayMode mode) {
		// Panels spawn anchored under the HUD.
		SlimeFlowUi.stopDrag();
		SlimeFlowUi.centerPanel(screenWidth, screenHeight, panelW, panelH);
	}

	public static boolean mouseClicked(
			Minecraft client,
			int screenWidth,
			int screenHeight,
			double mouseX,
			double mouseY,
			int button
	) {
		if (hidden || client == null || client.player == null) {
			return false;
		}

		if (button == 1) {
			// Right click only adjusts Spd/APT on the Editor panel.
			if (!macroOpen || SlimeFlowState.overlayMode != SlimeFlowState.OverlayMode.EDIT) {
				return false;
			}

			int rw = realScreenWidth(client, screenWidth);
			int rh = realScreenHeight(client, screenHeight);
			int vw = (int) (rw / UI_SCALE);
			int vh = (int) (rh / UI_SCALE);
			double vMouseX = mouseX / UI_SCALE;
			double vMouseY = mouseY / UI_SCALE;

			return SlimeFlowEditPanel.rightClick(client, vw, vh, vMouseX, vMouseY);
		}

		if (button != 0) {
			return false;
		}

		screenWidth = realScreenWidth(client, screenWidth);
		screenHeight = realScreenHeight(client, screenHeight);

		// Convert to the overlay's virtual scale space (see render()).
		mouseX = mouseX / UI_SCALE;
		mouseY = mouseY / UI_SCALE;
		screenWidth = (int) (screenWidth / UI_SCALE);
		screenHeight = (int) (screenHeight / UI_SCALE);

		// Pick mode takes priority over the main menu.
		if (shouldHideUiForPick() && handlePickTopBoxClick(screenWidth, screenHeight, mouseX, mouseY)) {
			return true;
		}

		if (macroOpen
				&& SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL
				&& SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM) {
			return SlimeFlowAutoSell.pickHoveredItem(client);
		}

		if (macroOpen
				&& SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT
				&& SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE) {
			return SlimeFlowEditPanel.pickHoveredSlot(client);
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, iconX(0), SIDE_Y, ICON_SIZE, ICON_SIZE)) {
			openPanel(screenWidth, screenHeight, SlimeFlowState.OverlayMode.LIST);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, iconX(1), SIDE_Y, ICON_SIZE, ICON_SIZE)) {
			openPanel(screenWidth, screenHeight, SlimeFlowState.OverlayMode.LOGIN);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, iconX(2), SIDE_Y, ICON_SIZE, ICON_SIZE)) {
			openPanel(screenWidth, screenHeight, SlimeFlowState.OverlayMode.AUTO_SELL);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, iconX(3), SIDE_Y, ICON_SIZE, ICON_SIZE)) {
			openPanel(screenWidth, screenHeight, SlimeFlowState.OverlayMode.KEYS);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, iconX(4), SIDE_Y, ICON_SIZE, ICON_SIZE)) {
			openPanel(screenWidth, screenHeight, SlimeFlowState.OverlayMode.MORE);
			return true;
		}

		if (!macroOpen) {
			return false;
		}

		if (shouldHideUiForPick() && handlePickTopBoxClick(screenWidth, screenHeight, mouseX, mouseY)) {
			return true;
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL
				&& SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM) {
			return SlimeFlowAutoSell.pickHoveredItem(client);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT
				&& SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE) {
			return SlimeFlowEditPanel.pickHoveredSlot(client);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			return SlimeFlowEditPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			return SlimeFlowLoginPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.MORE) {
			return SlimeFlowMorePanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			return SlimeFlowAutoSellPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			return SlimeFlowAutoCommandPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.KEYS) {
			return SlimeFlowKeybindPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
		}

		return SlimeFlowListPanel.click(client, screenWidth, screenHeight, mouseX, mouseY);
	}

	static void openPanel(int screenWidth, int screenHeight, SlimeFlowState.OverlayMode mode) {
		// Same icon toggles its panel closed; HUD stays up either way.
		if (macroOpen && SlimeFlowState.overlayMode == mode) {
			closeMenuOnly();
			return;
		}

		macroOpen = true;
		SlimeFlowState.overlayMode = mode;
		SlimeFlowState.autoLoginEditField = 0;
		SlimeFlowState.autoLoginEditingPassword = false;
		SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
		if (mode != SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
			SlimeFlowState.autoCommandEditingIndex = -1;
			SlimeFlowState.autoCommandEditingEntry = null;
			SlimeFlowState.autoCommandEditingKeyIndex = -1;
		}
		if (mode != SlimeFlowState.OverlayMode.EDIT) {
			SlimeFlowState.finishPick(true);
		}
		placeMenuUnderButton(screenWidth, screenHeight, currentPanelWidth(screenHeight), currentPanelHeight(screenHeight), mode);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == SlimeFlowState.keyOpenUi && SlimeFlowState.keybindEditingAction == SlimeFlowState.KEYBIND_NONE) {
			SlimeFlowInput.markOpenUiKeyHandled();
			toggle(Minecraft.getInstance());
			return true;
		}

		if (hidden) {
			return false;
		}

		if (!macroOpen) {
			return false;
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			return SlimeFlowLoginPanel.keyPressed(keyCode, scanCode, modifiers);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			return SlimeFlowAutoSellPanel.keyPressed(keyCode, scanCode, modifiers);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			return SlimeFlowAutoCommandPanel.keyPressed(keyCode, scanCode, modifiers);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.KEYS) {
			return SlimeFlowKeybindPanel.keyPressed(keyCode, scanCode, modifiers);
		}

		return false;
	}

	public static boolean charTyped(char chr, int modifiers) {
		if (hidden || !macroOpen) {
			return false;
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.LOGIN) {
			return SlimeFlowLoginPanel.charTyped(chr, modifiers);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_SELL) {
			return SlimeFlowAutoSellPanel.charTyped(chr, modifiers);
		}

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.AUTO_COMMAND) {
			return SlimeFlowAutoCommandPanel.charTyped(chr, modifiers);
		}

		return false;
	}

	public static void toggle(Minecraft client) {
		if (hidden) {
			show(client);
			return;
		}

		// State left visible with no active screen - reopen on next F12.
		if (client != null && client.screen == null) {
			show(client);
			return;
		}

		hide();
	}

	public static void show(Minecraft client) {
		// No SlimeFlow on the title screen - only once in a world/server.
		if (client == null || client.player == null) {
			hidden = true;
			macroOpen = false;
			SlimeFlowUi.stopDrag();
			return;
		}

		hidden = false;
		hudExpanded = true;

		if (client.screen == null) {
			client.setScreen(new SlimeFlowScreen());
		}
	}

	static void backToPreviousPanel() {
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);

		if (SlimeFlowState.overlayMode == SlimeFlowState.OverlayMode.EDIT) {
			SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LIST;
			SlimeFlowState.editingProfile = null;
			SlimeFlowState.editingProfileIndex = -1;
			SlimeFlowState.resetDraft();
			return;
		}

		closeMenuOnly();
	}

	static void showMoreMenu() {
		macroOpen = true;
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.MORE;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
	}

	static void showAutomationMenu() {
		macroOpen = true;
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LIST;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoLoginEditField = 0;
		SlimeFlowState.autoLoginEditingPassword = false;
	}

	static void showAccountMenu() {
		macroOpen = true;
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LOGIN;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoLoginEditField = 0;
		SlimeFlowState.autoLoginEditingPassword = false;
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
	}

	static void showAutoCommandMenu() {
		macroOpen = true;
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.AUTO_COMMAND;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.autoCommandEditingIndex = -1;
		SlimeFlowState.autoCommandEditingEntry = null;
	}

	static void showUtilityMenu() {
		macroOpen = true;
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.KEYS;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;
	}

	static void closeMenuOnly() {
		SlimeFlowUi.stopDrag();
		SlimeFlowState.finishPick(true);
		SlimeFlowState.autoLoginEditField = 0;
		SlimeFlowState.autoLoginEditingPassword = false;
		SlimeFlowState.autoLoginEditingIndex = -1;
		SlimeFlowState.autoLoginEditingEntry = null;
		SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.autoCommandEditingIndex = -1;
		SlimeFlowState.autoCommandEditingEntry = null;
		SlimeFlowState.autoCommandEditingKeyIndex = -1;
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;

		Minecraft client = Minecraft.getInstance();

		// Over a GUI, X/Back just closes the active panel; HUD stays visible.
		if (client != null && client.screen != null && !(client.screen instanceof SlimeFlowScreen)) {
			macroOpen = false;
			hidden = false;
			return;
		}

		// If SlimeFlow was opened as its own screen in-world, X fully closes the UI.
		hide();
	}

	static void hide() {
		hidden = true;
		macroOpen = false;
		SlimeFlowUi.stopDrag();
		SlimeFlowState.autoLoginEditField = 0;
		SlimeFlowState.autoLoginEditingPassword = false;
		SlimeFlowState.autoLoginEditingIndex = -1;
		SlimeFlowState.autoLoginEditingEntry = null;
		SlimeFlowState.autoSellEditField = SlimeFlowState.AUTO_SELL_FIELD_NONE;
		SlimeFlowState.autoCommandEditField = SlimeFlowState.AUTO_COMMAND_FIELD_NONE;
		SlimeFlowState.autoCommandEditingIndex = -1;
		SlimeFlowState.autoCommandEditingEntry = null;
		SlimeFlowState.autoCommandEditingKeyIndex = -1;
		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.AUTO_SELL_ITEM) {
			SlimeFlowState.finishPick(true);
		}
		SlimeFlowState.keybindEditingAction = SlimeFlowState.KEYBIND_NONE;

		Minecraft client = Minecraft.getInstance();

		if (client != null && client.screen instanceof SlimeFlowScreen) {
			client.setScreen(null);
		}
	}

	private static void stopAll(Minecraft client) {
		SlimeFlowControl.stopAll(client);
	}
}
