package com.mybezo.macro;

import net.minecraft.client.Minecraft;

public final class SlimeFlowControl {
	private SlimeFlowControl() {
	}

	public static void stopAll(Minecraft client) {
		SlimeFlowBackpackRefillRunner.stopFromButton(client);
		SlimeFlowStackRunner.stop(client);
		SlimeFlowState.stopMacroRuntimeState();
	}
}
