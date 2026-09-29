package com.mybezo.macro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class SlimeFlowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SlimeFlowState.init();
		SlimeFlowCommands.register();

		ClientTickEvents.END_CLIENT_TICK.register(SlimeFlowClient::tick);
	}

	private static void tick(Minecraft client) {
		if (client == null) {
			return;
		}

		// F12 / Keys must keep working even while still on the title screen / main menu.
		SlimeFlowInput.tick(client);

		if (client.player == null || client.gameMode == null) {
			SlimeFlowState.clickQueue.clear();
			SlimeFlowStackRunner.stop(client);
			return;
		}

		SlimeFlowAutoLogin.tick(client);
		SlimeFlowAutoSell.tick(client);
		SlimeFlowAutoCommand.tick(client);
		SlimeFlowBackpackRefillRunner.tick(client);
		SlimeFlowRunner.handleAutoRun(client);
		SlimeFlowRunner.processClickQueue(client);
		SlimeFlowStackRunner.tick(client);
		SlimeFlowHud.tick(client);
	}
}
