package com.mybezo.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SlimeFlowStackRunner {
	private static final int ACTION_DELAY = 10;
	private static final int GUI_CLOSE_DELAY = 16;
	private static final int CLICK_DELAY = 10;
	private static final int FAIL_TIMEOUT = 70;
	private static final int CLOSE_WAIT_TIMEOUT = 40;

	private static final double REACH_DISTANCE_SQR = 18.0;
	private static final double Y_TOLERANCE = 0.20;
	private static final double HORIZONTAL_TOLERANCE = 0.55;
	private static final double HORIZONTAL_START_DISTANCE = 0.72;

	private static boolean waitingForGuiClose = false;
	private static int closeWaitTicks = 0;
	private static int openFaceIndex = 0;
	private static int smoothDescendTicks = 0;
	private static double selectionAnchorPlayerX = 0.0;
	private static double selectionAnchorPlayerZ = 0.0;
	private static BlockPos selectionAnchorBlock = null;
	private static double lastSelectionSideError = -1.0;
	private static int lastSelectionSteerSign = 0;
	private static int sideErrorGrowingTicks = 0;
	private static boolean selectionSteerInverted = false;

	private static final Direction[] OPEN_FACES = new Direction[] {
			Direction.NORTH,
			Direction.SOUTH,
			Direction.EAST,
			Direction.WEST,
			Direction.UP
	};

	private SlimeFlowStackRunner() {
	}

	public static void recordMachineTarget(Minecraft client, BlockHitResult hit) {
		if (client == null || client.player == null || hit == null) {
			return;
		}

		BlockPos pos = hit.getBlockPos();
		SlimeFlowStackTarget target = new SlimeFlowStackTarget(
				pos,
				client.player.getX(),
				client.player.getY(),
				client.player.getZ(),
				hit.getDirection()
		);

		// If the same machine gets targeted again, update its stand position instead of adding a duplicate.
		for (int i = 0; i < SlimeFlowState.stackTargets.size(); i++) {
			SlimeFlowStackTarget old = SlimeFlowState.stackTargets.get(i);
			if (old != null && old.x == pos.getX() && old.y == pos.getY() && old.z == pos.getZ()) {
				SlimeFlowState.stackTargets.set(i, target);
				SlimeFlowConfig.save();
				return;
			}
		}

		SlimeFlowState.stackTargets.add(target);
		SlimeFlowState.stackSelectionPos1 = null;
		SlimeFlowState.stackSelectionPos2 = null;
		SlimeFlowConfig.save();
	}

	public static void startFromCrosshair(Minecraft client) {
		SlimeFlowState.clearHardStop();

		if (SlimeFlowState.hasStackSelection()) {
			startFromSelection(client);
			return;
		}

		if (!(client.hitResult instanceof BlockHitResult hit)) {
			return;
		}

		if (hit.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos basePos = hit.getBlockPos();

		SlimeFlowState.stackBasePos = basePos;
		SlimeFlowState.stackIndex = 0;
		SlimeFlowState.stackWaitTicks = 0;
		SlimeFlowState.stackGuiTicks = 0;
		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackPhase = 0;
		SlimeFlowState.stackRunning = true;
		SlimeFlowState.stackItemIndex = 0;

		SlimeFlowState.stackPlayerYOffset = client.player.getY() - basePos.getY();

		waitingForGuiClose = false;
		closeWaitTicks = 0;
		openFaceIndex = 0;
		smoothDescendTicks = 0;
		selectionAnchorBlock = null;
		selectionAnchorPlayerX = 0.0;
		selectionAnchorPlayerZ = 0.0;
		resetSelectionSteering();

		SlimeFlowState.autoRanThisGui = false;
		SlimeFlowState.autoRunDelayTicks = 5;
	}

	private static void startFromSelection(Minecraft client) {
		SlimeFlowState.clearHardStop();

		BlockPos first = getSelectionTarget(0);

		if (first == null) {
			return;
		}

		SlimeFlowState.stackBasePos = first;
		SlimeFlowState.stackIndex = 0;
		SlimeFlowState.stackWaitTicks = 0;
		SlimeFlowState.stackGuiTicks = 0;
		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackPhase = 0;
		SlimeFlowState.stackRunning = true;
		SlimeFlowState.stackItemIndex = 0;

		SlimeFlowState.stackPlayerYOffset = getRecordedTarget(0) != null ? getRecordedTarget(0).playerY - first.getY() : client.player.getY() - first.getY();

		waitingForGuiClose = false;
		closeWaitTicks = 0;
		openFaceIndex = 0;
		smoothDescendTicks = 0;
		selectionAnchorBlock = first;
		selectionAnchorPlayerX = client.player.getX();
		selectionAnchorPlayerZ = client.player.getZ();
		resetSelectionSteering();

		SlimeFlowState.autoRanThisGui = false;
		SlimeFlowState.autoRunDelayTicks = 5;
	}

	public static void stop(Minecraft client) {
		SlimeFlowState.stackRunning = false;
		SlimeFlowState.stackBasePos = null;
		SlimeFlowState.stackIndex = 0;
		SlimeFlowState.stackWaitTicks = 0;
		SlimeFlowState.stackGuiTicks = 0;
		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackPhase = 0;
		SlimeFlowState.stackPlayerYOffset = 0.0;
		SlimeFlowState.stackItemIndex = 0;

		waitingForGuiClose = false;
		closeWaitTicks = 0;
		openFaceIndex = 0;
		smoothDescendTicks = 0;
		selectionAnchorBlock = null;
		selectionAnchorPlayerX = 0.0;
		selectionAnchorPlayerZ = 0.0;
		resetSelectionSteering();

		SlimeFlowState.stopMacroRuntimeState();

		releaseMovement(client);
	}

	public static void tick(Minecraft client) {
		if (SlimeFlowState.macroHardStopped) {
			SlimeFlowState.stackRunning = false;
			return;
		}

		if (!SlimeFlowState.stackRunning) {
			return;
		}

		if (client.player == null || client.level == null || client.gameMode == null) {
			stop(client);
			return;
		}

		if (SlimeFlowBackpackRefillRunner.isRunning()) {
			releaseMovement(client);
			return;
		}

		if (getCurrentTarget() == null) {
			stop(client);
			return;
		}

		if (SlimeFlowState.stackIndex >= getTargetCount()) {
			stop(client);
			return;
		}

		if (waitingForGuiClose) {
			waitForGuiToClose(client);
			return;
		}

		if (client.screen instanceof AbstractContainerScreen<?>) {
			handleOpenGui(client);
			return;
		}

		SlimeFlowState.stackGuiTicks = 0;

		if (SlimeFlowState.stackWaitTicks > 0) {
			SlimeFlowState.stackWaitTicks--;
			return;
		}

		if (SlimeFlowState.stackClickMode) {
			tickClickBlock(client);
		} else {
			tickOpenGui(client);
		}
	}

	private static void tickOpenGui(Minecraft client) {
		BlockPos targetPos = getCurrentTarget();

		if (targetPos == null) {
			stop(client);
			return;
		}

		if (client.level.getBlockState(targetPos).isAir()) {
			if (SlimeFlowState.hasStackSelection()) {
				nextStack();
				return;
			}

			stop(client);
			return;
		}

		if (!moveToTargetYAndReach(client, targetPos)) {
			return;
		}

		releaseMovement(client);

		openBlock(client, targetPos);

		SlimeFlowState.stackOpenFailTicks++;

		if (SlimeFlowState.stackOpenFailTicks > FAIL_TIMEOUT) {
			nextStack();
		}
	}

	private static void tickClickBlock(Minecraft client) {
		BlockPos targetPos = getCurrentTarget();

		if (targetPos == null) {
			stop(client);
			return;
		}

		if (client.level.getBlockState(targetPos).isAir()) {
			if (SlimeFlowState.hasStackSelection()) {
				nextStack();
				return;
			}

			stop(client);
			return;
		}

		if (!moveToTargetYAndReach(client, targetPos)) {
			return;
		}

		releaseMovement(client);

		if (!prepareClickItem(client)) {
			stop(client);
			return;
		}

		clickBlock(client, targetPos);

		SlimeFlowState.stackWaitTicks = CLICK_DELAY;
		nextStack();
	}

	private static void handleOpenGui(Minecraft client) {
		releaseMovement(client);

		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackGuiTicks++;

		if (SlimeFlowState.stackGuiTicks < GUI_CLOSE_DELAY) {
			return;
		}

		if (!SlimeFlowState.clickQueue.isEmpty()) {
			return;
		}

		client.player.closeContainer();

		waitingForGuiClose = true;
		closeWaitTicks = 0;
		SlimeFlowState.stackWaitTicks = 0;
	}

	private static void waitForGuiToClose(Minecraft client) {
		releaseMovement(client);

		closeWaitTicks++;

		if (client.screen instanceof AbstractContainerScreen<?>) {
			if (closeWaitTicks > CLOSE_WAIT_TIMEOUT) {
				waitingForGuiClose = false;
				nextStack();
			}

			return;
		}

		waitingForGuiClose = false;
		closeWaitTicks = 0;
		nextStack();
	}

	public static void retryCurrentAfterBackpack(Minecraft client) {
		if (!SlimeFlowState.stackRunning) {
			return;
		}

		SlimeFlowState.stackGuiTicks = 0;
		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackPhase = 0;
		SlimeFlowState.stackWaitTicks = ACTION_DELAY;

		waitingForGuiClose = false;
		closeWaitTicks = 0;
		openFaceIndex = 0;
		smoothDescendTicks = 0;
		resetSelectionSteering();

		SlimeFlowState.autoRanThisGui = false;
		SlimeFlowState.autoRunDelayTicks = 5;

		releaseMovement(client);
	}

	private static void nextStack() {
		SlimeFlowState.stackGuiTicks = 0;
		SlimeFlowState.stackOpenFailTicks = 0;
		SlimeFlowState.stackPhase = 0;
		SlimeFlowState.stackIndex++;
		SlimeFlowState.stackWaitTicks = ACTION_DELAY;

		openFaceIndex = 0;
		smoothDescendTicks = 0;
		resetSelectionSteering();

		SlimeFlowState.autoRanThisGui = false;
		SlimeFlowState.autoRunDelayTicks = 5;
	}

	private static BlockPos getCurrentTarget() {
		if (SlimeFlowState.hasStackSelection()) {
			return getSelectionTarget(SlimeFlowState.stackIndex);
		}

		if (SlimeFlowState.stackBasePos == null) {
			return null;
		}

		return SlimeFlowState.stackBasePos.above(SlimeFlowState.stackIndex);
	}

	private static int getTargetCount() {
		int height = Math.max(1, SlimeFlowState.stackLimit);

		if (SlimeFlowState.hasStackRecordedTargets()) {
			long total = (long) SlimeFlowState.stackTargets.size() * (long) height;
			return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
		}

		if (!SlimeFlowState.hasStackSelection()) {
			return height;
		}

		BlockPos p1 = SlimeFlowState.stackSelectionPos1;
		BlockPos p2 = SlimeFlowState.stackSelectionPos2;

		int dx = Math.abs(p1.getX() - p2.getX()) + 1;
		int dz = Math.abs(p1.getZ() - p2.getZ()) + 1;

		long total = (long) dx * (long) dz * (long) height;
		return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
	}

	private static BlockPos getSelectionTarget(int index) {
		if (SlimeFlowState.hasStackRecordedTargets()) {
			SlimeFlowStackTarget target = getRecordedTarget(index);
			if (target == null) {
				return null;
			}
			return target.blockPos().above(getRecordedYStep(index));
		}

		if (!SlimeFlowState.hasStackSelection()) {
			return null;
		}

		BlockPos p1 = SlimeFlowState.stackSelectionPos1;
		BlockPos p2 = SlimeFlowState.stackSelectionPos2;

		int stepX = Integer.compare(p2.getX(), p1.getX());
		int stepZ = Integer.compare(p2.getZ(), p1.getZ());

		int dx = Math.abs(p1.getX() - p2.getX()) + 1;
		int dz = Math.abs(p1.getZ() - p2.getZ()) + 1;

		int baseY = Math.min(p1.getY(), p2.getY());
		int height = Math.max(1, SlimeFlowState.stackLimit);
		int columns = dx * dz;

		if (columns <= 0) {
			return null;
		}

		int columnIndex = index / height;
		int yStep = index % height;

		if (columnIndex >= columns) {
			return null;
		}

		int zOffset = columnIndex / dx;
		int xOffset = columnIndex % dx;
		int x = p1.getX() + (stepX * xOffset);
		int z = p1.getZ() + (stepZ * zOffset);

		int y = (columnIndex % 2 == 0)
				? baseY + yStep
				: baseY + (height - 1 - yStep);

		return new BlockPos(x, y, z);
	}

	private static SlimeFlowStackTarget getRecordedTarget(int index) {
		if (!SlimeFlowState.hasStackRecordedTargets()) {
			return null;
		}

		int height = Math.max(1, SlimeFlowState.stackLimit);
		int targetIndex = index / height;
		if (targetIndex < 0 || targetIndex >= SlimeFlowState.stackTargets.size()) {
			return null;
		}
		return SlimeFlowState.stackTargets.get(targetIndex);
	}

	private static int getRecordedYStep(int index) {
		int height = Math.max(1, SlimeFlowState.stackLimit);
		return height <= 0 ? 0 : index % height;
	}

	private static boolean moveToTargetYAndReach(Minecraft client, BlockPos targetPos) {
		SlimeFlowStackTarget recordedTarget = getRecordedTarget(SlimeFlowState.stackIndex);
		double targetPlayerY = recordedTarget != null
				? recordedTarget.playerY + getRecordedYStep(SlimeFlowState.stackIndex)
				: targetPos.getY() + SlimeFlowState.stackPlayerYOffset;
		double playerY = client.player.getY();
		double deltaY = targetPlayerY - playerY;

		boolean verticalReady = Math.abs(deltaY) <= Y_TOLERANCE;
		boolean horizontalReady = recordedTarget != null
				? stayAtCurrentStandingPosition(client)
				: (SlimeFlowState.hasStackSelection() ? stayAtCurrentStandingPosition(client) : true);

		if (deltaY > Y_TOLERANCE) {
			client.options.keyJump.setDown(true);
			client.options.keyShift.setDown(false);
			smoothDescendTicks = 0;
		} else if (deltaY < -Y_TOLERANCE) {
			client.options.keyJump.setDown(false);

			// Smooth descent: hold sneak while far, pulse it once close.
			double over = -deltaY;
			smoothDescendTicks++;
			boolean holdShift = over > 0.65 || (smoothDescendTicks % 3) != 0;
			client.options.keyShift.setDown(holdShift);
		} else {
			client.options.keyJump.setDown(false);
			client.options.keyShift.setDown(false);
			smoothDescendTicks = 0;
		}

		if (!verticalReady || !horizontalReady) {
			return false;
		}

		Vec3 center = new Vec3(
				targetPos.getX() + 0.5,
				targetPos.getY() + 0.5,
				targetPos.getZ() + 0.5
		);

		double distance = client.player.position().distanceToSqr(center);

		if (distance > REACH_DISTANCE_SQR) {
			return false;
		}

		releaseHorizontalMovement(client);
		return true;
	}

	private static boolean stayAtCurrentStandingPosition(Minecraft client) {
		releaseHorizontalMovement(client);
		return true;
	}


	private static boolean moveToRecordedStandingPosition(Minecraft client, SlimeFlowStackTarget target) {
		if (client == null || client.player == null || client.options == null || target == null) {
			return true;
		}

		double dx = target.playerX - client.player.getX();
		double dz = target.playerZ - client.player.getZ();
		double distSqr = dx * dx + dz * dz;

		if (distSqr <= 0.10 * 0.10) {
			releaseHorizontalMovement(client);
			return true;
		}

		double yaw = Math.toRadians(client.player.getYRot());
		double forwardX = -Math.sin(yaw);
		double forwardZ = Math.cos(yaw);
		double rightX = Math.cos(yaw);
		double rightZ = Math.sin(yaw);
		double len = Math.sqrt(distSqr);
		double nx = dx / len;
		double nz = dz / len;
		double forward = nx * forwardX + nz * forwardZ;
		double right = nx * rightX + nz * rightZ;
		double threshold = 0.18;

		client.options.keyUp.setDown(forward > threshold);
		client.options.keyDown.setDown(forward < -threshold);
		client.options.keyRight.setDown(right > threshold);
		client.options.keyLeft.setDown(right < -threshold);
		return false;
	}


	private static boolean moveToSelectionStandingOffset(Minecraft client, BlockPos targetPos) {
		if (client == null || client.player == null || client.options == null) {
			return true;
		}

		if (selectionAnchorBlock == null) {
			selectionAnchorBlock = targetPos;
			selectionAnchorPlayerX = client.player.getX();
			selectionAnchorPlayerZ = client.player.getZ();
			resetSelectionSteering();
		}

		// Keep the player's distance from the machine; only the sideways offset changes.
		double targetPlayerX = selectionAnchorPlayerX + (targetPos.getX() - selectionAnchorBlock.getX());
		double targetPlayerZ = selectionAnchorPlayerZ + (targetPos.getZ() - selectionAnchorBlock.getZ());
		double dx = targetPlayerX - client.player.getX();
		double dz = targetPlayerZ - client.player.getZ();
		double distSqr = dx * dx + dz * dz;

		if (distSqr <= 0.12 * 0.12) {
			releaseHorizontalMovement(client);
			resetSelectionSteering();
			return true;
		}

		double yaw = Math.toRadians(client.player.getYRot());
		double rightX = Math.cos(yaw);
		double rightZ = Math.sin(yaw);
		double side = dx * rightX + dz * rightZ;
		double absSide = Math.abs(side);

		client.options.keyUp.setDown(false);
		client.options.keyDown.setDown(false);

		if (absSide <= 0.08) {
			client.options.keyLeft.setDown(false);
			client.options.keyRight.setDown(false);
			resetSelectionSteering();
			return true;
		}

		int desiredSign = side > 0.0 ? 1 : -1;
		int steerSign = selectionSteerInverted ? -desiredSign : desiredSign;

		// Auto-flip steering direction if the side error keeps growing.
		if (lastSelectionSteerSign == steerSign && lastSelectionSideError >= 0.0 && absSide > lastSelectionSideError + 0.035) {
			sideErrorGrowingTicks++;
			if (sideErrorGrowingTicks >= 5) {
				selectionSteerInverted = !selectionSteerInverted;
				steerSign = -steerSign;
				sideErrorGrowingTicks = 0;
			}
		} else {
			sideErrorGrowingTicks = 0;
		}

		lastSelectionSideError = absSide;
		lastSelectionSteerSign = steerSign;

		client.options.keyRight.setDown(steerSign > 0);
		client.options.keyLeft.setDown(steerSign < 0);
		return false;
	}

	private static void resetSelectionSteering() {
		lastSelectionSideError = -1.0;
		lastSelectionSteerSign = 0;
		sideErrorGrowingTicks = 0;
		selectionSteerInverted = false;
	}

	private static boolean moveToTargetXZ(Minecraft client, BlockPos targetPos) {
		double targetX = targetPos.getX() + 0.5;
		double targetZ = targetPos.getZ() + 0.5;
		double dx = targetX - client.player.getX();
		double dz = targetZ - client.player.getZ();
		double distSqr = dx * dx + dz * dz;

		if (distSqr <= HORIZONTAL_TOLERANCE * HORIZONTAL_TOLERANCE) {
			releaseHorizontalMovement(client);
			return true;
		}

		if (distSqr <= HORIZONTAL_START_DISTANCE * HORIZONTAL_START_DISTANCE) {
			releaseHorizontalMovement(client);
			return true;
		}

		if (client.options == null) {
			return false;
		}

		double yaw = Math.toRadians(client.player.getYRot());
		double forwardX = -Math.sin(yaw);
		double forwardZ = Math.cos(yaw);
		double rightX = Math.cos(yaw);
		double rightZ = Math.sin(yaw);

		double len = Math.sqrt(distSqr);
		double nx = dx / len;
		double nz = dz / len;
		double forward = nx * forwardX + nz * forwardZ;
		double right = nx * rightX + nz * rightZ;
		double threshold = 0.25;

		client.options.keyUp.setDown(forward > threshold);
		client.options.keyDown.setDown(forward < -threshold);
		client.options.keyRight.setDown(right > threshold);
		client.options.keyLeft.setDown(right < -threshold);

		return false;
	}

	private static void releaseMovement(Minecraft client) {
		if (client == null || client.options == null) {
			return;
		}

		client.options.keyJump.setDown(false);
		client.options.keyShift.setDown(false);
		releaseHorizontalMovement(client);
	}

	private static void releaseHorizontalMovement(Minecraft client) {
		if (client == null || client.options == null) {
			return;
		}

		client.options.keyUp.setDown(false);
		client.options.keyDown.setDown(false);
		client.options.keyLeft.setDown(false);
		client.options.keyRight.setDown(false);
	}

	private static void openBlock(Minecraft client, BlockPos pos) {
		releaseMovement(client);

		Direction face = getCurrentRecordedFace();
		if (face == null) {
			face = OPEN_FACES[openFaceIndex];

			openFaceIndex++;

			if (openFaceIndex >= OPEN_FACES.length) {
				openFaceIndex = 0;
			}
		}

		useOnBlock(client, pos, face, InteractionHand.MAIN_HAND);

		SlimeFlowState.stackWaitTicks = ACTION_DELAY;
	}

	private static Direction getCurrentRecordedFace() {
		SlimeFlowStackTarget target = getRecordedTarget(SlimeFlowState.stackIndex);
		return target == null ? null : target.direction();
	}

	private static boolean prepareClickItem(Minecraft client) {
		if (client == null || client.player == null) {
			return false;
		}

		if (SlimeFlowState.stackItemSlots.isEmpty()) {
			return true;
		}

		int size = SlimeFlowState.stackItemSlots.size();

		for (int tries = 0; tries < size; tries++) {
			if (SlimeFlowState.stackItemIndex < 0 || SlimeFlowState.stackItemIndex >= size) {
				SlimeFlowState.stackItemIndex = 0;
			}

			int hotbarSlot = SlimeFlowState.stackItemSlots.get(SlimeFlowState.stackItemIndex);

			if (hotbarSlot < 0 || hotbarSlot > 8) {
				SlimeFlowState.stackItemIndex++;
				continue;
			}

			if (!client.player.getInventory().getItem(hotbarSlot).isEmpty()) {
				client.player.getInventory().setSelectedSlot(hotbarSlot);
				return true;
			}

			SlimeFlowState.stackItemIndex++;
		}

		return false;
	}

	private static void clickBlock(Minecraft client, BlockPos pos) {
		releaseMovement(client);

		Direction face = getCurrentRecordedFace();
		if (face == null) {
			face = Direction.NORTH;
		}

		if (SlimeFlowState.stackClickLeft) {
			leftClickBlock(client, pos, face);
		} else {
			useOnBlock(client, pos, face, InteractionHand.MAIN_HAND);
		}

		SlimeFlowState.stackWaitTicks = CLICK_DELAY;
	}

	private static void leftClickBlock(Minecraft client, BlockPos pos, Direction face) {
		client.gameMode.startDestroyBlock(pos, face == null ? Direction.NORTH : face);
		client.player.swing(InteractionHand.MAIN_HAND);
	}

	private static void useOnBlock(Minecraft client, BlockPos pos, Direction face, InteractionHand hand) {
		Vec3 hitVec = hitVecForFace(pos, face);

		BlockHitResult hit = new BlockHitResult(
				hitVec,
				face,
				pos,
				false
		);

		client.gameMode.useItemOn(
				client.player,
				hand,
				hit
		);
	}

	private static Vec3 hitVecForFace(BlockPos pos, Direction face) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;

		if (face == Direction.NORTH) {
			z = pos.getZ();
		} else if (face == Direction.SOUTH) {
			z = pos.getZ() + 1.0;
		} else if (face == Direction.EAST) {
			x = pos.getX() + 1.0;
		} else if (face == Direction.WEST) {
			x = pos.getX();
		} else if (face == Direction.UP) {
			y = pos.getY() + 1.0;
		} else if (face == Direction.DOWN) {
			y = pos.getY();
		}

		return new Vec3(x, y, z);
	}
}
