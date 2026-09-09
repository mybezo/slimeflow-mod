package com.mybezo.macro;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class SlimeFlowBackpackRefillRunner {
	private static final int STAGE_OPEN_BACKPACK = 1;
	private static final int STAGE_WAIT_BACKPACK = 2;
	private static final int STAGE_TAKE_ITEMS = 3;

	private static final String BACKPACK_WORD_KEY = "backpack";
	private static final String REFILL_WORD_KEY = "refill";
	private static final int DEFAULT_SCAN_LIMIT = 36;

	private SlimeFlowBackpackRefillRunner() {
	}

	public static boolean isRunning() {
		return SlimeFlowState.backpackPausedProfile != null;
	}

	public static boolean start(Minecraft client, SlimeFlowProfile profile, int resumeRow, String requestedItemName) {
		if (client == null || client.player == null || client.gameMode == null || profile == null) {
			return false;
		}

		if (!profile.backpackRefillEnabled) {
			return false;
		}

		String itemName = requestedItemName == null ? "" : requestedItemName.trim();

		if (itemName.isEmpty()) {
			return false;
		}

		List<Integer> backpacks = findAutoBackpacks(client, profile);

		if (backpacks.isEmpty()) {
			return false;
		}

		SlimeFlowState.clearHardStop();
		SlimeFlowState.clickQueue.clear();

		SlimeFlowState.backpackPausedProfile = profile;
		SlimeFlowState.backpackResumeRow = Math.max(0, resumeRow);
		SlimeFlowState.backpackStage = STAGE_OPEN_BACKPACK;
		SlimeFlowState.backpackTicks = 10;
		SlimeFlowState.backpackIndex = 0;
		SlimeFlowState.backpackClicks = 0;
		SlimeFlowState.backpackRequestedItemName = itemName;
		SlimeFlowState.backpackPreviousHotbarSlot = client.player.getInventory().getSelectedSlot();
		SlimeFlowState.backpackAutoSlots.clear();
		SlimeFlowState.backpackAutoSlots.addAll(backpacks);
		SlimeFlowState.backpackCurrentInventorySlot = -1;
		SlimeFlowState.backpackCurrentSwappedSlot = -1;
		SlimeFlowState.backpackAnyItemsCollected = false;

		if (client.screen != null) {
			client.player.closeContainer();
		}

		return true;
	}

	public static void tick(Minecraft client) {
		if (SlimeFlowState.macroHardStopped) {
			stop(client);
			return;
		}

		if (!isRunning()) {
			return;
		}

		if (client == null || client.player == null || client.gameMode == null) {
			stop(client);
			return;
		}

		if (SlimeFlowState.backpackTicks > 0) {
			SlimeFlowState.backpackTicks--;
			return;
		}

		if (SlimeFlowState.backpackStage == STAGE_OPEN_BACKPACK) {
			openCurrentBackpack(client);
			SlimeFlowState.backpackStage = STAGE_WAIT_BACKPACK;
			SlimeFlowState.backpackTicks = 14;
			return;
		}

		if (SlimeFlowState.backpackStage == STAGE_WAIT_BACKPACK) {
			if (client.screen instanceof AbstractContainerScreen<?>) {
				SlimeFlowState.backpackStage = STAGE_TAKE_ITEMS;
				SlimeFlowState.backpackTicks = 5;
				return;
			}

			openCurrentBackpack(client);
			SlimeFlowState.backpackTicks = 14;
			return;
		}

		if (SlimeFlowState.backpackStage == STAGE_TAKE_ITEMS) {
			takeItems(client);
		}
	}

	private static void openCurrentBackpack(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null) {
			stopNoResources(client);
			return;
		}

		restoreTemporarySwap(client);

		if (!moveToNextUsableBackpack()) {
			stopOutOfResources(client);
			return;
		}

		int inventorySlot = SlimeFlowState.backpackAutoSlots.get(SlimeFlowState.backpackIndex);

		if (inventorySlot < 0 || inventorySlot >= DEFAULT_SCAN_LIMIT) {
			markCurrentBackpackEmpty();
			nextBackpack(client);
			return;
		}

		SlimeFlowState.backpackCurrentInventorySlot = inventorySlot;

		int hotbarSlot = inventorySlot;

		if (inventorySlot > 8) {
			hotbarSlot = getSafeHotbarSlot(player);

			if (!swapInventorySlotToHotbar(client, inventorySlot, hotbarSlot)) {
				markCurrentBackpackEmpty();
				nextBackpack(client);
				return;
			}

			SlimeFlowState.backpackCurrentSwappedSlot = inventorySlot;
		}

		player.getInventory().setSelectedSlot(hotbarSlot);
		client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		player.swing(InteractionHand.MAIN_HAND);
	}

	private static void takeItems(Minecraft client) {
		if (!(client.screen instanceof AbstractContainerScreen<?>)) {
			SlimeFlowState.backpackStage = STAGE_OPEN_BACKPACK;
			SlimeFlowState.backpackTicks = 8;
			return;
		}

		if (client.player == null || client.gameMode == null) {
			stop(client);
			return;
		}

		AbstractContainerMenu menu = client.player.containerMenu;

		if (menu == null) {
			nextBackpack(client);
			return;
		}

		if (isInventoryFull(menu)) {
			finishAndRetry(client);
			return;
		}

		int sourceSlot = SlimeFlowItemFinder.findNonPlayerSlotByName(menu, SlimeFlowState.backpackRequestedItemName);

		if (sourceSlot < 0) {
			markCurrentBackpackEmpty();
			nextBackpack(client);
			return;
		}

		client.gameMode.handleContainerInput(menu.containerId, sourceSlot, 0, ContainerInput.QUICK_MOVE, client.player);
		SlimeFlowState.backpackAnyItemsCollected = true;

		SlimeFlowState.backpackClicks++;
		SlimeFlowState.backpackTicks = 5;

		if (SlimeFlowState.backpackClicks >= 36) {
			finishAndRetry(client);
		}
	}

	private static void nextBackpack(Minecraft client) {
		if (client != null && client.player != null && client.screen != null) {
			client.player.closeContainer();
		}

		restoreTemporarySwap(client);

		SlimeFlowState.backpackIndex++;
		SlimeFlowState.backpackClicks = 0;
		SlimeFlowState.backpackStage = STAGE_OPEN_BACKPACK;
		SlimeFlowState.backpackTicks = 10;

		if (!moveToNextUsableBackpack()) {
			stopOutOfResources(client);
		}
	}

	private static boolean moveToNextUsableBackpack() {
		if (SlimeFlowState.backpackAutoSlots == null || SlimeFlowState.backpackAutoSlots.isEmpty()) {
			return false;
		}

		while (SlimeFlowState.backpackIndex < SlimeFlowState.backpackAutoSlots.size()) {
			int inventorySlot = SlimeFlowState.backpackAutoSlots.get(SlimeFlowState.backpackIndex);

			if (!SlimeFlowState.backpackKnownEmptySlots.contains(inventorySlot)) {
				return true;
			}

			SlimeFlowState.backpackIndex++;
		}

		return false;
	}

	private static void markCurrentBackpackEmpty() {
		if (SlimeFlowState.backpackAutoSlots == null || SlimeFlowState.backpackIndex < 0 || SlimeFlowState.backpackIndex >= SlimeFlowState.backpackAutoSlots.size()) {
			return;
		}

		int inventorySlot = SlimeFlowState.backpackAutoSlots.get(SlimeFlowState.backpackIndex);

		if (inventorySlot >= 0 && inventorySlot < DEFAULT_SCAN_LIMIT) {
			SlimeFlowState.backpackKnownEmptySlots.add(inventorySlot);
		}
	}

	/** Picks the right way to bail when no more backpack items are available. */
	private static void stopOutOfResources(Minecraft client) {
		if (SlimeFlowState.backpackAnyItemsCollected) {
			// Already pulled some items this session (just not enough to
			// fill the inventory) - carry on with what we have instead of
			// aborting the whole macro.
			finishAndRetry(client);
		} else {
			stopNoResources(client);
		}
	}

	private static void stopNoResources(Minecraft client) {
		restoreTemporarySwap(client);
		restorePreviousHotbar(client);

		if (client != null && client.player != null && client.screen != null) {
			client.player.closeContainer();
		}

		SlimeFlowStackRunner.stop(client);
		SlimeFlowState.stopMacroRuntimeState();
		stopOnlyState();
	}

	private static void finishAndRetry(Minecraft client) {
		SlimeFlowProfile profile = SlimeFlowState.backpackPausedProfile;
		int resumeRow = SlimeFlowState.backpackResumeRow;

		if (profile != null) {
			SlimeFlowState.backpackResumeProfile = profile;
			SlimeFlowState.backpackResumeRowReady = Math.max(0, resumeRow);
			SlimeFlowState.backpackResumePending = true;
		}

		restoreTemporarySwap(client);
		restorePreviousHotbar(client);

		if (client != null && client.player != null && client.screen != null) {
			client.player.closeContainer();
		}

		SlimeFlowStackRunner.retryCurrentAfterBackpack(client);
		stopOnlyState();
	}

	public static void stop(Minecraft client) {
		restoreTemporarySwap(client);
		restorePreviousHotbar(client);
		stopOnlyState();
	}

	public static void stopFromButton(Minecraft client) {
		boolean wasRunning = isRunning();

		restoreTemporarySwap(client);
		restorePreviousHotbar(client);

		SlimeFlowState.clickQueue.clear();
		SlimeFlowState.runningProfile = null;
		SlimeFlowState.stopBackpackRefillState();
		SlimeFlowState.macroHardStopped = true;
		SlimeFlowState.autoRanThisGui = true;
		SlimeFlowState.autoRunDelayTicks = -1;

		if (wasRunning && client != null && client.player != null && client.screen != null) {
			client.player.closeContainer();
		}
	}

	private static List<Integer> findAutoBackpacks(Minecraft client, SlimeFlowProfile profile) {
		List<Integer> result = new ArrayList<>();

		if (client == null || client.player == null) {
			return result;
		}

		for (int i = 0; i < DEFAULT_SCAN_LIMIT; i++) {
			ItemStack stack = client.player.getInventory().getItem(i);

			if (stack == null || stack.isEmpty()) {
				continue;
			}

			String itemName = SlimeFlowItemFinder.getItemName(stack);

			if (isAutoBackpackName(profile, itemName)) {
				result.add(i);
			}
		}

		return result;
	}

	static boolean isAutoBackpackName(String itemName) {
		return isAutoBackpackName(null, itemName);
	}

	static boolean isAutoBackpackName(SlimeFlowProfile profile, String itemName) {
		if (itemName == null) {
			return false;
		}

		String name = normalize(itemName);

		if (name.isEmpty()) {
			return false;
		}

		for (String keyword : getRefillKeywords(profile)) {
			String key = normalize(keyword);

			if (!key.isEmpty() && name.contains(key)) {
				return true;
			}
		}

		return false;
	}

	static List<String> getRefillKeywords(SlimeFlowProfile profile) {
		List<String> result = new ArrayList<>();

		if (profile != null && profile.backpackRefillKeywords != null) {
			for (String keyword : profile.backpackRefillKeywords) {
				addKeyword(result, keyword);
			}
		}

		if (result.isEmpty()) {
			result.add(BACKPACK_WORD_KEY);
			result.add(REFILL_WORD_KEY);
		}

		return result;
	}

	static String refillKeywordText(SlimeFlowProfile profile) {
		List<String> keywords = getRefillKeywords(profile);
		StringBuilder builder = new StringBuilder();

		for (String keyword : keywords) {
			if (builder.length() > 0) {
				builder.append(",");
			}

			builder.append(keyword);
		}

		return builder.toString();
	}

	static void setCustomRefillKeyword(SlimeFlowProfile profile, String keyword) {
		if (profile == null) {
			return;
		}

		if (profile.backpackRefillKeywords == null) {
			profile.backpackRefillKeywords = new ArrayList<>();
		}

		profile.backpackRefillKeywords.clear();
		addKeyword(profile.backpackRefillKeywords, keyword);
	}

	static void resetRefillKeywords(SlimeFlowProfile profile) {
		if (profile == null) {
			return;
		}

		if (profile.backpackRefillKeywords != null) {
			profile.backpackRefillKeywords.clear();
		}
	}

	private static void addKeyword(List<String> keywords, String keyword) {
		String cleaned = cleanKeyword(keyword);

		if (cleaned.isEmpty()) {
			return;
		}

		for (String existing : keywords) {
			if (existing != null && existing.equalsIgnoreCase(cleaned)) {
				return;
			}
		}

		keywords.add(cleaned);
	}

	private static String cleanKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}

		String cleaned = keyword.trim();

		while (cleaned.contains("  ")) {
			cleaned = cleaned.replace("  ", " ");
		}

		return cleaned;
	}

	private static String normalize(String text) {
		return text == null ? "" : text.trim().toLowerCase();
	}

	private static int getSafeHotbarSlot(LocalPlayer player) {
		int selected = player.getInventory().getSelectedSlot();

		if (selected >= 0 && selected <= 8) {
			return selected;
		}

		return 0;
	}

	private static boolean swapInventorySlotToHotbar(Minecraft client, int inventorySlot, int hotbarSlot) {
		if (client == null || client.player == null || client.gameMode == null) {
			return false;
		}

		int fromMenuSlot = inventoryIndexToPlayerMenuSlot(inventorySlot);
		int hotbarMenuSlot = inventoryIndexToPlayerMenuSlot(hotbarSlot);

		if (fromMenuSlot < 0 || hotbarMenuSlot < 0) {
			return false;
		}

		AbstractContainerMenu menu = client.player.inventoryMenu;

		if (menu == null) {
			return false;
		}

		client.gameMode.handleContainerInput(menu.containerId, fromMenuSlot, 0, ContainerInput.PICKUP, client.player);
		client.gameMode.handleContainerInput(menu.containerId, hotbarMenuSlot, 0, ContainerInput.PICKUP, client.player);
		client.gameMode.handleContainerInput(menu.containerId, fromMenuSlot, 0, ContainerInput.PICKUP, client.player);
		return true;
	}

	private static void restoreTemporarySwap(Minecraft client) {
		if (client == null || client.player == null || client.gameMode == null) {
			return;
		}

		int swappedSlot = SlimeFlowState.backpackCurrentSwappedSlot;

		if (swappedSlot < 9 || swappedSlot >= DEFAULT_SCAN_LIMIT) {
			SlimeFlowState.backpackCurrentSwappedSlot = -1;
			return;
		}

		int hotbarSlot = getSafeHotbarSlot(client.player);
		int fromMenuSlot = inventoryIndexToPlayerMenuSlot(hotbarSlot);
		int toMenuSlot = inventoryIndexToPlayerMenuSlot(swappedSlot);
		AbstractContainerMenu menu = client.player.inventoryMenu;

		if (menu != null && fromMenuSlot >= 0 && toMenuSlot >= 0) {
			client.gameMode.handleContainerInput(menu.containerId, fromMenuSlot, 0, ContainerInput.PICKUP, client.player);
			client.gameMode.handleContainerInput(menu.containerId, toMenuSlot, 0, ContainerInput.PICKUP, client.player);
			client.gameMode.handleContainerInput(menu.containerId, fromMenuSlot, 0, ContainerInput.PICKUP, client.player);
		}

		SlimeFlowState.backpackCurrentSwappedSlot = -1;
		SlimeFlowState.backpackCurrentInventorySlot = -1;
	}

	private static int inventoryIndexToPlayerMenuSlot(int inventoryIndex) {
		if (inventoryIndex < 0 || inventoryIndex >= DEFAULT_SCAN_LIMIT) {
			return -1;
		}

		if (inventoryIndex < 9) {
			return inventoryIndex + 36;
		}

		return inventoryIndex;
	}

	private static void restorePreviousHotbar(Minecraft client) {
		if (client == null || client.player == null) {
			return;
		}

		int previous = SlimeFlowState.backpackPreviousHotbarSlot;

		if (previous >= 0 && previous <= 8) {
			client.player.getInventory().setSelectedSlot(previous);
		}
	}

	private static boolean isInventoryFull(AbstractContainerMenu menu) {
		int start = Math.max(0, menu.slots.size() - 36);

		for (int i = start; i < menu.slots.size(); i++) {
			if (!menu.slots.get(i).hasItem()) {
				return false;
			}
		}

		return true;
	}

	private static void stopOnlyState() {
		SlimeFlowState.stopBackpackRefillState();
	}
}
