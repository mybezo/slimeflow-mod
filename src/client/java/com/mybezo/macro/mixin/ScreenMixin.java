package com.mybezo.macro.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mybezo.macro.SlimeFlowOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
	private void mybezomacro$renderOverlayOnAnyScreen(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float delta,
			CallbackInfo ci
	) {
		Screen self = (Screen) (Object) this;

		if (self instanceof AbstractContainerScreen<?>) {
			return;
		}

		SlimeFlowOverlay.render(
				Minecraft.getInstance(),
				graphics,
				self.width,
				self.height,
				mouseX,
				mouseY
		);
	}
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
	private void mybezomacro$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (SlimeFlowOverlay.keyPressed(input.key(), input.scancode(), input.modifiers())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
	private void mybezomacro$mouseClicked(
			MouseButtonEvent event,
			boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir
	) {
		Screen self = (Screen) (Object) this;

		if (self instanceof AbstractContainerScreen<?>) {
			return;
		}

		boolean handled = SlimeFlowOverlay.mouseClicked(
				Minecraft.getInstance(),
				self.width,
				self.height,
				event.x(),
				event.y(),
				event.button()
		);

		if (handled) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
	private void mybezomacro$charTyped(CharacterEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (SlimeFlowOverlay.charTyped((char) input.codepoint(), 0)) {
			cir.setReturnValue(true);
		}
	}

}