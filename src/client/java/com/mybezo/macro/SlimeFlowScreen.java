package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class SlimeFlowScreen extends Screen {
	public SlimeFlowScreen() {
		super(Component.literal("SlimeFlow"));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean handled = SlimeFlowOverlay.mouseClicked(
				Minecraft.getInstance(),
				this.width,
				this.height,
				event.x(),
				event.y(),
				event.button()
		);

		if (handled) {
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (SlimeFlowOverlay.keyPressed(event.key(), event.scancode(), event.modifiers())) {
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (SlimeFlowOverlay.charTyped((char) event.codepoint(), 0)) {
			return true;
		}

		return super.charTyped(event);
	}
}
