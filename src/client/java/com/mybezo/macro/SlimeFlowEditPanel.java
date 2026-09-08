package com.mybezo.macro;

import com.mybezo.macro.mixin.AbstractContainerScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class SlimeFlowEditPanel {
	private static final int MAX_ROWS = 20;
	private static final int EDIT_W = 252;
	private static final int HEADER_H = 20;
	private static final int INFO_Y = 24;
	private static final int INFO_H = 66;
	private static final int DRAFT_Y = 96;
	private static final int DRAFT_H = 82;
	private static final int ROWS_TITLE_Y = 184;
	private static final int ROW_START_Y = 198;
	private static final int ROW_H = 20;

	private static int rowScroll = 0;

	private SlimeFlowEditPanel() {
	}

	public static int width() {
		return EDIT_W;
	}

	public static int height(int screenHeight) {
		return editHeight(screenHeight);
	}

	public static void resetScroll() {
		rowScroll = 0;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		SlimeFlowProfile profile = SlimeFlowState.editingProfile;

		if (profile == null) {
			SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LIST;
			return;
		}

		int h = editHeight(screenHeight);
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, EDIT_W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, EDIT_W, h);

		clampRowScroll(profile, screenHeight);

		SlimeFlowUi.drawPanel(graphics, x, y, EDIT_W, h, SlimeFlowTheme.BORDER);

		renderHeader(client, graphics, x, y);
		renderInfo(client, graphics, profile, x, y);
		renderDraft(client, graphics, x, y);
		renderRows(client, graphics, profile, x, y, screenHeight);
		renderFooter(client, graphics, x, y, h);
	}

	private static void renderHeader(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		SlimeFlowUi.text(client, graphics, "Editor", x + 6, y + 6, SlimeFlowTheme.TEXT);

		SlimeFlowUi.drawButton(client, graphics, x + EDIT_W - 50, y + 4, 14, 12, "^", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + EDIT_W - 34, y + 4, 14, 12, "v", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + EDIT_W - 18, y + 4, 14, 12, "x", SlimeFlowTheme.RED);
	}

	private static void renderInfo(Minecraft client, GuiGraphicsExtractor graphics, SlimeFlowProfile profile, int x, int y) {
		int infoY = y + INFO_Y;

		SlimeFlowUi.fill(graphics, x + 4, infoY, EDIT_W - 8, INFO_H, SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 4, infoY, EDIT_W - 8, INFO_H, SlimeFlowTheme.BORDER_SOFT);
		SlimeFlowUi.cutCorners(graphics, x + 4, infoY, EDIT_W - 8, INFO_H, SlimeFlowTheme.WINDOW);

		SlimeFlowUi.iconInfo(graphics, x + 8, infoY + 5, SlimeFlowTheme.BLUE);
		SlimeFlowUi.text(client, graphics, "Name: " + SlimeFlowUi.cut(profile.name, 13), x + 16, infoY + 5, SlimeFlowTheme.TEXT);
		SlimeFlowUi.drawButtonRounded(client, graphics, x + EDIT_W - 34, infoY + 3, 26, 12, "NM", SlimeFlowTheme.MUTED, SlimeFlowTheme.BG_SOFT);

		SlimeFlowUi.text(client, graphics, "Gui: " + SlimeFlowUi.cut(profile.guiTitle, 24), x + 8, infoY + 17, SlimeFlowTheme.MUTED);

		// Identity group ends here.
		graphics.fill(x + 6, infoY + 26, x + EDIT_W - 6, infoY + 27, SlimeFlowTheme.HAIRLINE);

		SlimeFlowUi.drawToggleChip(graphics, x + 8, infoY + 30, profile.autoRun);
		SlimeFlowUi.text(client, graphics, "Auto", x + 22, infoY + 29, SlimeFlowTheme.MUTED);

		SlimeFlowUi.border(graphics, x + 51, infoY + 28, 29, 11, 0x66FFD36B);
		SlimeFlowUi.text(client, graphics, "Spd:" + profile.speed, x + 54, infoY + 31, SlimeFlowTheme.YELLOW);

		SlimeFlowUi.border(graphics, x + 84, infoY + 28, 33, 11, 0x66FFD36B);
		SlimeFlowUi.text(client, graphics, "APT:" + SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick), x + 87, infoY + 31, SlimeFlowTheme.YELLOW);

		SlimeFlowUi.text(client, graphics, "Out:" + (profile.outputCollectLoop ? "Loop" : "Once"), x + 122, infoY + 29, SlimeFlowTheme.MUTED);

		SlimeFlowUi.drawToggleChip(graphics, x + 8, infoY + 42, profile.backpackRefillEnabled);
		SlimeFlowUi.text(client, graphics, "Refill", x + 22, infoY + 41, SlimeFlowTheme.MUTED);
		SlimeFlowUi.text(client, graphics, "Bag:" + SlimeFlowUi.cut(SlimeFlowBackpackRefillRunner.refillKeywordText(profile), 12), x + 60, infoY + 41, SlimeFlowTheme.MUTED);

		// Settings group ends here.
		graphics.fill(x + 6, infoY + 50, x + EDIT_W - 6, infoY + 51, SlimeFlowTheme.HAIRLINE);

		int bx = x + 8;
		int by = infoY + 53;
		SlimeFlowUi.drawButton(client, graphics, bx, by, 23, 12, "Key", SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, bx + 27, by, 23, 12, "Def", SlimeFlowTheme.MUTED);
	}

	private static final String[] TYPE_LABELS = {"Move", "Item", "Click", "Multi", "Output"};
	private static final SlimeFlowState.DraftType[] TYPE_VALUES = {
			SlimeFlowState.DraftType.MOVE,
			SlimeFlowState.DraftType.ITEM,
			SlimeFlowState.DraftType.CLICK,
			SlimeFlowState.DraftType.MULTI,
			SlimeFlowState.DraftType.OUTPUT
	};
	private static final int TYPE_SELECT_X = 52;
	private static final int TYPE_SELECT_W = 78;
	private static final int TYPE_SELECT_H = 13;
	private static final int TYPE_OPTION_H = 12;

	private static void renderDraft(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		int draftY = y + DRAFT_Y;

		SlimeFlowUi.fill(graphics, x + 4, draftY, EDIT_W - 8, DRAFT_H, SlimeFlowTheme.BG_SOFT);
		SlimeFlowUi.border(graphics, x + 4, draftY, EDIT_W - 8, DRAFT_H, SlimeFlowTheme.BORDER_SOFT);
		SlimeFlowUi.cutCorners(graphics, x + 4, draftY, EDIT_W - 8, DRAFT_H, SlimeFlowTheme.WINDOW);

		SlimeFlowUi.iconAction(graphics, x + 8, draftY + 5, SlimeFlowTheme.BLUE);
		graphics.text(client.font, "Mode", x + 16, draftY + 5, SlimeFlowTheme.BLUE, false);
		renderTypeSelectBox(client, graphics, x, draftY);
		renderDraftBody(client, graphics, x, y, draftY);

		// Draw last so it overlays the draft content below it.
		if (SlimeFlowState.draftTypeMenuOpen) {
			renderTypeDropdown(client, graphics, x, draftY);
		}
	}

	private static void renderDraftBody(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int draftY) {
		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.MOVE) {
			graphics.text(client.font, slotShort("F", SlimeFlowState.draftFromSlot), x + 8, draftY + 27, slotColor(SlimeFlowState.draftFromSlot), false);
			graphics.text(client.font, slotShort("T", SlimeFlowState.draftToSlot), x + 66, draftY + 27, slotColor(SlimeFlowState.draftToSlot), false);
			graphics.text(client.font, "A" + SlimeFlowState.draftAmount, x + 124, draftY + 27, SlimeFlowTheme.TEXT, false);
			graphics.text(client.font, "Pick: from -> to", x + 8, draftY + 39, SlimeFlowTheme.MUTED, false);

			SlimeFlowUi.drawButtonRounded(client, graphics, x + 8, draftY + 60, 31, 13, pickMoveActive() ? "P*" : "P", SlimeFlowTheme.BLUE, SlimeFlowTheme.BG_SOFT);
			SlimeFlowUi.drawButton(client, graphics, x + 43, draftY + 60, 14, 13, "-", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 60, draftY + 60, 14, 13, "+", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButtonRounded(client, graphics, x + 80, draftY + 60, 42, 13, "Add", canAddMove() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED, SlimeFlowTheme.BG_SOFT);
			SlimeFlowUi.drawButtonRounded(client, graphics, x + 126, draftY + 60, 26, 13, "+32", SlimeFlowTheme.ACCENT, SlimeFlowTheme.BG_SOFT);
			return;
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.ITEM) {
			graphics.text(client.font, itemShort(SlimeFlowState.draftItemName), x + 8, draftY + 27, itemColor(SlimeFlowState.draftItemName), false);
			graphics.text(client.font, slotShort("T", SlimeFlowState.draftItemTargetSlot), x + 120, draftY + 27, slotColor(SlimeFlowState.draftItemTargetSlot), false);
			graphics.text(client.font, "A" + SlimeFlowState.draftItemAmount, x + 174, draftY + 27, SlimeFlowTheme.TEXT, false);
			graphics.text(client.font, "Pick once: item -> slot", x + 8, draftY + 39, SlimeFlowTheme.MUTED, false);

			SlimeFlowUi.drawButton(client, graphics, x + 8, draftY + 60, 31, 13, pickItemActive() ? "P*" : "P", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, x + 43, draftY + 60, 14, 13, "-", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 60, draftY + 60, 14, 13, "+", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 80, draftY + 60, 42, 13, "Add", canAddItem() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 126, draftY + 60, 26, 13, "+32", SlimeFlowTheme.ACCENT);
			return;
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.CLICK) {
			graphics.text(client.font, slotShort("S", SlimeFlowState.draftClickSlot), x + 8, draftY + 27, slotColor(SlimeFlowState.draftClickSlot), false);
			graphics.text(client.font, buttonName(SlimeFlowState.draftClickButton), x + 70, draftY + 27, SlimeFlowTheme.TEXT, false);
			graphics.text(client.font, "x" + SlimeFlowState.draftClickTimes, x + 104, draftY + 27, SlimeFlowTheme.TEXT, false);
			graphics.text(client.font, "Pick slot | B = button", x + 8, draftY + 39, SlimeFlowTheme.MUTED, false);

			SlimeFlowUi.drawButton(client, graphics, x + 8, draftY + 60, 31, 13, SlimeFlowState.pickMode == SlimeFlowState.PickMode.CLICK_SLOT ? "P*" : "P", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, x + 43, draftY + 60, 18, 13, "B", SlimeFlowTheme.YELLOW);
			SlimeFlowUi.drawButton(client, graphics, x + 65, draftY + 60, 14, 13, "-", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 82, draftY + 60, 14, 13, "+", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 102, draftY + 60, 42, 13, "Add", canAddClick() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED);
			return;
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.OUTPUT) {
			SlimeFlowProfile profile = SlimeFlowState.editingProfile;
			boolean loop = profile == null || profile.outputCollectLoop;

			graphics.text(client.font, slotShort("O", SlimeFlowState.draftOutputSlot), x + 8, draftY + 27, slotColor(SlimeFlowState.draftOutputSlot), false);
			graphics.text(client.font, loop ? "Auto collect + stack" : "Take once on run", x + 8, draftY + 39, SlimeFlowTheme.MUTED, false);

			SlimeFlowUi.drawButton(client, graphics, x + 8, draftY + 60, 31, 13, SlimeFlowState.pickMode == SlimeFlowState.PickMode.OUTPUT_SLOT ? "P*" : "P", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, x + 43, draftY + 60, 42, 13, "Add", canAddOutput() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 89, draftY + 60, 42, 13, loop ? "Loop" : "Once", loop ? SlimeFlowTheme.GREEN : SlimeFlowTheme.YELLOW);
			return;
		}

		graphics.text(client.font, "I:" + SlimeFlowState.draftMultiItemNames.size(), x + 8, draftY + 25, SlimeFlowState.draftMultiItemNames.isEmpty() ? SlimeFlowTheme.MUTED : SlimeFlowTheme.GREEN, false);
		graphics.text(client.font, slotShort("T", SlimeFlowState.draftMultiTargetSlot), x + 42, draftY + 25, slotColor(SlimeFlowState.draftMultiTargetSlot), false);
		graphics.text(client.font, "A" + SlimeFlowState.draftMultiAmount, x + 96, draftY + 25, SlimeFlowTheme.TEXT, false);
		renderMultiItemList(client, graphics, x + 8, draftY + 39);

		SlimeFlowUi.drawButton(client, graphics, x + 8, draftY + 60, 25, 13, SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM ? "IT*" : "IT", SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM ? SlimeFlowTheme.GREEN : SlimeFlowTheme.BLUE);
		SlimeFlowUi.drawButton(client, graphics, x + 36, draftY + 60, 24, 13, SlimeFlowState.draftMultiTargetSlot >= 0 ? "TO*" : "TO", SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_TO ? SlimeFlowTheme.GREEN : SlimeFlowTheme.YELLOW);
		SlimeFlowUi.drawButton(client, graphics, x + 63, draftY + 60, 14, 13, "-", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 80, draftY + 60, 14, 13, "+", SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 100, draftY + 60, 38, 13, "Add", canAddMulti() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED);
		SlimeFlowUi.drawButton(client, graphics, x + 142, draftY + 60, 27, 13, "Del", SlimeFlowTheme.ORANGE);
		SlimeFlowUi.drawButton(client, graphics, x + 173, draftY + 60, 25, 13, "Clr", SlimeFlowTheme.RED);
		SlimeFlowUi.drawButton(client, graphics, x + 202, draftY + 60, 26, 13, "+32", SlimeFlowTheme.ACCENT);
	}

	private static int typeIndex(SlimeFlowState.DraftType type) {
		for (int i = 0; i < TYPE_VALUES.length; i++) {
			if (TYPE_VALUES[i] == type) {
				return i;
			}
		}
		return 0;
	}

	private static void renderTypeSelectBox(Minecraft client, GuiGraphicsExtractor graphics, int x, int draftY) {
		String label = TYPE_LABELS[typeIndex(SlimeFlowState.draftType)] + (SlimeFlowState.draftTypeMenuOpen ? " \u25B4" : " \u25BE");
		SlimeFlowUi.drawButtonRounded(client, graphics, x + TYPE_SELECT_X, draftY + 3, TYPE_SELECT_W, TYPE_SELECT_H, label, SlimeFlowTheme.ACCENT, SlimeFlowTheme.BG_SOFT);
	}

	private static void renderTypeDropdown(Minecraft client, GuiGraphicsExtractor graphics, int x, int draftY) {
		int boxX = x + TYPE_SELECT_X;
		int listY = draftY + 3 + TYPE_SELECT_H + 1;
		int listH = TYPE_LABELS.length * TYPE_OPTION_H;

		SlimeFlowUi.fill(graphics, boxX, listY, TYPE_SELECT_W, listH, SlimeFlowTheme.WINDOW_STRONG);
		SlimeFlowUi.border(graphics, boxX, listY, TYPE_SELECT_W, listH, SlimeFlowTheme.ACCENT);

		for (int i = 0; i < TYPE_LABELS.length; i++) {
			int optionY = listY + i * TYPE_OPTION_H;
			boolean selected = TYPE_VALUES[i] == SlimeFlowState.draftType;

			if (selected) {
				SlimeFlowUi.fill(graphics, boxX + 1, optionY, TYPE_SELECT_W - 2, TYPE_OPTION_H, SlimeFlowTheme.ROW_HOVER);
			}

			graphics.text(client.font, TYPE_LABELS[i], boxX + 4, optionY + 2, selected ? SlimeFlowTheme.ACCENT : SlimeFlowTheme.TEXT, false);
		}
	}

	private static void renderRows(Minecraft client, GuiGraphicsExtractor graphics, SlimeFlowProfile profile, int x, int y, int screenHeight) {
		int rowsTitleY = y + ROWS_TITLE_Y;
		int visibleRows = editVisibleRows(screenHeight);
		int startY = y + ROW_START_Y;

		String title = "Actions " + profile.rows.size() + "/" + MAX_ROWS;
		if (profile.rows.size() > 0 && visibleRows > 0) {
			int first = Math.min(profile.rows.size(), rowScroll + 1);
			int last = Math.min(profile.rows.size(), rowScroll + visibleRows);
			title += " " + first + "-" + last;
		}

		SlimeFlowUi.iconList(graphics, x + 6, rowsTitleY + 1, SlimeFlowTheme.BLUE);
		graphics.text(client.font, title, x + 14, rowsTitleY, SlimeFlowTheme.BLUE, false);

		if (visibleRows <= 0) {
			graphics.text(client.font, "List hidden while picking", x + 8, startY + 4, SlimeFlowTheme.MUTED, false);
			return;
		}

		for (int visibleIndex = 0; visibleIndex < visibleRows; visibleIndex++) {
			int rowIndex = rowScroll + visibleIndex;

			if (rowIndex >= profile.rows.size()) {
				break;
			}

			SlimeFlowProfile.Row row = profile.rows.get(rowIndex);
			int rowY = startY + visibleIndex * ROW_H;

			SlimeFlowUi.fill(graphics, x + 4, rowY, EDIT_W - 8, ROW_H - 2, visibleIndex % 2 == 0 ? SlimeFlowTheme.CARD : SlimeFlowTheme.CARD_DARK);

			graphics.text(client.font, (rowIndex + 1) + ". " + SlimeFlowUi.cut(rowText(row), 23), x + 8, rowY + 5, SlimeFlowTheme.TEXT, false);

			SlimeFlowUi.drawButton(client, graphics, x + 166, rowY + 3, 13, 12, "^", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 181, rowY + 3, 13, 12, "v", SlimeFlowTheme.MUTED);
			SlimeFlowUi.drawButton(client, graphics, x + 197, rowY + 3, 21, 12, "Cp", SlimeFlowTheme.BLUE);
			SlimeFlowUi.drawButton(client, graphics, x + 221, rowY + 3, 18, 12, "x", SlimeFlowTheme.RED);
		}
	}

	private static void renderFooter(Minecraft client, GuiGraphicsExtractor graphics, int x, int y, int h) {
		int bottomY = y + h - 22;

		if (SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE) {
			graphics.text(client.font, pickText(), x + 6, bottomY - 13, SlimeFlowTheme.BLUE, false);
			SlimeFlowUi.drawButton(client, graphics, x + EDIT_W - 42, bottomY - 15, 34, 12, "Stp", SlimeFlowTheme.RED);
		}

		SlimeFlowUi.drawButtonRounded(client, graphics, x + 4, bottomY, 118, 16, "Save", SlimeFlowTheme.GREEN, SlimeFlowTheme.WINDOW);
		SlimeFlowUi.iconSave(graphics, x + 10, bottomY + 5, SlimeFlowTheme.TEXT);
		SlimeFlowUi.drawButtonRounded(client, graphics, x + 130, bottomY, 118, 16, "Back", SlimeFlowTheme.RED, SlimeFlowTheme.WINDOW);
		SlimeFlowUi.iconBack(graphics, x + 136, bottomY + 5, SlimeFlowTheme.TEXT);
	}

	/** Right-click on Spd/APT decrements them. */
	public static boolean rightClick(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		SlimeFlowProfile profile = SlimeFlowState.editingProfile;
		if (profile == null) {
			return false;
		}

		int h = editHeight(screenHeight);
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, EDIT_W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, EDIT_W, h);
		int infoY = y + INFO_Y;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 51, infoY + 28, 29, 11)) {
			profile.speed = Math.max(0, profile.speed - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 84, infoY + 28, 33, 11)) {
			profile.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick - aptStep(profile.actionsPerTick));
			return true;
		}

		return false;
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		SlimeFlowProfile profile = SlimeFlowState.editingProfile;

		int h = editHeight(screenHeight);
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, EDIT_W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, EDIT_W, h);

		if (profile == null) {
			SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LIST;
			return true;
		}

		int bottomY = y + h - 22;

		if (SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE
				&& SlimeFlowUi.inside(mouseX, mouseY, x + EDIT_W - 42, bottomY - 15, 34, 12)) {
			SlimeFlowState.finishPick(true);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + EDIT_W - 50, y + 4, 14, 12)) {
			rowScroll = Math.max(0, rowScroll - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + EDIT_W - 34, y + 4, 14, 12)) {
			rowScroll++;
			clampRowScroll(profile, screenHeight);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + EDIT_W - 18, y + 4, 14, 12)) {
			backToList();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, EDIT_W, HEADER_H)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		if (clickInfo(client, profile, x, y, mouseX, mouseY)) {
			return true;
		}

		if (clickDraft(profile, x, y, mouseX, mouseY)) {
			return true;
		}

		if (clickRows(profile, x, y, screenHeight, mouseX, mouseY)) {
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 4, bottomY, 118, 16)) {
			saveProfile(client, profile);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 130, bottomY, 118, 16)) {
			backToList();
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, EDIT_W, h);
	}


	private static void stopRuntimeFromSp(Minecraft client) {
		SlimeFlowStackRunner.stop(client);
		SlimeFlowBackpackRefillRunner.stopFromButton(client);
		SlimeFlowState.stopMacroRuntimeState();
	}

	private static boolean clickInfo(Minecraft client, SlimeFlowProfile profile, int x, int y, double mouseX, double mouseY) {
		int infoY = y + INFO_Y;
		int bx = x + 8;
		int by = infoY + 53;

		if (SlimeFlowUi.inside(mouseX, mouseY, x + EDIT_W - 34, infoY + 3, 26, 12)) {
			renameProfile(client, profile);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, infoY + 28, 42, 12)) {
			profile.autoRun = !profile.autoRun;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, infoY + 40, 42, 12)) {
			profile.backpackRefillEnabled = !profile.backpackRefillEnabled;
			profile.backpackSlots.clear();
			profile.backpackRefillItemName = "";
			SlimeFlowState.backpackKnownEmptySlots.clear();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 51, infoY + 28, 29, 11)) {
			profile.speed = Math.min(20, profile.speed + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 84, infoY + 28, 33, 11)) {
			profile.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick + aptStep(profile.actionsPerTick));
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, bx, by, 23, 12)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD) {
				SlimeFlowState.finishPick(true);
			} else {
				SlimeFlowState.beginPick(SlimeFlowState.PickMode.REFILL_KEYWORD);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, bx + 27, by, 23, 12)) {
			SlimeFlowBackpackRefillRunner.resetRefillKeywords(profile);
			SlimeFlowState.backpackKnownEmptySlots.clear();
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD) {
				SlimeFlowState.finishPick(true);
			}
			return true;
		}

		return false;
	}

	private static boolean clickDraft(SlimeFlowProfile profile, int x, int y, double mouseX, double mouseY) {
		int draftY = y + DRAFT_Y;

		if (SlimeFlowState.draftTypeMenuOpen) {
			return clickTypeDropdown(x, draftY, mouseX, mouseY);
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + TYPE_SELECT_X, draftY + 3, TYPE_SELECT_W, TYPE_SELECT_H)) {
			SlimeFlowState.draftTypeMenuOpen = true;
			return true;
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.MOVE) {
			return clickMoveDraft(profile, x, draftY, mouseX, mouseY);
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.ITEM) {
			return clickItemDraft(profile, x, draftY, mouseX, mouseY);
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.CLICK) {
			return clickClickDraft(profile, x, draftY, mouseX, mouseY);
		}

		if (SlimeFlowState.draftType == SlimeFlowState.DraftType.OUTPUT) {
			return clickOutputDraft(profile, x, draftY, mouseX, mouseY);
		}

		return clickMultiDraft(profile, x, draftY, mouseX, mouseY);
	}

	/** Dropdown eats every click while open. */
	private static boolean clickTypeDropdown(int x, int draftY, double mouseX, double mouseY) {
		int boxX = x + TYPE_SELECT_X;
		int listY = draftY + 3 + TYPE_SELECT_H + 1;

		for (int i = 0; i < TYPE_LABELS.length; i++) {
			int optionY = listY + i * TYPE_OPTION_H;
			if (SlimeFlowUi.inside(mouseX, mouseY, boxX, optionY, TYPE_SELECT_W, TYPE_OPTION_H)) {
				SlimeFlowState.draftType = TYPE_VALUES[i];
				SlimeFlowState.draftTypeMenuOpen = false;
				SlimeFlowState.finishPick(true);
				return true;
			}
		}

		SlimeFlowState.draftTypeMenuOpen = false;
		return true;
	}

	private static boolean clickMoveDraft(SlimeFlowProfile profile, int x, int draftY, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, draftY + 60, 31, 13)) {
			if (pickMoveActive()) {
				SlimeFlowState.finishPick(true);
				return true;
			}

			SlimeFlowState.beginPick(SlimeFlowState.PickMode.MOVE_FROM);
			SlimeFlowState.draftFromSlot = -1;
			SlimeFlowState.draftToSlot = -1;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 43, draftY + 60, 14, 13)) {
			SlimeFlowState.draftAmount = Math.max(1, SlimeFlowState.draftAmount - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 60, draftY + 60, 14, 13)) {
			SlimeFlowState.draftAmount = Math.min(64, SlimeFlowState.draftAmount + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 126, draftY + 60, 26, 13)) {
			SlimeFlowState.draftAmount = Math.min(64, SlimeFlowState.draftAmount + 32);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 80, draftY + 60, 42, 13)) {
			if (profile.rows.size() >= MAX_ROWS || !canAddMove()) {
				return true;
			}

			profile.rows.add(SlimeFlowProfile.Row.move(
					SlimeFlowState.draftFromSlot,
					SlimeFlowState.draftToSlot,
					SlimeFlowState.draftAmount
			));

			SlimeFlowState.draftFromSlot = -1;
			SlimeFlowState.draftToSlot = -1;
			SlimeFlowState.draftAmount = 1;
			scrollToLast(profile);
			return true;
		}

		return false;
	}

	private static boolean clickClickDraft(SlimeFlowProfile profile, int x, int draftY, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, draftY + 60, 31, 13)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.CLICK_SLOT) {
				SlimeFlowState.finishPick(true);
				return true;
			}

			SlimeFlowState.beginPick(SlimeFlowState.PickMode.CLICK_SLOT);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 43, draftY + 60, 18, 13)) {
			SlimeFlowState.draftClickButton = SlimeFlowState.draftClickButton == 0 ? 1 : 0;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 65, draftY + 60, 14, 13)) {
			SlimeFlowState.draftClickTimes = Math.max(1, SlimeFlowState.draftClickTimes - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 82, draftY + 60, 14, 13)) {
			SlimeFlowState.draftClickTimes = Math.min(64, SlimeFlowState.draftClickTimes + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 102, draftY + 60, 42, 13)) {
			if (profile.rows.size() >= MAX_ROWS || !canAddClick()) {
				return true;
			}

			profile.rows.add(SlimeFlowProfile.Row.click(
					SlimeFlowState.draftClickSlot,
					SlimeFlowState.draftClickButton,
					SlimeFlowState.draftClickTimes
			));

			SlimeFlowState.draftClickSlot = -1;
			SlimeFlowState.draftClickButton = 0;
			SlimeFlowState.draftClickTimes = 1;
			scrollToLast(profile);
			return true;
		}

		return false;
	}

	private static boolean clickItemDraft(SlimeFlowProfile profile, int x, int draftY, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, draftY + 60, 31, 13)) {
			if (pickItemActive()) {
				SlimeFlowState.finishPick(true);
				return true;
			}

			SlimeFlowState.beginPick(SlimeFlowState.PickMode.ITEM_NAME);
			SlimeFlowState.draftItemName = "";
			SlimeFlowState.draftItemTargetSlot = -1;
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 43, draftY + 60, 14, 13)) {
			SlimeFlowState.draftItemAmount = Math.max(1, SlimeFlowState.draftItemAmount - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 60, draftY + 60, 14, 13)) {
			SlimeFlowState.draftItemAmount = Math.min(64, SlimeFlowState.draftItemAmount + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 126, draftY + 60, 26, 13)) {
			SlimeFlowState.draftItemAmount = Math.min(64, SlimeFlowState.draftItemAmount + 32);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 80, draftY + 60, 42, 13)) {
			if (profile.rows.size() >= MAX_ROWS || !canAddItem()) {
				return true;
			}

			profile.rows.add(SlimeFlowProfile.Row.item(
					SlimeFlowState.draftItemName,
					SlimeFlowState.draftItemTargetSlot,
					SlimeFlowState.draftItemAmount
			));

			SlimeFlowState.draftItemName = "";
			SlimeFlowState.draftItemTargetSlot = -1;
			SlimeFlowState.draftItemAmount = 1;
			scrollToLast(profile);
			return true;
		}

		return false;
	}

	private static boolean clickOutputDraft(SlimeFlowProfile profile, int x, int draftY, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, draftY + 60, 31, 13)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.OUTPUT_SLOT) {
				SlimeFlowState.finishPick(true);
			} else {
				SlimeFlowState.beginPick(SlimeFlowState.PickMode.OUTPUT_SLOT);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 43, draftY + 60, 42, 13)) {
			if (profile.rows.size() >= MAX_ROWS || !canAddOutput()) {
				return true;
			}

			profile.rows.add(SlimeFlowProfile.Row.output(SlimeFlowState.draftOutputSlot));
			SlimeFlowState.draftOutputSlot = -1;
			SlimeFlowState.finishPick(true);
			scrollToLast(profile);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 89, draftY + 60, 42, 13)) {
			profile.outputCollectLoop = !profile.outputCollectLoop;
			return true;
		}

		return false;
	}

	private static boolean clickMultiDraft(SlimeFlowProfile profile, int x, int draftY, double mouseX, double mouseY) {
		if (SlimeFlowUi.inside(mouseX, mouseY, x + 8, draftY + 60, 25, 13)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM) {
				SlimeFlowState.finishPick(true);
			} else {
				SlimeFlowState.beginPick(SlimeFlowState.PickMode.MULTI_ITEM);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 36, draftY + 60, 24, 13)) {
			if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_TO) {
				SlimeFlowState.finishPick(true);
			} else {
				SlimeFlowState.beginPick(SlimeFlowState.PickMode.MULTI_TO);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 63, draftY + 60, 14, 13)) {
			SlimeFlowState.draftMultiAmount = Math.max(1, SlimeFlowState.draftMultiAmount - 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 80, draftY + 60, 14, 13)) {
			SlimeFlowState.draftMultiAmount = Math.min(64, SlimeFlowState.draftMultiAmount + 1);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 142, draftY + 60, 27, 13)) {
			if (!SlimeFlowState.draftMultiItemNames.isEmpty()) {
				SlimeFlowState.draftMultiItemNames.remove(SlimeFlowState.draftMultiItemNames.size() - 1);
			}
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 173, draftY + 60, 25, 13)) {
			SlimeFlowState.draftMultiItemNames.clear();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 202, draftY + 60, 26, 13)) {
			SlimeFlowState.draftMultiAmount = Math.min(64, SlimeFlowState.draftMultiAmount + 32);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 100, draftY + 60, 38, 13)) {
			if (profile.rows.size() >= MAX_ROWS
					|| SlimeFlowState.draftMultiItemNames.isEmpty()
					|| SlimeFlowState.draftMultiTargetSlot < 0) {
				return true;
			}

			profile.rows.add(SlimeFlowProfile.Row.multi(
					SlimeFlowState.draftMultiItemNames,
					SlimeFlowState.draftMultiTargetSlot,
					SlimeFlowState.draftMultiAmount
			));

			SlimeFlowState.draftMultiItemNames.clear();
			SlimeFlowState.draftMultiTargetSlot = -1;
			SlimeFlowState.draftMultiAmount = 1;
			SlimeFlowState.finishPick(true);
			scrollToLast(profile);
			return true;
		}

		return false;
	}

	private static boolean clickRows(SlimeFlowProfile profile, int x, int y, int screenHeight, double mouseX, double mouseY) {
		int visibleRows = editVisibleRows(screenHeight);
		int startY = y + ROW_START_Y;

		for (int visibleIndex = 0; visibleIndex < visibleRows; visibleIndex++) {
			int rowIndex = rowScroll + visibleIndex;

			if (rowIndex >= profile.rows.size()) {
				break;
			}

			int rowY = startY + visibleIndex * ROW_H;

			if (SlimeFlowUi.inside(mouseX, mouseY, x + 166, rowY + 3, 13, 12)) {
				moveRowUp(profile, rowIndex);
				clampRowScroll(profile, screenHeight);
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, x + 181, rowY + 3, 13, 12)) {
				moveRowDown(profile, rowIndex);
				clampRowScroll(profile, screenHeight);
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, x + 197, rowY + 3, 21, 12)) {
				duplicateRow(profile, rowIndex);
				clampRowScroll(profile, screenHeight);
				return true;
			}

			if (SlimeFlowUi.inside(mouseX, mouseY, x + 221, rowY + 3, 18, 12)) {
				deleteRow(profile, rowIndex);
				clampRowScroll(profile, screenHeight);
				return true;
			}
		}

		return false;
	}

	public static boolean pickHoveredSlot(Minecraft client) {
		if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
			return false;
		}

		Slot slot = ((AbstractContainerScreenAccessor) screen).slimeflow$getHoveredSlot();

		if (slot == null) {
			return false;
		}

		AbstractContainerMenu menu = client.player.containerMenu;
		int slotId = -1;

		for (int i = 0; i < menu.slots.size(); i++) {
			if (menu.slots.get(i) == slot) {
				slotId = i;
				break;
			}
		}

		if (slotId < 0) {
			return false;
		}

		SlimeFlowProfile profile = SlimeFlowState.editingProfile;

		if (profile == null) {
			SlimeFlowState.finishPick(true);
			return false;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD) {
			String itemName = SlimeFlowItemFinder.getSlotItemName(slot);

			if (itemName == null || itemName.isEmpty()) {
				return true;
			}

			SlimeFlowBackpackRefillRunner.setCustomRefillKeyword(profile, itemName);
			SlimeFlowState.backpackKnownEmptySlots.clear();
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_FROM) {
			SlimeFlowState.draftFromSlot = slotId;
			SlimeFlowState.pickMode = SlimeFlowState.PickMode.MOVE_TO;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_TO) {
			SlimeFlowState.draftToSlot = slotId;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.CLICK_SLOT) {
			SlimeFlowState.draftClickSlot = slotId;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM) {
			String itemName = SlimeFlowItemFinder.getSlotItemName(slot);

			if (itemName == null || itemName.isEmpty()) {
				return true;
			}

			if (!SlimeFlowState.draftMultiItemNames.contains(itemName) && SlimeFlowState.draftMultiItemNames.size() < 9) {
				SlimeFlowState.draftMultiItemNames.add(itemName);
			}

			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_TO) {
			SlimeFlowState.draftMultiTargetSlot = slotId;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.OUTPUT_SLOT) {
			SlimeFlowState.draftOutputSlot = slotId;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_NAME) {
			String itemName = SlimeFlowItemFinder.getSlotItemName(slot);

			if (itemName == null || itemName.isEmpty()) {
				return true;
			}

			SlimeFlowState.draftItemName = itemName;
			SlimeFlowState.pickMode = SlimeFlowState.PickMode.ITEM_TO;
			return true;
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_TO) {
			SlimeFlowState.draftItemTargetSlot = slotId;

			// Continuous ITEM picker: press P once, then item -> target -> auto-add,
			// and immediately wait for the next item. Stop it with STP.
			if (SlimeFlowState.draftItemName != null
					&& !SlimeFlowState.draftItemName.isEmpty()
					&& profile.rows.size() < MAX_ROWS) {
				profile.rows.add(SlimeFlowProfile.Row.item(
						SlimeFlowState.draftItemName,
						SlimeFlowState.draftItemTargetSlot,
						SlimeFlowState.draftItemAmount
				));
				scrollToLast(profile);

				SlimeFlowState.draftItemName = "";
				SlimeFlowState.draftItemTargetSlot = -1;
				SlimeFlowState.pickMode = SlimeFlowState.PickMode.ITEM_NAME;
				return true;
			}

			return true;
		}

		return false;
	}

	public static boolean insidePanel(int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int h = editHeight(screenHeight);
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, EDIT_W, h);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, EDIT_W, h);

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, EDIT_W, h);
	}

	private static int aptStep(int current) {
		return current >= 10 ? 5 : 1;
	}


	private static void saveProfile(Minecraft client, SlimeFlowProfile profile) {
		if (profile.guiTitle == null || profile.guiTitle.isEmpty()) {
			profile.guiTitle = SlimeFlowState.getCurrentGuiTitle(client);
		}

		profile.speed = Math.max(0, Math.min(20, profile.speed));
		profile.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick);

		if (SlimeFlowState.editingProfileIndex >= 0) {
			SlimeFlowState.profiles.set(SlimeFlowState.editingProfileIndex, profile);
		} else {
			SlimeFlowState.profiles.add(profile);
			SlimeFlowListPanel.scrollToBottom();
		}

		SlimeFlowConfig.save();
		backToList();
	}

	private static void moveRowUp(SlimeFlowProfile profile, int rowIndex) {
		if (rowIndex <= 0 || rowIndex >= profile.rows.size()) {
			return;
		}

		SlimeFlowProfile.Row current = profile.rows.get(rowIndex);
		profile.rows.set(rowIndex, profile.rows.get(rowIndex - 1));
		profile.rows.set(rowIndex - 1, current);

		if (rowIndex == rowScroll) {
			rowScroll = Math.max(0, rowScroll - 1);
		}
	}

	private static void moveRowDown(SlimeFlowProfile profile, int rowIndex) {
		if (rowIndex < 0 || rowIndex >= profile.rows.size() - 1) {
			return;
		}

		SlimeFlowProfile.Row current = profile.rows.get(rowIndex);
		profile.rows.set(rowIndex, profile.rows.get(rowIndex + 1));
		profile.rows.set(rowIndex + 1, current);
	}

	private static void duplicateRow(SlimeFlowProfile profile, int rowIndex) {
		if (profile.rows.size() >= MAX_ROWS || rowIndex < 0 || rowIndex >= profile.rows.size()) {
			return;
		}

		profile.rows.add(rowIndex + 1, SlimeFlowUi.copyRow(profile.rows.get(rowIndex)));
	}

	private static void deleteRow(SlimeFlowProfile profile, int rowIndex) {
		if (rowIndex < 0 || rowIndex >= profile.rows.size()) {
			return;
		}

		profile.rows.remove(rowIndex);
	}

	private static void scrollToLast(SlimeFlowProfile profile) {
		rowScroll = Math.max(0, profile.rows.size() - 1);
	}

	private static int editHeight(int screenHeight) {
		return Math.max(252, Math.min(screenHeight - 52, 374));
	}

	private static int editVisibleRows(int screenHeight) {
		int h = editHeight(screenHeight);
		int footerTop = h - 22;
		int rowAreaBottom = footerTop - 4;

		if (SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE) {
			rowAreaBottom = footerTop - 18;
		}

		int available = rowAreaBottom - ROW_START_Y;
		int rows = available / ROW_H;

		if (SlimeFlowState.pickMode != SlimeFlowState.PickMode.NONE && rows <= 0) {
			return 0;
		}

		return Math.max(1, Math.min(MAX_ROWS, rows));
	}

	private static void clampRowScroll(SlimeFlowProfile profile, int screenHeight) {
		int visibleRows = Math.max(1, editVisibleRows(screenHeight));
		int max = Math.max(0, profile.rows.size() - visibleRows);

		if (rowScroll < 0) {
			rowScroll = 0;
		}

		if (rowScroll > max) {
			rowScroll = max;
		}
	}

	private static void backToList() {
		SlimeFlowState.overlayMode = SlimeFlowState.OverlayMode.LIST;
		SlimeFlowState.editingProfile = null;
		SlimeFlowState.editingProfileIndex = -1;
		SlimeFlowState.resetDraft();
		rowScroll = 0;
	}

	private static void renameProfile(Minecraft client, SlimeFlowProfile profile) {
		String title = cleanName(SlimeFlowState.getCurrentGuiTitle(client));

		if (title.isEmpty()) {
			title = "m";
		}

		int number = 1;
		String base = title;
		String next = base;

		while (nameExists(next, profile)) {
			number++;
			next = base + number;
		}

		profile.name = next;
	}

	private static boolean nameExists(String name, SlimeFlowProfile current) {
		for (SlimeFlowProfile profile : SlimeFlowState.profiles) {
			if (profile == current) {
				continue;
			}

			if (profile.name != null && profile.name.equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	private static String cleanName(String text) {
		if (text == null) {
			return "";
		}

		String cleaned = text.toLowerCase()
				.replace(" ", "_")
				.replace("[", "")
				.replace("]", "")
				.replace(":", "")
				.replace("/", "_")
				.replace("\\", "_");

		if (cleaned.length() > 12) {
			return cleaned.substring(0, 12);
		}

		return cleaned;
	}

	private static String rowText(SlimeFlowProfile.Row row) {
		SlimeFlowProfile.RowType type = row.type == null ? SlimeFlowProfile.RowType.MOVE : row.type;

		if (type == SlimeFlowProfile.RowType.MOVE) {
			return "MV F" + row.fromSlot + ">T" + row.toSlot + " A" + row.amount;
		}

		if (type == SlimeFlowProfile.RowType.ITEM) {
			return "IT " + SlimeFlowUi.cut(row.itemName, 9) + ">T" + row.itemTargetSlot + " A" + row.itemAmount;
		}

		if (type == SlimeFlowProfile.RowType.CLICK) {
			return "CL S" + row.clickSlot + " " + buttonName(row.clickButton) + "x" + row.clickTimes;
		}

		if (type == SlimeFlowProfile.RowType.OUTPUT) {
			return "OUT O" + row.outputSlot + ">Stack";
		}

		int multiCount = row.multiItemNames == null ? 0 : row.multiItemNames.size();
		String first = multiCount <= 0 ? "" : row.multiItemNames.get(0);
		return "MU I" + multiCount + " " + SlimeFlowUi.cut(first, 7) + ">T" + row.multiTargetSlot + " A" + row.multiAmount;
	}

	private static void renderMultiItemList(Minecraft client, GuiGraphicsExtractor graphics, int x, int y) {
		if (SlimeFlowState.draftMultiItemNames.isEmpty()) {
			graphics.text(client.font, "IT: Pick Items", x, y, SlimeFlowTheme.MUTED, false);
			return;
		}

		int max = Math.min(3, SlimeFlowState.draftMultiItemNames.size());
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < max; i++) {
			if (i > 0) {
				line.append(" ");
			}
			line.append(i + 1).append('.').append(SlimeFlowUi.cut(SlimeFlowState.draftMultiItemNames.get(i), 6));
		}

		if (SlimeFlowState.draftMultiItemNames.size() > max) {
			line.append(" +").append(SlimeFlowState.draftMultiItemNames.size() - max);
		}

		graphics.text(client.font, line.toString(), x, y, SlimeFlowTheme.TEXT, false);
	}

	private static boolean canAddMove() {
		return SlimeFlowState.draftFromSlot >= 0 && SlimeFlowState.draftToSlot >= 0;
	}

	private static boolean canAddItem() {
		return SlimeFlowState.draftItemName != null && !SlimeFlowState.draftItemName.isEmpty() && SlimeFlowState.draftItemTargetSlot >= 0;
	}

	private static boolean canAddClick() {
		return SlimeFlowState.draftClickSlot >= 0;
	}

	private static boolean canAddMulti() {
		return !SlimeFlowState.draftMultiItemNames.isEmpty() && SlimeFlowState.draftMultiTargetSlot >= 0;
	}

	private static boolean canAddOutput() {
		return SlimeFlowState.draftOutputSlot >= 0;
	}

	private static boolean pickMoveActive() {
		return SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_FROM
				|| SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_TO;
	}

	private static boolean pickItemActive() {
		return SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_NAME
				|| SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_TO;
	}

	private static String slotShort(String name, int slot) {
		return name + ":" + (slot >= 0 ? slot : "-");
	}

	private static String itemShort(String itemName) {
		return "I:" + (itemName == null || itemName.isEmpty() ? "-" : SlimeFlowUi.cut(itemName, 13));
	}

	private static int slotColor(int slot) {
		return slot >= 0 ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED;
	}

	private static int itemColor(String itemName) {
		return itemName != null && !itemName.isEmpty() ? SlimeFlowTheme.GREEN : SlimeFlowTheme.MUTED;
	}

	public static void finishPick(boolean keep) {
		SlimeFlowState.finishPick(keep);
	}

	public static String[] pickListLines() {
		String line1 = "";
		String line2 = "";
		String line3 = "";
		SlimeFlowProfile profile = SlimeFlowState.editingProfile;

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_FROM || SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_TO) {
			line1 = slotShort("From", SlimeFlowState.draftFromSlot) + "  " + slotShort("To", SlimeFlowState.draftToSlot);
			line2 = "Amount: " + SlimeFlowState.draftAmount;
		} else if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_NAME || SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_TO) {
			line1 = itemShort(SlimeFlowState.draftItemName) + "  " + slotShort("To", SlimeFlowState.draftItemTargetSlot);
			line2 = "Amount: " + SlimeFlowState.draftItemAmount + addedRowsText(profile);
		} else if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.CLICK_SLOT) {
			line1 = slotShort("Slot", SlimeFlowState.draftClickSlot);
			line2 = "Button: " + buttonName(SlimeFlowState.draftClickButton) + "  Times: " + SlimeFlowState.draftClickTimes;
		} else if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM || SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_TO) {
			line1 = "Items: " + SlimeFlowState.draftMultiItemNames.size() + "  " + slotShort("To", SlimeFlowState.draftMultiTargetSlot);
			line2 = multiItemsText();
			line3 = "Amount: " + SlimeFlowState.draftMultiAmount;
		} else if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.OUTPUT_SLOT) {
			line1 = slotShort("Output", SlimeFlowState.draftOutputSlot);
			line2 = profile == null || profile.outputCollectLoop ? "Mode: Loop collect" : "Mode: Once";
		} else if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD) {
			line1 = "Bag item: " + (profile == null ? "-" : SlimeFlowUi.cut(SlimeFlowBackpackRefillRunner.refillKeywordText(profile), 24));
		}

		return new String[] { line1, line2, line3 };
	}

	private static String addedRowsText(SlimeFlowProfile profile) {
		if (profile == null || !SlimeFlowState.pickSnapshotActive || SlimeFlowState.pickSnapshotProfile != profile) {
			return "";
		}

		int added = Math.max(0, profile.rows.size() - SlimeFlowState.pickSnapshotRows.size());
		return added <= 0 ? "" : "  Added: +" + added;
	}

	private static String multiItemsText() {
		if (SlimeFlowState.draftMultiItemNames.isEmpty()) {
			return "Pick item list";
		}

		StringBuilder text = new StringBuilder();
		int max = Math.min(4, SlimeFlowState.draftMultiItemNames.size());
		for (int i = 0; i < max; i++) {
			if (i > 0) {
				text.append(", ");
			}
			text.append(i + 1).append('.').append(SlimeFlowUi.cut(SlimeFlowState.draftMultiItemNames.get(i), 8));
		}
		if (SlimeFlowState.draftMultiItemNames.size() > max) {
			text.append(" +").append(SlimeFlowState.draftMultiItemNames.size() - max);
		}
		return text.toString();
	}

	public static String pickTitle() {
		return pickText();
	}

	private static String pickText() {
		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_FROM) {
			return "Pick From";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MOVE_TO) {
			return "Pick To";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.CLICK_SLOT) {
			return "Pick Slot";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_NAME) {
			return "Pick Item";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.ITEM_TO) {
			return "Pick Target";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_ITEM) {
			return "Pick Multi Items";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.MULTI_TO) {
			return "Pick Multi Target";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.OUTPUT_SLOT) {
			return "Pick Output";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.BACKPACK_SLOT) {
			return "Pick Backpack Slot";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.BACKPACK_ITEM) {
			return "Pick Refill Item";
		}

		if (SlimeFlowState.pickMode == SlimeFlowState.PickMode.REFILL_KEYWORD) {
			return "Pick Bag Name";
		}

		return "";
	}

	private static String buttonName(int button) {
		return button == 1 ? "R" : "L";
	}
}
