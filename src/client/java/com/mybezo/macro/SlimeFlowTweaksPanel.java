package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Compatibility stub - Utility was removed from the HUD. */
public final class SlimeFlowTweaksPanel {
	private SlimeFlowTweaksPanel() {
	}

	public static int width() {
		return 1;
	}

	public static int height() {
		return 1;
	}

	public static void render(Minecraft client, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
	}

	public static boolean click(Minecraft client, int screenWidth, int screenHeight, double mouseX, double mouseY) {
		return false;
	}
}
