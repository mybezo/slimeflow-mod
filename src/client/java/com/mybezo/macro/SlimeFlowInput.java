package com.mybezo.macro;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class SlimeFlowInput {
	private static boolean openUiWasDown = false;
	private static boolean stopWasDown = false;
	private static boolean stackStartWasDown = false;
	private static boolean autoSellWasDown = false;
	private static final Set<Integer> autoCommandKeysDown = new HashSet<>();
	private static boolean attackWasDown = false;
	private static boolean useWasDown = false;

	private SlimeFlowInput() {
	}

	public static void tick(Minecraft client) {
		if (client == null || client.getWindow() == null) {
			return;
		}

		if (SlimeFlowState.keybindEditingAction != SlimeFlowState.KEYBIND_NONE) {
			openUiWasDown = isKeyDown(client, SlimeFlowState.keyOpenUi);
			stopWasDown = isKeyDown(client, SlimeFlowState.keyStopAll);
			stackStartWasDown = isKeyDown(client, SlimeFlowState.keyStartStack);
			autoSellWasDown = isKeyDown(client, SlimeFlowState.autoSellKeybind);
			autoCommandKeysDown.clear();
			return;
		}

		handleUiToggle(client);

		if (client.player == null || client.gameMode == null) {
			stopWasDown = isKeyDown(client, SlimeFlowState.keyStopAll);
			stackStartWasDown = isKeyDown(client, SlimeFlowState.keyStartStack);
			autoSellWasDown = isKeyDown(client, SlimeFlowState.autoSellKeybind);
			autoCommandKeysDown.clear();
			return;
		}

		handleForceStop(client);
		handleStackStart(client);
		handleAutoSellKey(client);
		handleAutoCommandKeys(client);
		handleWoodenSwordSelection(client);
	}

	private static void handleUiToggle(Minecraft client) {
		boolean down = isKeyDown(client, SlimeFlowState.keyOpenUi);

		if (down && !openUiWasDown) {
			SlimeFlowOverlay.toggle(client);
		}

		openUiWasDown = down;
	}

	public static void markOpenUiKeyHandled() {
		openUiWasDown = true;
	}

	private static void handleForceStop(Minecraft client) {
		boolean down = isKeyDown(client, SlimeFlowState.keyStopAll);

		if (down && !stopWasDown) {
			SlimeFlowControl.stopAll(client);
		}

		stopWasDown = down;
	}

	private static void handleStackStart(Minecraft client) {
		boolean down = isKeyDown(client, SlimeFlowState.keyStartStack);

		if (down && !stackStartWasDown) {
			SlimeFlowStackRunner.startFromCrosshair(client);
		}

		stackStartWasDown = down;
	}

	private static void handleAutoSellKey(Minecraft client) {
		boolean down = isKeyDown(client, SlimeFlowState.autoSellKeybind);

		if (down && !autoSellWasDown) {
			SlimeFlowAutoSell.runCommandNow(client);
		}

		autoSellWasDown = down;
	}

	private static void handleAutoCommandKeys(Minecraft client) {
		Set<Integer> stillDown = new HashSet<>();
		for (SlimeFlowAutoCommandEntry entry : SlimeFlowState.autoCommandEntries) {
			if (entry == null || entry.keybind <= 0) {
				continue;
			}

			int key = entry.keybind;
			boolean down = isKeyDown(client, key);
			if (down) {
				stillDown.add(key);
				if (!autoCommandKeysDown.contains(key)) {
					SlimeFlowAutoCommand.runEntry(client, entry);
				}
			}
		}
		autoCommandKeysDown.clear();
		autoCommandKeysDown.addAll(stillDown);
	}

	private static boolean isKeyDown(Minecraft client, int keyCode) {
		try {
			return keyCode > 0 && InputConstants.isKeyDown(client.getWindow(), keyCode);
		} catch (Throwable ignored) {
			return false;
		}
	}

	private static void handleWoodenSwordSelection(Minecraft client) {
		if (client.player == null) {
			return;
		}

		boolean attackDown = client.options.keyAttack.isDown();
		boolean useDown = client.options.keyUse.isDown();
		boolean shiftDown = client.options.keyShift.isDown() || client.player.isShiftKeyDown();

		boolean holdingWoodenSword = client.player.getMainHandItem().is(Items.WOODEN_SWORD);

		if (!holdingWoodenSword || !shiftDown) {
			attackWasDown = attackDown;
			useWasDown = useDown;
			return;
		}

		if (!(client.hitResult instanceof BlockHitResult hit)) {
			attackWasDown = attackDown;
			useWasDown = useDown;
			return;
		}

		if (hit.getType() != HitResult.Type.BLOCK) {
			attackWasDown = attackDown;
			useWasDown = useDown;
			return;
		}

		// Record a stack machine: Shift + hit with a wooden sword.
		// Tidak ada limit jumlah mesin; posisi berdiri player ikut disimpan per mesin.
		if (attackDown && !attackWasDown) {
			SlimeFlowStackRunner.recordMachineTarget(client, hit);
			client.options.keyAttack.setDown(false);
		}

		attackWasDown = attackDown;
		useWasDown = useDown;
	}

}
