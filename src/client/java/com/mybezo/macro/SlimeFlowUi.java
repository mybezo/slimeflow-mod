package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import com.mybezo.macro.mixin.WindowAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowUi {
	private SlimeFlowUi() {
	}

	static void updateDrag(
			Minecraft client,
			int screenWidth,
			int screenHeight,
			int panelW,
			int panelH,
			int mouseX,
			int mouseY
	) {
		ensurePosition(screenWidth, screenHeight, panelW, panelH);

		if (SlimeFlowState.uiDragging && !isLeftMouseDown(client)) {
			SlimeFlowState.uiDragging = false;
		}

		if (SlimeFlowState.uiDragging) {
			SlimeFlowState.uiX = mouseX - SlimeFlowState.uiDragOffsetX;
			SlimeFlowState.uiY = mouseY - SlimeFlowState.uiDragOffsetY;
		}

		clampPanel(screenWidth, screenHeight, panelW, panelH);
	}

	static void startDrag(double mouseX, double mouseY, int panelX, int panelY) {
		SlimeFlowState.uiDragging = true;
		SlimeFlowState.uiDragOffsetX = (int) mouseX - panelX;
		SlimeFlowState.uiDragOffsetY = (int) mouseY - panelY;
	}

	static void stopDrag() {
		SlimeFlowState.uiDragging = false;
	}

	private static boolean isLeftMouseDown(Minecraft client) {
		try {
			if (client == null || client.getWindow() == null) {
				return false;
			}

			long handle = ((WindowAccessor) (Object) client.getWindow()).slimeflow$getHandle();
			return GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		} catch (Throwable ignored) {
			try {
				return client != null
						&& client.options != null
						&& client.options.keyAttack != null
						&& client.options.keyAttack.isDown();
			} catch (Throwable ignoredToo) {
				return false;
			}
		}
	}

	static int panelX(int screenWidth, int screenHeight, int panelW, int panelH) {
		ensurePosition(screenWidth, screenHeight, panelW, panelH);
		return SlimeFlowState.uiX;
	}

	static int panelY(int screenWidth, int screenHeight, int panelW, int panelH) {
		ensurePosition(screenWidth, screenHeight, panelW, panelH);
		return SlimeFlowState.uiY;
	}


	static void centerPanel(int screenWidth, int screenHeight, int panelW, int panelH) {
		// Spawn under the HUD instead of screen-center.
		SlimeFlowState.uiX = 3;
		SlimeFlowState.uiY = 23;
		clampPanel(screenWidth, screenHeight, panelW, panelH);
	}

	private static void ensurePosition(int screenWidth, int screenHeight, int panelW, int panelH) {
		if (SlimeFlowState.uiX == Integer.MIN_VALUE) {
			centerPanel(screenWidth, screenHeight, panelW, panelH);
		}

		clampPanel(screenWidth, screenHeight, panelW, panelH);
	}

	private static void clampPanel(int screenWidth, int screenHeight, int panelW, int panelH) {
		int maxX = Math.max(0, screenWidth - panelW - 5);
		int maxY = Math.max(0, screenHeight - panelH - 5);

		if (SlimeFlowState.uiX < 0) {
			SlimeFlowState.uiX = 0;
		}

		if (SlimeFlowState.uiY < 0) {
			SlimeFlowState.uiY = 0;
		}

		if (SlimeFlowState.uiX > maxX) {
			SlimeFlowState.uiX = maxX;
		}

		if (SlimeFlowState.uiY > maxY) {
			SlimeFlowState.uiY = maxY;
		}
	}

	/** Text scale relative to Minecraft's default 8px font; boxes stay full size. */
	private static final float TEXT_SCALE = 0.75f;

	private static String toDisplayCase(String text) {
		return text == null ? "" : text.toUpperCase();
	}

	/** Width of {@code text} as drawn by {@link #drawScaledText}. */
	static int textWidth(Minecraft client, String text) {
		return (int) (client.font.width("\u00A7l" + toDisplayCase(text)) * TEXT_SCALE);
	}

	/** Draws bold text scaled down from Minecraft's default font. */
	private static void drawScaledText(Minecraft client, GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
		graphics.pose().pushMatrix();
		graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
		graphics.text(client.font, "\u00A7l" + text, (int) (x / TEXT_SCALE), (int) (y / TEXT_SCALE), color, false);
		graphics.pose().popMatrix();
	}

	/** Label text style used for all buttons and regular labels. */
	static void text(Minecraft client, GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
		drawScaledText(client, graphics, toDisplayCase(text), x, y, color);
	}

	/** Compact two-segment on/off indicator. */
	static void drawToggleChip(GuiGraphicsExtractor graphics, int x, int y, boolean on) {
		int w = 12;
		int h = 5;
		int half = w / 2;

		graphics.fill(x, y, x + half, y + h, on ? 0x66E0393F : 0xFFE0393F);
		graphics.fill(x + half, y, x + w, y + h, on ? 0xFF3DDC97 : 0x663DDC97);
		border(graphics, x, y, w, h, 0x66000000);
	}

	static void drawButton(
			Minecraft client,
			GuiGraphicsExtractor graphics,
			int x,
			int y,
			int w,
			int h,
			String text,
			int color
	) {
		text = toDisplayCase(text);

		int borderColor = buttonBorder(color);

		fill(graphics, x, y, w, h, 0x9010151A);
		border(graphics, x, y, w, h, borderColor);

		int textW = textWidth(client, text);
		int textX = x + Math.max(2, (w - textW) / 2);
		int textY = y + Math.max(1, (h - (int) (8 * TEXT_SCALE)) / 2) + 1;
		drawScaledText(client, graphics, text, textX + 1, textY + 1, 0x99000000);
		drawScaledText(client, graphics, text, textX, textY, buttonText(color));
	}

	private static int buttonBorder(int color) {
		if (color == SlimeFlowTheme.RED) {
			return 0xCCFF343C;
		}

		if (color == SlimeFlowTheme.GREEN) {
			return 0xAA7CFF9B;
		}

		if (color == SlimeFlowTheme.BLUE) {
			return 0xAA39B9FF;
		}

		if (color == SlimeFlowTheme.YELLOW) {
			return 0xAAFFD36B;
		}

		if (color == SlimeFlowTheme.ORANGE) {
			return 0xAAFF9F43;
		}

		if (color == SlimeFlowTheme.PURPLE) {
			return 0xCCB45CFF;
		}

		return 0x664A6272;
	}

	private static int buttonText(int color) {
		if (color == SlimeFlowTheme.RED) {
			return 0xFFFFFFFF;
		}

		if (color == SlimeFlowTheme.GREEN) {
			return 0xFFE8FFEE;
		}

		if (color == SlimeFlowTheme.BLUE) {
			return 0xFFEAF8FF;
		}

		if (color == SlimeFlowTheme.YELLOW) {
			return 0xFFFFF0BC;
		}

		if (color == SlimeFlowTheme.PURPLE) {
			return 0xFFF6EAFF;
		}

		return SlimeFlowTheme.TEXT;
	}

	static String cut(String text, int max) {
		if (text == null) {
			return "";
		}

		if (text.length() <= max) {
			return text;
		}

		return text.substring(0, max);
	}

	static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
	}

	static void fill(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
		graphics.fill(x, y, x + w, y + h, color);
	}

	static void border(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
		graphics.outline(x, y, w, h, color);
	}

	static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int accent) {
		fill(graphics, x, y, w, h, SlimeFlowTheme.WINDOW);
		if (w > 4 && h > 18) {
			fill(graphics, x + 1, y + 1, w - 2, 16, SlimeFlowTheme.HEADER);
			graphics.fill(x + 1, y + 1, x + w - 1, y + 2, SlimeFlowTheme.HAIRLINE);
			graphics.fill(x + 1, y + 17, x + w - 1, y + 18, accent);
			graphics.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, SlimeFlowTheme.SHADOW);
		}
		border(graphics, x - 1, y - 1, w + 2, h + 2, 0x55000000);
		border(graphics, x, y, w, h, accent);
	}

	/** Fakes a rounded corner by painting over corner pixels with parentBg. Only valid when parentBg is a known solid color. */
	static void cutCorners(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int parentBg) {
		if (w < 6 || h < 6) {
			return;
		}
		graphics.fill(x, y, x + 1, y + 1, parentBg);
		graphics.fill(x + w - 1, y, x + w, y + 1, parentBg);
		graphics.fill(x, y + h - 1, x + 1, y + h, parentBg);
		graphics.fill(x + w - 1, y + h - 1, x + w, y + h, parentBg);
	}

	/** Same as {@link #drawButton}, but with rounded corners against a known parent background. */
	static void drawButtonRounded(
			Minecraft client,
			GuiGraphicsExtractor graphics,
			int x,
			int y,
			int w,
			int h,
			String text,
			int color,
			int parentBg
	) {
		drawButton(client, graphics, x, y, w, h, text, color);
		cutCorners(graphics, x, y, w, h, parentBg);
	}

	// 7x7 pixel-art icons.
	private static final String[] ICON_INFO = {
			".###.",
			"#...#",
			"#.#.#",
			"#...#",
			"#.#.#",
			"#.#.#",
			".###."
	};
	private static final String[] ICON_ACTION = {
			"#.#.#",
			".###.",
			"##.##",
			"#...#",
			"##.##",
			".###.",
			"#.#.#"
	};
	private static final String[] ICON_LIST = {
			".....",
			"#.###",
			".....",
			"#.###",
			".....",
			"#.###",
			"....."
	};
	private static final String[] ICON_SAVE = {
			"#####",
			"#...#",
			"#.#.#",
			"#.#.#",
			"#...#",
			"#...#",
			"#####"
	};
	private static final String[] ICON_BACK = {
			"...#.",
			"..##.",
			".###.",
			"####.",
			".###.",
			"..##.",
			"...#."
	};

	static void iconInfo(GuiGraphicsExtractor graphics, int x, int y, int color) {
		drawIconGrid(graphics, x, y, ICON_INFO, color);
	}

	static void iconAction(GuiGraphicsExtractor graphics, int x, int y, int color) {
		drawIconGrid(graphics, x, y, ICON_ACTION, color);
	}

	static void iconList(GuiGraphicsExtractor graphics, int x, int y, int color) {
		drawIconGrid(graphics, x, y, ICON_LIST, color);
	}

	static void iconSave(GuiGraphicsExtractor graphics, int x, int y, int color) {
		drawIconGrid(graphics, x, y, ICON_SAVE, color);
	}

	static void iconBack(GuiGraphicsExtractor graphics, int x, int y, int color) {
		drawIconGrid(graphics, x, y, ICON_BACK, color);
	}

	private static void drawIconGrid(GuiGraphicsExtractor graphics, int x, int y, String[] rows, int color) {
		for (int row = 0; row < rows.length; row++) {
			String line = rows[row];
			for (int col = 0; col < line.length(); col++) {
				if (line.charAt(col) == '#') {
					graphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
				}
			}
		}
	}

	static SlimeFlowProfile copyProfile(SlimeFlowProfile old) {
		SlimeFlowProfile p = new SlimeFlowProfile();

		p.name = old.name;
		p.autoRun = old.autoRun;
		p.guiTitle = old.guiTitle;
		p.speed = old.speed;
		p.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(old.actionsPerTick);
		p.outputCollectLoop = old.outputCollectLoop;
		p.backpackRefillEnabled = old.backpackRefillEnabled;
		p.backpackRefillItemName = old.backpackRefillItemName;

		if (old.backpackRefillKeywords != null) {
			p.backpackRefillKeywords.addAll(old.backpackRefillKeywords);
		}

		if (old.backpackSlots != null) {
			p.backpackSlots.addAll(old.backpackSlots);
		}

		for (SlimeFlowProfile.Row row : old.rows) {
			p.rows.add(copyRow(row));
		}

		return p;
	}

	static SlimeFlowProfile.Row copyRow(SlimeFlowProfile.Row row) {
		SlimeFlowProfile.Row newRow = new SlimeFlowProfile.Row();

		newRow.type = row.type == null ? SlimeFlowProfile.RowType.MOVE : row.type;

		newRow.fromSlot = row.fromSlot;
		newRow.toSlot = row.toSlot;
		newRow.amount = row.amount;

		newRow.clickSlot = row.clickSlot;
		newRow.clickButton = row.clickButton;
		newRow.clickTimes = row.clickTimes;

		newRow.itemName = row.itemName;
		newRow.itemTargetSlot = row.itemTargetSlot;
		newRow.itemAmount = row.itemAmount;

		if (row.multiItemNames != null) {
			newRow.multiItemNames.addAll(row.multiItemNames);
		}
		newRow.multiTargetSlot = row.multiTargetSlot;
		newRow.multiAmount = row.multiAmount;

		newRow.outputSlot = row.outputSlot;

		newRow.delay = row.delay;

		return newRow;
	}
}
