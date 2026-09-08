package com.mybezo.macro;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public final class SlimeFlowRunner {
	private SlimeFlowRunner() {
	}

	public static void handleAutoRun(Minecraft client) {
		if (client.player == null || client.gameMode == null) {
			SlimeFlowState.clickQueue.clear();
			SlimeFlowState.stopOutputCollectorState();
			return;
		}

		boolean inContainerScreen = client.screen instanceof AbstractContainerScreen<?>;
		AbstractContainerMenu menu = client.player.containerMenu;

		if (SlimeFlowState.macroHardStopped) {
			if (!inContainerScreen) {
				SlimeFlowState.clearHardStop();
				SlimeFlowState.lastContainerId = -999;
				SlimeFlowState.autoRanThisGui = false;
				SlimeFlowState.autoRunDelayTicks = -1;
			} else {
				int currentContainerId = menu == null ? -999 : menu.containerId;

				if (SlimeFlowState.hardStoppedContainerId == -999) {
					SlimeFlowState.hardStoppedContainerId = currentContainerId;
				}

				if (currentContainerId == SlimeFlowState.hardStoppedContainerId) {
					SlimeFlowState.autoRanThisGui = true;
					return;
				}

				SlimeFlowState.clearHardStop();
				SlimeFlowState.lastContainerId = -999;
				SlimeFlowState.autoRanThisGui = false;
				SlimeFlowState.autoRunDelayTicks = 5;
			}
		}

		if (SlimeFlowBackpackRefillRunner.isRunning()) {
			return;
		}

		if (!inContainerScreen) {
			SlimeFlowState.lastContainerId = -999;
			SlimeFlowState.autoRanThisGui = false;
			SlimeFlowState.autoRunDelayTicks = -1;
			SlimeFlowState.stopOutputCollectorState();
			return;
		}

		if (menu == null) {
			return;
		}

		if (SlimeFlowState.lastContainerId != menu.containerId) {
			SlimeFlowState.lastContainerId = menu.containerId;
			SlimeFlowState.autoRanThisGui = false;
			SlimeFlowState.autoRunDelayTicks = 5;
		}

		if (SlimeFlowState.autoRanThisGui) {
			return;
		}

		if (SlimeFlowState.autoRunDelayTicks > 0) {
			SlimeFlowState.autoRunDelayTicks--;
			return;
		}

		String title = SlimeFlowState.getCurrentGuiTitle(client);

		if (SlimeFlowState.backpackResumePending && SlimeFlowState.backpackResumeProfile != null) {
			if (!SlimeFlowState.profiles.contains(SlimeFlowState.backpackResumeProfile)) {
				SlimeFlowState.stopMacroRuntimeState();
				return;
			}

			SlimeFlowProfile profile = SlimeFlowState.backpackResumeProfile;

			if (matchesGui(profile, title)) {
				int resumeRow = Math.max(0, SlimeFlowState.backpackResumeRowReady);

				SlimeFlowState.backpackResumePending = false;
				SlimeFlowState.backpackResumeProfile = null;
				SlimeFlowState.backpackResumeRowReady = -1;

				scheduleProfileFromRow(client, profile, resumeRow);
				SlimeFlowState.autoRanThisGui = true;
				return;
			}
		}

		for (SlimeFlowProfile profile : SlimeFlowState.profiles) {
			if (!profile.autoRun) {
				continue;
			}

			if (!matchesGui(profile, title)) {
				continue;
			}

			scheduleProfile(client, profile, false);
			SlimeFlowState.autoRanThisGui = true;
			return;
		}
	}

	public static void scheduleProfile(Minecraft client, SlimeFlowProfile profile, boolean manualRun) {
		scheduleProfileFromRow(client, profile, 0, manualRun);
	}

	public static void scheduleProfileFromRow(Minecraft client, SlimeFlowProfile profile, int startRow) {
		scheduleProfileFromRow(client, profile, startRow, true);
	}

	private static void scheduleProfileFromRow(Minecraft client, SlimeFlowProfile profile, int startRow, boolean manualRun) {
		SlimeFlowState.clearHardStop();

		if (client.player == null || client.gameMode == null || profile == null) {
			return;
		}

		if (!(client.screen instanceof AbstractContainerScreen<?>)) {
			return;
		}

		if (manualRun) {
			SlimeFlowState.clickQueue.clear();
		}

		SlimeFlowState.delayTicks = Math.max(0, profile.speed);
		SlimeFlowState.actionsPerTick = SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick);
		SlimeFlowState.clickCooldownTicks = 0;
		SlimeFlowState.runningProfile = profile;
		armOutputCollector(client, profile);

		for (int rowIndex = Math.max(0, startRow); rowIndex < profile.rows.size(); rowIndex++) {
			SlimeFlowProfile.Row row = profile.rows.get(rowIndex);
			SlimeFlowProfile.RowType type = row.type == null ? SlimeFlowProfile.RowType.MOVE : row.type;

			if (type == SlimeFlowProfile.RowType.MOVE) {
				queueMove(row.fromSlot, row.toSlot, row.amount);
			} else if (type == SlimeFlowProfile.RowType.ITEM) {
				queueDynamicItem(row.itemName, row.itemTargetSlot, row.itemAmount, rowIndex);
				queueAutoSyncWait(profile);
			} else if (type == SlimeFlowProfile.RowType.CLICK) {
				queueClick(row.clickSlot, row.clickButton, row.clickTimes);
			} else if (type == SlimeFlowProfile.RowType.MULTI) {
				queueMultiItem(row.multiItemNames, row.multiTargetSlot, row.multiAmount, rowIndex);
				queueAutoSyncWait(profile);
			} else if (type == SlimeFlowProfile.RowType.OUTPUT) {
				if (!profile.outputCollectLoop) {
					queueOutput(row.outputSlot);
					queueAutoSyncWait(profile);
				}
			}

			if (row.delay > 0) {
				for (int i = 0; i < row.delay; i++) {
					SlimeFlowState.clickQueue.add(new SlimeFlowState.ClickAction(-999, 0, ContainerInput.PICKUP));
				}
			}
		}
	}

	public static void processClickQueue(Minecraft client) {
		if (SlimeFlowState.macroHardStopped) {
			SlimeFlowState.clickQueue.clear();
			return;
		}

		if (SlimeFlowBackpackRefillRunner.isRunning()) {
			return;
		}

		if (client.player == null || client.gameMode == null) {
			SlimeFlowState.clickQueue.clear();
			SlimeFlowState.stopOutputCollectorState();
			return;
		}

		if (!(client.screen instanceof AbstractContainerScreen<?>)) {
			SlimeFlowState.clickQueue.clear();
			return;
		}

		if (SlimeFlowState.clickCooldownTicks > 0) {
			SlimeFlowState.clickCooldownTicks--;
			return;
		}

		LocalPlayer player = client.player;
		AbstractContainerMenu menu = player.containerMenu;

		if (menu == null) {
			SlimeFlowState.clickQueue.clear();
			SlimeFlowState.stopOutputCollectorState();
			return;
		}

		int actionBurstBudget = SlimeFlowState.delayTicks == 0 ? SlimeFlowProfile.normalizeActionsPerTick(SlimeFlowState.actionsPerTick) : 1;
		int rawPacketBudget = SlimeFlowState.delayTicks == 0 ? Math.min(256, 8 * Math.max(1, actionBurstBudget)) : 1;

		if (processOutputCollector(client, player, menu, actionBurstBudget)) {
			return;
		}

		if (SlimeFlowState.clickQueue.isEmpty()) {
			return;
		}

		while (rawPacketBudget > 0 && !SlimeFlowState.clickQueue.isEmpty()) {
			SlimeFlowState.ClickAction next = SlimeFlowState.clickQueue.peek();

			if (next != null && actionBurstBudget <= 0 && isMacroStep(next)) {
				SlimeFlowState.clickCooldownTicks = specialYieldCooldown();
				return;
			}

			SlimeFlowState.ClickAction action = SlimeFlowState.clickQueue.poll();

			if (action.slotId() == -999) {
				SlimeFlowState.clickCooldownTicks = Math.max(1, SlimeFlowState.delayTicks);
				return;
			}

			if (action.slotId() == -995) {
				if (collectOutputSlot(client, player, menu, action.itemTargetSlot())) {
					actionBurstBudget--;
					rawPacketBudget--;
				} else {
					rawPacketBudget--;
				}

				continue;
			}

			if (isRowStartSentinel(action) && !menu.getCarried().isEmpty()) {
				// Cursor stuck from a previous row (target likely rejected it) - stop instead of risking further corruption.
				SlimeFlowState.clickQueue.clear();
				SlimeFlowState.stopMacroRuntimeState();
				SlimeFlowState.msg(client, "Macro stopped: an item got stuck on the cursor (target slot may have rejected it).");
				return;
			}

			if (action.slotId() == -996) {
				if (isSlotOccupied(menu, action.itemTargetSlot())) {
					rawPacketBudget--;
					continue;
				}

				int sourceSlot = findFirstMultiItemSource(menu, action.itemNames());

				if (sourceSlot >= 0) {
					pushMoveToFront(menu, sourceSlot, action.itemTargetSlot(), action.itemAmount());
					actionBurstBudget--;
					continue;
				}

				rawPacketBudget--;
				continue;
			}

			if (action.slotId() == -997) {
				if (!isSlotOccupied(menu, action.itemTargetSlot())) {
					pushMoveToFront(menu, action.button(), action.itemTargetSlot(), action.itemAmount());
					actionBurstBudget--;
					continue;
				}

				rawPacketBudget--;
				continue;
			}

			if (action.slotId() == -998) {
				if (isSlotOccupied(menu, action.itemTargetSlot())) {
					rawPacketBudget--;
					continue;
				}

				int sourceSlot = SlimeFlowItemFinder.findPlayerInventorySlotByName(menu, action.itemName());

				if (sourceSlot >= 0) {
					pushMoveToFront(menu, sourceSlot, action.itemTargetSlot(), action.itemAmount());
					actionBurstBudget--;
					continue;
				}

				SlimeFlowProfile activeProfile = SlimeFlowState.runningProfile;

				if (activeProfile == null) {
					activeProfile = findAutoProfileForCurrentGui(client);
				}

				if (activeProfile != null && activeProfile.backpackRefillEnabled) {
					if (SlimeFlowBackpackRefillRunner.start(client, activeProfile, action.profileRowIndex(), action.itemName())) {
						return;
					}
				}

				rawPacketBudget--;
				continue;
			}

			if (action.slotId() < 0 || action.slotId() >= menu.slots.size()) {
				rawPacketBudget--;
				continue;
			}

			client.gameMode.handleContainerInput(menu.containerId, action.slotId(), action.button(), action.input(), player);

			rawPacketBudget--;
		}

		SlimeFlowState.clickCooldownTicks = SlimeFlowState.delayTicks;
	}

	private static boolean isRowStartSentinel(SlimeFlowState.ClickAction action) {
		if (action == null) {
			return false;
		}

		return action.slotId() == -996 || action.slotId() == -997 || action.slotId() == -998;
	}

	private static boolean isMacroStep(SlimeFlowState.ClickAction action) {
		if (action == null) {
			return false;
		}

		return action.slotId() == -995
				|| action.slotId() == -996
				|| action.slotId() == -997
				|| action.slotId() == -998;
	}

	private static boolean shouldYieldAfterSpecial(int actionBurstBudget) {
		return SlimeFlowState.delayTicks > 0 || actionBurstBudget <= 0;
	}

	private static int specialYieldCooldown() {
		if (SlimeFlowState.delayTicks > 0) {
			return Math.max(1, SlimeFlowState.delayTicks);
		}

		return 0;
	}

	private static void armOutputCollector(Minecraft client, SlimeFlowProfile profile) {
		SlimeFlowState.outputCollectSlots.clear();
		SlimeFlowState.outputCollectActive = false;
		SlimeFlowState.outputCollectContainerId = -999;

		if (client == null || client.player == null || client.player.containerMenu == null || profile == null) {
			return;
		}

		for (SlimeFlowProfile.Row row : profile.rows) {
			if (row == null || row.type != SlimeFlowProfile.RowType.OUTPUT || row.outputSlot < 0) {
				continue;
			}

			if (!SlimeFlowState.outputCollectSlots.contains(row.outputSlot)) {
				SlimeFlowState.outputCollectSlots.add(row.outputSlot);
			}
		}

		SlimeFlowState.outputCollectActive = profile.outputCollectLoop && !SlimeFlowState.outputCollectSlots.isEmpty();
		SlimeFlowState.outputCollectContainerId = SlimeFlowState.outputCollectActive
				? client.player.containerMenu.containerId
				: -999;
	}

	private static boolean processOutputCollector(Minecraft client, LocalPlayer player, AbstractContainerMenu menu, int actionBurstBudget) {
		if (!SlimeFlowState.outputCollectActive || SlimeFlowState.outputCollectSlots.isEmpty()) {
			return false;
		}

		if (SlimeFlowState.outputCollectContainerId != -999 && menu.containerId != SlimeFlowState.outputCollectContainerId) {
			SlimeFlowState.stopOutputCollectorState();
			return false;
		}

		if (!menu.getCarried().isEmpty()) {
			return false;
		}

		int budget = SlimeFlowState.delayTicks == 0 ? Math.max(1, actionBurstBudget) : 1;
		int collected = 0;

		for (int outputSlot : SlimeFlowState.outputCollectSlots) {
			if (budget <= 0) {
				break;
			}

			if (outputSlot < 0 || outputSlot >= menu.slots.size()) {
				continue;
			}

			if (!isSlotOccupied(menu, outputSlot)) {
				continue;
			}

			if (collectOutputSlot(client, player, menu, outputSlot)) {
				collected++;
				budget--;
				continue;
			}

			SlimeFlowState.clickCooldownTicks = Math.max(3, SlimeFlowState.delayTicks + 1);
			return false;
		}

		if (collected <= 0) {
			return false;
		}

		SlimeFlowState.clickCooldownTicks = SlimeFlowState.delayTicks;
		return true;
	}


	private static void queueMove(int fromSlot, int toSlot, int amount) {
		if (fromSlot < 0 || toSlot < 0) {
			return;
		}

		SlimeFlowState.clickQueue.add(SlimeFlowState.ClickAction.guardedMove(fromSlot, toSlot, amount));
	}

	private static void pushMoveToFront(AbstractContainerMenu menu, int fromSlot, int toSlot, int amount) {
		if (fromSlot < 0 || toSlot < 0 || menu == null) {
			return;
		}

		int available = fromSlot < menu.slots.size() ? menu.getSlot(fromSlot).getItem().getCount() : 0;
		if (available <= 0) {
			return;
		}

		// Cap clicks to the actual stack size to avoid overshooting into the target slot.
		int want = Math.min(Math.max(1, amount), available);

		if (want >= available) {
			// Full stack: one click instead of one per item. Deposit must be queued before pickup.
			SlimeFlowState.clickQueue.addFirst(new SlimeFlowState.ClickAction(toSlot, 0, ContainerInput.PICKUP));
			SlimeFlowState.clickQueue.addFirst(new SlimeFlowState.ClickAction(fromSlot, 0, ContainerInput.PICKUP));
			return;
		}

		SlimeFlowState.clickQueue.addFirst(new SlimeFlowState.ClickAction(fromSlot, 0, ContainerInput.PICKUP));

		for (int i = want - 1; i >= 0; i--) {
			SlimeFlowState.clickQueue.addFirst(new SlimeFlowState.ClickAction(toSlot, 1, ContainerInput.PICKUP));
		}

		SlimeFlowState.clickQueue.addFirst(new SlimeFlowState.ClickAction(fromSlot, 0, ContainerInput.PICKUP));
	}

	private static void queueDynamicItem(String itemName, int targetSlot, int amount, int rowIndex) {
		if (itemName == null || itemName.isEmpty() || targetSlot < 0) {
			return;
		}

		SlimeFlowState.clickQueue.add(SlimeFlowState.ClickAction.dynamicItem(itemName, targetSlot, amount, rowIndex));
	}

	private static void queueMultiItem(List<String> itemNames, int targetSlot, int amount, int rowIndex) {
		if (itemNames == null || itemNames.isEmpty() || targetSlot < 0) {
			return;
		}

		SlimeFlowState.clickQueue.add(SlimeFlowState.ClickAction.multiItem(itemNames, targetSlot, amount, rowIndex));
	}

	private static void queueOutput(int outputSlot) {
		if (outputSlot < 0) {
			return;
		}

		SlimeFlowState.clickQueue.add(SlimeFlowState.ClickAction.output(outputSlot));
	}

	private static boolean collectOutputSlot(Minecraft client, LocalPlayer player, AbstractContainerMenu menu, int outputSlot) {
		if (client == null || client.gameMode == null || player == null || menu == null) {
			return false;
		}

		if (outputSlot < 0 || outputSlot >= menu.slots.size()) {
			return false;
		}

		if (!isSlotOccupied(menu, outputSlot)) {
			return false;
		}

		if (menu.getCarried() != null && !menu.getCarried().isEmpty()) {
			return false;
		}

		// Shift-click output. Minecraft/server will stack into existing inventory stacks first,
		// then use an empty inventory slot when no matching stack has room.
		client.gameMode.handleContainerInput(menu.containerId, outputSlot, 0, ContainerInput.QUICK_MOVE, player);
		return true;
	}

	private static void queueAutoSyncWait(SlimeFlowProfile profile) {
		if (profile == null) {
			queueWait(1);
			return;
		}

		if (profile.speed <= 0 && SlimeFlowProfile.normalizeActionsPerTick(profile.actionsPerTick) > 1) {
			return;
		}

		queueWait(Math.max(2, profile.speed + 1));
	}


	private static void queueWait(int ticks) {
		int count = Math.max(1, ticks);

		for (int i = 0; i < count; i++) {
			SlimeFlowState.clickQueue.add(new SlimeFlowState.ClickAction(-999, 0, ContainerInput.PICKUP));
		}
	}

	private static void queueClick(int slot, int button, int times) {
		if (slot < 0) {
			return;
		}

		int count = Math.max(1, times);

		for (int i = 0; i < count; i++) {
			SlimeFlowState.clickQueue.add(new SlimeFlowState.ClickAction(slot, button, ContainerInput.PICKUP));
		}
	}

	private static int findFirstMultiItemSource(AbstractContainerMenu menu, List<String> itemNames) {
		if (itemNames == null || itemNames.isEmpty()) {
			return -1;
		}

		for (String itemName : itemNames) {
			if (itemName == null || itemName.isEmpty()) {
				continue;
			}

			int sourceSlot = SlimeFlowItemFinder.findPlayerInventorySlotByName(menu, itemName);

			if (sourceSlot >= 0) {
				return sourceSlot;
			}
		}

		return -1;
	}

	private static boolean isSlotOccupied(AbstractContainerMenu menu, int slotId) {
		if (menu == null || slotId < 0 || slotId >= menu.slots.size()) {
			return false;
		}

		return !menu.slots.get(slotId).getItem().isEmpty();
	}

	private static SlimeFlowProfile findAutoProfileForCurrentGui(Minecraft client) {
		String title = SlimeFlowState.getCurrentGuiTitle(client);

		for (SlimeFlowProfile profile : SlimeFlowState.profiles) {
			if (profile.autoRun && matchesGui(profile, title)) {
				return profile;
			}
		}

		return null;
	}

	private static boolean matchesGui(SlimeFlowProfile profile, String title) {
		if (profile.guiTitle == null || profile.guiTitle.isEmpty()) {
			return true;
		}

		if (title == null) {
			return false;
		}

		return title.equalsIgnoreCase(profile.guiTitle);
	}
}
