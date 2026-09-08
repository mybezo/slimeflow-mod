package com.mybezo.macro;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class SlimeFlowStackTarget {
	public int x;
	public int y;
	public int z;
	public double playerX;
	public double playerY;
	public double playerZ;
	public String face = "NORTH";

	public SlimeFlowStackTarget() {
	}

	public SlimeFlowStackTarget(BlockPos pos, double playerX, double playerY, double playerZ, Direction face) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.playerX = playerX;
		this.playerY = playerY;
		this.playerZ = playerZ;
		this.face = face == null ? "NORTH" : face.name();
	}

	public BlockPos blockPos() {
		return new BlockPos(x, y, z);
	}

	public Direction direction() {
		String value = face == null ? "" : face.trim();
		for (Direction direction : Direction.values()) {
			if (direction.name().equalsIgnoreCase(value)) {
				return direction;
			}
		}
		return Direction.NORTH;
	}

	public SlimeFlowStackTarget copy() {
		SlimeFlowStackTarget copy = new SlimeFlowStackTarget();
		copy.x = x;
		copy.y = y;
		copy.z = z;
		copy.playerX = playerX;
		copy.playerY = playerY;
		copy.playerZ = playerZ;
		copy.face = face == null ? "NORTH" : face;
		return copy;
	}
}
