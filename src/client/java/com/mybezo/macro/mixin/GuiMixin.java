package com.mybezo.macro.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mybezo.macro.SlimeFlowOverlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(targets = "net.minecraft.client.gui.Gui")
public abstract class GuiMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
	private void mybezomacro$renderGameplayHud(
			GuiGraphicsExtractor graphics,
			DeltaTracker tickCounter,
			CallbackInfo ci
	) {
		Minecraft client = Minecraft.getInstance();

		if (client == null || client.getWindow() == null) {
			return;
		}

		SlimeFlowOverlay.renderGameplayHud(
				client,
				graphics,
				client.getWindow().getGuiScaledWidth(),
				client.getWindow().getGuiScaledHeight()
		);
	}
}
