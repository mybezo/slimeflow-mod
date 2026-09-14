package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SlimeFlowMorePanel {
	private static final int W = 118;
	private static final int H = 58;

	private SlimeFlowMorePanel() {
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

		SlimeFlowUi.fill(graphics, x, y, W, H, 0xD60B0E12);
		SlimeFlowUi.border(graphics, x, y, W, H, 0xB5FF343C);
		graphics.fill(x + 1, y + 1, x + W - 1, y + 2, 0x44FFFFFF);
		graphics.text(client.font, "About", x + 6, y + 5, SlimeFlowTheme.TEXT, false);
		SlimeFlowUi.drawButton(client, graphics, x + W - 18, y + 3, 13, 12, "x", SlimeFlowTheme.RED);
		graphics.fill(x + 5, y + 18, x + W - 5, y + 19, 0x66FF343C);

		graphics.text(client.font, "SlimeFlow by Mybezo", x + 7, y + 24, 0xCCFF4A52, false);
		graphics.text(client.font, "Version: 26.1.2", x + 7, y + 35, SlimeFlowTheme.MUTED, false);
		SlimeFlowUi.drawButton(client, graphics, x + 7, y + 45, W - 14, 10, "Join Discord", SlimeFlowTheme.BLUE);
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		int x = SlimeFlowUi.panelX(screenWidth, screenHeight, W, H);
		int y = SlimeFlowUi.panelY(screenWidth, screenHeight, W, H);

		if (SlimeFlowUi.inside(mouseX, mouseY, x + W - 18, y + 3, 13, 12)) {
			SlimeFlowOverlay.closeMenuOnly();
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x + 7, y + 45, W - 14, 10)) {
			SlimeFlowOverlay.showDiscordLink(client);
			return true;
		}

		if (SlimeFlowUi.inside(mouseX, mouseY, x, y, W, 18)) {
			SlimeFlowUi.startDrag(mouseX, mouseY, x, y);
			return true;
		}

		return SlimeFlowUi.inside(mouseX, mouseY, x, y, W, H);
	}
}
