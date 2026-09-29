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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
	private void slimeflow$renderOverlay(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float delta,
			CallbackInfo ci
	) {
		SlimeFlowOverlay.render(
				Minecraft.getInstance(),
				graphics,
				this.width,
				this.height,
				mouseX,
				mouseY
		);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
	private void slimeflow$mouseClicked(
			MouseButtonEvent event,
			boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir
	) {
		boolean handled = SlimeFlowOverlay.mouseClicked(
				Minecraft.getInstance(),
				this.width,
				this.height,
				event.x(),
				event.y(),
				event.button()
		);

		if (handled) {
			cir.setReturnValue(true);
		}
	}
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
	private void slimeflow$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (SlimeFlowOverlay.keyPressed(input.key(), input.scancode(), input.modifiers())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
	private void slimeflow$charTyped(CharacterEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (SlimeFlowOverlay.charTyped((char) input.codepoint(), 0)) {
			cir.setReturnValue(true);
		}
	}

}