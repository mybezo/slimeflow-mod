package com.mybezo.macro;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlimeFlowItemFinder {
	private SlimeFlowItemFinder() {
	}

	static int findSlotByName(AbstractContainerMenu menu, String itemName) {
		if (menu == null || itemName == null || itemName.isEmpty()) {
			return -1;
		}

		String wanted = normalize(itemName);

		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.equals(wanted)) {
				return i;
			}
		}

		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.contains(wanted) || wanted.contains(current)) {
				return i;
			}
		}

		return -1;
	}

	static int findNonPlayerSlotByName(AbstractContainerMenu menu, String itemName) {
		if (menu == null || itemName == null || itemName.isEmpty()) {
			return -1;
		}

		String wanted = normalize(itemName);
		int end = Math.max(0, menu.slots.size() - 36);

		for (int i = 0; i < end; i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.equals(wanted)) {
				return i;
			}
		}

		for (int i = 0; i < end; i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.contains(wanted) || wanted.contains(current)) {
				return i;
			}
		}

		return -1;
	}

	static int findPlayerInventorySlotByName(AbstractContainerMenu menu, String itemName) {
		if (menu == null || itemName == null || itemName.isEmpty()) {
			return -1;
		}

		String wanted = normalize(itemName);
		int start = Math.max(0, menu.slots.size() - 36);

		for (int i = start; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.equals(wanted)) {
				return i;
			}
		}

		for (int i = start; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			String current = normalize(getItemName(stack));

			if (current.contains(wanted) || wanted.contains(current)) {
				return i;
			}
		}

		return -1;
	}


	static int findEmptyPlayerInventorySlot(AbstractContainerMenu menu) {
		if (menu == null) {
			return -1;
		}

		int start = Math.max(0, menu.slots.size() - 36);

		// Prefer main inventory first, then hotbar, so active hotbar items are not disturbed.
		int hotbarStart = Math.max(start, menu.slots.size() - 9);
		for (int i = start; i < hotbarStart; i++) {
			Slot slot = menu.slots.get(i);
			if (slot.getItem().isEmpty()) {
				return i;
			}
		}

		for (int i = hotbarStart; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (slot.getItem().isEmpty()) {
				return i;
			}
		}

		return -1;
	}

	static String getItemName(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return "";
		}

		return stack.getHoverName().getString();
	}

	static String getSlotItemName(Slot slot) {
		if (slot == null) {
			return "";
		}

		return getItemName(slot.getItem());
	}

	private static String normalize(String text) {
		return text == null ? "" : text.trim().toLowerCase();
	}
}
