package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerInput;

public final class SlimeFlowState {
	static final ArrayDeque<ClickAction> clickQueue = new ArrayDeque<>();
	static final List<SlimeFlowProfile> profiles = new ArrayList<>();

	static boolean autoLoginEnabled = false;
	static String autoLoginPassword = "";
	static int autoLoginDelayTicks = 60;
	static int autoLoginCountdown = -1;
	static boolean autoLoginSent = false;
	static Object autoLoginConnectionRef = null;
	static boolean autoLoginEditingPassword = false;
	static boolean autoLoginShowPassword = false;
	static int autoLoginEditField = 0;
	static String autoLoginDraftPassword = "";
	static boolean autoLoginPerServer = false;
	static String autoLoginServerAddress = "";
	static String autoLoginServerPassword = "";
	static String autoLoginDraftServerAddress = "";

	static final List<SlimeFlowAutoLoginEntry> autoLoginEntries = new ArrayList<>();
	static int autoLoginEditingIndex = -1;
	static SlimeFlowAutoLoginEntry autoLoginEditingEntry = null;
	static int autoLoginServerCountdown = -1;
	static boolean autoLoginServerSent = false;
	static int autoLoginServerAttempts = 0;
	static final Set<String> autoLoginJoinedTargets = new HashSet<>();
	static int autoLoginMissingPlayerTicks = 0;
	static String autoLoginLastPhysicalServer = "";


	// Kept for compatibility with older panel files.
	static boolean autoReconnectEnabled = false;
	static int autoReconnectDelayTicks = 100;
	static boolean autoReconnectUnlimited = true;
	static int autoReconnectRetryCount = 3;
	static boolean backpackPreviewEnabled = false;
	static boolean shulkerPreviewEnabled = false;

	static final int DEFAULT_KEY_OPEN_UI = GLFW.GLFW_KEY_F12;
	static final int DEFAULT_KEY_STOP_ALL = GLFW.GLFW_KEY_F10;
	static final int DEFAULT_KEY_START_STACK = GLFW.GLFW_KEY_F9;
	static final int KEYBIND_NONE = 0;
	static final int KEYBIND_OPEN_UI = 1;
	static final int KEYBIND_STOP_ALL = 2;
	static final int KEYBIND_START_STACK = 3;
	static final int KEYBIND_AUTO_SELL = 4;
	static final int KEYBIND_AUTO_COMMAND = 5;
	static int keyOpenUi = DEFAULT_KEY_OPEN_UI;
	static int keyStopAll = DEFAULT_KEY_STOP_ALL;
	static int keyStartStack = DEFAULT_KEY_START_STACK;
	static int autoSellKeybind = KEYBIND_NONE;
	static int keybindEditingAction = KEYBIND_NONE;
	static int autoCommandEditingKeyIndex = -1;

	static int discordLinkVisibleTicks = 0;

	static final int AUTO_SELL_FIELD_NONE = 0;
	static final int AUTO_SELL_FIELD_ITEM = 1;
	static final int AUTO_SELL_FIELD_COMMAND = 2;
	static final int AUTO_SELL_UNIT_MS = 0;
	static final int AUTO_SELL_UNIT_SECONDS = 1;
	static final int AUTO_SELL_UNIT_MINUTES = 2;
	static final int AUTO_SELL_UNIT_HOURS = 3;
	static boolean autoSellEnabled = false;
	static String autoSellItemName = "";
	static String autoSellCommand = "sell all";
	static int autoSellIntervalAmount = 5;
	static int autoSellIntervalUnit = AUTO_SELL_UNIT_SECONDS;
	static int autoSellCooldownTicks = 0;
	static int autoSellEditField = AUTO_SELL_FIELD_NONE;
	static boolean autoSellCommandSentWhileFull = false;

	static final int AUTO_COMMAND_FIELD_NONE = 0;
	static final int AUTO_COMMAND_FIELD_NAME = 1;
	static final int AUTO_COMMAND_FIELD_COMMAND = 2;
	static final List<SlimeFlowAutoCommandEntry> autoCommandEntries = new ArrayList<>();
	static int autoCommandEditingIndex = -1;
	static SlimeFlowAutoCommandEntry autoCommandEditingEntry = null;
	static int autoCommandEditField = AUTO_COMMAND_FIELD_NONE;

	static int lastContainerId = -999;
	static boolean autoRanThisGui = false;
	static boolean macroHardStopped = false;
	static int hardStoppedContainerId = -999;
	static int autoRunDelayTicks = -1;

	static int delayTicks = 0;
	static int actionsPerTick = SlimeFlowProfile.MIN_ACTIONS_PER_TICK;
	static int clickCooldownTicks = 0;
	static SlimeFlowProfile runningProfile = null;

	static boolean outputCollectActive = false;
	static int outputCollectContainerId = -999;
	static final List<Integer> outputCollectSlots = new ArrayList<>();

	static int uiX = Integer.MIN_VALUE;
	static int uiY = 45;
	static boolean uiDragging = false;
	static int uiDragOffsetX = 0;
	static int uiDragOffsetY = 0;

	static boolean stackRunning = false;
	static boolean stackClickMode = false;
	static boolean stackClickLeft = false;
	static BlockPos stackBasePos = null;
	static BlockPos stackSelectionPos1 = null;
	static BlockPos stackSelectionPos2 = null;
	static final List<SlimeFlowStackTarget> stackTargets = new ArrayList<>();

	static int stackIndex = 0;
	static int stackLimit = 64;
	static final List<Integer> stackItemSlots = new ArrayList<>();
	static int stackItemIndex = 0;
	static int stackWaitTicks = 0;
	static int stackGuiTicks = 0;
	static int stackOpenFailTicks = 0;
	static int stackPhase = 0;

	static double stackPlayerYOffset = 0.0;


	static OverlayMode overlayMode = OverlayMode.LIST;
	static SlimeFlowProfile editingProfile = null;
	static int editingProfileIndex = -1;

	static DraftType draftType = DraftType.MOVE;
	static boolean draftTypeMenuOpen = false;
	static int draftFromSlot = -1;
	static int draftToSlot = -1;
	static int draftAmount = 1;

	static int draftClickSlot = -1;
	static int draftClickButton = 0;
	static int draftClickTimes = 1;

	static String draftItemName = "";
	static int draftItemTargetSlot = -1;
	static int draftItemAmount = 1;

	static final List<String> draftMultiItemNames = new ArrayList<>();
	static int draftMultiTargetSlot = -1;
	static int draftMultiAmount = 1;

	static int draftOutputSlot = -1;

	static PickMode pickMode = PickMode.NONE;
	static boolean pickSnapshotActive = false;
	static SlimeFlowProfile pickSnapshotProfile = null;
	static final List<SlimeFlowProfile.Row> pickSnapshotRows = new ArrayList<>();
	static int pickSnapshotFromSlot = -1;
	static int pickSnapshotToSlot = -1;
	static int pickSnapshotAmount = 1;
	static int pickSnapshotClickSlot = -1;
	static int pickSnapshotClickButton = 0;
	static int pickSnapshotClickTimes = 1;
	static String pickSnapshotItemName = "";
	static int pickSnapshotItemTargetSlot = -1;
	static int pickSnapshotItemAmount = 1;
	static final List<String> pickSnapshotMultiItemNames = new ArrayList<>();
	static int pickSnapshotMultiTargetSlot = -1;
	static int pickSnapshotMultiAmount = 1;
	static int pickSnapshotOutputSlot = -1;
	static String pickSnapshotBackpackRefillItemName = "";
	static final List<Integer> pickSnapshotBackpackSlots = new ArrayList<>();
	static final List<String> pickSnapshotBackpackKeywords = new ArrayList<>();
	static String pickSnapshotAutoSellItemName = "";

	static SlimeFlowProfile backpackPausedProfile = null;
	static SlimeFlowProfile backpackResumeProfile = null;
	static int backpackResumeRow = 0;
	static int backpackResumeRowReady = -1;
	static int backpackStage = 0;
	static int backpackTicks = 0;
	static int backpackIndex = 0;
	static int backpackClicks = 0;
	static int backpackPreviousHotbarSlot = -1;
	static boolean backpackResumePending = false;
	static String backpackRequestedItemName = "";
	static final List<Integer> backpackAutoSlots = new ArrayList<>();
	static int backpackCurrentInventorySlot = -1;
	static int backpackCurrentSwappedSlot = -1;
	static final Set<Integer> backpackKnownEmptySlots = new HashSet<>();

	static void stopOutputCollectorState() {
		outputCollectActive = false;
		outputCollectContainerId = -999;
		outputCollectSlots.clear();
	}

	static void stopBackpackRefillState() {
		backpackPausedProfile = null;
		backpackResumeProfile = null;
		backpackResumeRow = 0;
		backpackResumeRowReady = -1;
		backpackStage = 0;
		backpackTicks = 0;
		backpackIndex = 0;
		backpackClicks = 0;
		backpackPreviousHotbarSlot = -1;
		backpackResumePending = false;
		backpackRequestedItemName = "";
		backpackAutoSlots.clear();
		backpackCurrentInventorySlot = -1;
		backpackCurrentSwappedSlot = -1;
	}

	static void stopMacroRuntimeState() {
		macroHardStopped = true;
		hardStoppedContainerId = getCurrentContainerId();
		clickQueue.clear();
		runningProfile = null;
		stopOutputCollectorState();
		stopBackpackRefillState();
		backpackKnownEmptySlots.clear();
		autoRanThisGui = true;
		autoRunDelayTicks = -1;
		lastContainerId = -999;
		pickMode = PickMode.NONE;
		clearPickSnapshot();
	}

	static void clearHardStop() {
		macroHardStopped = false;
		hardStoppedContainerId = -999;
	}

	private static int getCurrentContainerId() {
		Minecraft client = Minecraft.getInstance();

		if (client == null || client.player == null || client.player.containerMenu == null) {
			return -999;
		}

		return client.player.containerMenu.containerId;
	}

	private SlimeFlowState() {
	}

	public static void init() {
		SlimeFlowConfig.load();
	}

	enum OverlayMode {
		LIST,
		EDIT,
		LOGIN,
		MORE,
		AUTO_SELL,
		AUTO_COMMAND,
		KEYS,
		// Kept for compatibility with older panel classes.
		ACCOUNT,
		TWEAKS,
		RECONNECT,
		STORAGE
	}

	enum DraftType {
		MOVE,
		ITEM,
		CLICK,
		MULTI,
		OUTPUT
	}

	enum PickMode {
		NONE,
		MOVE_FROM,
		MOVE_TO,
		CLICK_SLOT,
		ITEM_NAME,
		ITEM_TO,
		MULTI_ITEM,
		MULTI_TO,
		OUTPUT_SLOT,
		STACK_ITEM,
		BACKPACK_SLOT,
		BACKPACK_ITEM,
		REFILL_KEYWORD,
		AUTO_SELL_ITEM
	}

	static boolean hasStackSelection() {
		return hasStackRecordedTargets() || (stackSelectionPos1 != null && stackSelectionPos2 != null);
	}

	static boolean hasStackRecordedTargets() {
		return !stackTargets.isEmpty();
	}

	static String getCurrentGuiTitle(Minecraft client) {
		if (client.screen == null) {
			return "";
		}

		return client.screen.getTitle().getString();
	}

	static void resetDraft() {
		draftType = DraftType.MOVE;
		draftTypeMenuOpen = false;

		draftFromSlot = -1;
		draftToSlot = -1;
		draftAmount = 1;

		draftClickSlot = -1;
		draftClickButton = 0;
		draftClickTimes = 1;

		draftItemName = "";
		draftItemTargetSlot = -1;
		draftItemAmount = 1;

		draftMultiItemNames.clear();
		draftMultiTargetSlot = -1;
		draftMultiAmount = 1;

		draftOutputSlot = -1;

		pickMode = PickMode.NONE;
		clearPickSnapshot();
	}

	static void beginPick(PickMode mode) {
		if (mode == PickMode.NONE) {
			finishPick(true);
			return;
		}

		if (!pickSnapshotActive) {
			capturePickSnapshot();
		}

		pickMode = mode;
	}

	static void finishPick(boolean keep) {
		if (!keep) {
			restorePickSnapshot();
		}

		pickMode = PickMode.NONE;
		clearPickSnapshot();
	}

	private static void capturePickSnapshot() {
		pickSnapshotActive = true;
		pickSnapshotProfile = editingProfile;
		pickSnapshotRows.clear();

		if (editingProfile != null && editingProfile.rows != null) {
			for (SlimeFlowProfile.Row row : editingProfile.rows) {
				if (row != null) {
					pickSnapshotRows.add(SlimeFlowUi.copyRow(row));
				}
			}
		}

		pickSnapshotFromSlot = draftFromSlot;
		pickSnapshotToSlot = draftToSlot;
		pickSnapshotAmount = draftAmount;
		pickSnapshotClickSlot = draftClickSlot;
		pickSnapshotClickButton = draftClickButton;
		pickSnapshotClickTimes = draftClickTimes;
		pickSnapshotItemName = draftItemName;
		pickSnapshotItemTargetSlot = draftItemTargetSlot;
		pickSnapshotItemAmount = draftItemAmount;
		pickSnapshotMultiItemNames.clear();
		pickSnapshotMultiItemNames.addAll(draftMultiItemNames);
		pickSnapshotMultiTargetSlot = draftMultiTargetSlot;
		pickSnapshotMultiAmount = draftMultiAmount;
		pickSnapshotOutputSlot = draftOutputSlot;

		pickSnapshotBackpackRefillItemName = editingProfile == null ? "" : editingProfile.backpackRefillItemName;
		pickSnapshotBackpackSlots.clear();
		pickSnapshotBackpackKeywords.clear();
		if (editingProfile != null) {
			if (editingProfile.backpackSlots != null) {
				pickSnapshotBackpackSlots.addAll(editingProfile.backpackSlots);
			}
			if (editingProfile.backpackRefillKeywords != null) {
				pickSnapshotBackpackKeywords.addAll(editingProfile.backpackRefillKeywords);
			}
		}
		pickSnapshotAutoSellItemName = autoSellItemName;
	}

	private static void restorePickSnapshot() {
		if (pickSnapshotActive && pickSnapshotProfile != null && pickSnapshotProfile == editingProfile) {
			pickSnapshotProfile.rows.clear();
			for (SlimeFlowProfile.Row row : pickSnapshotRows) {
				pickSnapshotProfile.rows.add(SlimeFlowUi.copyRow(row));
			}

			pickSnapshotProfile.backpackRefillItemName = pickSnapshotBackpackRefillItemName;
			pickSnapshotProfile.backpackSlots.clear();
			pickSnapshotProfile.backpackSlots.addAll(pickSnapshotBackpackSlots);
			pickSnapshotProfile.backpackRefillKeywords.clear();
			pickSnapshotProfile.backpackRefillKeywords.addAll(pickSnapshotBackpackKeywords);
		}

		draftFromSlot = pickSnapshotFromSlot;
		draftToSlot = pickSnapshotToSlot;
		draftAmount = pickSnapshotAmount;
		draftClickSlot = pickSnapshotClickSlot;
		draftClickButton = pickSnapshotClickButton;
		draftClickTimes = pickSnapshotClickTimes;
		draftItemName = pickSnapshotItemName;
		draftItemTargetSlot = pickSnapshotItemTargetSlot;
		draftItemAmount = pickSnapshotItemAmount;
		draftMultiItemNames.clear();
		draftMultiItemNames.addAll(pickSnapshotMultiItemNames);
		draftMultiTargetSlot = pickSnapshotMultiTargetSlot;
		draftMultiAmount = pickSnapshotMultiAmount;
		draftOutputSlot = pickSnapshotOutputSlot;
		autoSellItemName = pickSnapshotAutoSellItemName;
	}

	static void clearPickSnapshot() {
		pickSnapshotActive = false;
		pickSnapshotProfile = null;
		pickSnapshotRows.clear();
		pickSnapshotMultiItemNames.clear();
		pickSnapshotBackpackSlots.clear();
		pickSnapshotBackpackKeywords.clear();
	}

	static void msg(Minecraft client, String text) {
		// Chat message dimatikan.
	}

	static record ClickAction(
			int slotId,
			int button,
			ContainerInput input,
			String itemName,
			List<String> itemNames,
			int itemTargetSlot,
			int itemAmount,
			int profileRowIndex
	) {
		ClickAction(int slotId, int button, ContainerInput input) {
			this(slotId, button, input, "", null, -1, 1, -1);
		}

		static ClickAction guardedMove(int fromSlot, int toSlot, int amount) {
			return new ClickAction(-997, fromSlot, ContainerInput.PICKUP, "", null, toSlot, Math.max(1, amount), -1);
		}

		static ClickAction dynamicItem(String itemName, int itemTargetSlot, int itemAmount, int profileRowIndex) {
			return new ClickAction(-998, 0, ContainerInput.PICKUP, itemName, null, itemTargetSlot, Math.max(1, itemAmount), profileRowIndex);
		}

		static ClickAction multiItem(List<String> itemNames, int itemTargetSlot, int itemAmount, int profileRowIndex) {
			return new ClickAction(-996, 0, ContainerInput.PICKUP, "", itemNames == null ? null : new ArrayList<>(itemNames), itemTargetSlot, Math.max(1, itemAmount), profileRowIndex);
		}

		static ClickAction output(int outputSlot) {
			return new ClickAction(-995, 0, ContainerInput.PICKUP, "", null, outputSlot, 1, -1);
		}
	}
}
