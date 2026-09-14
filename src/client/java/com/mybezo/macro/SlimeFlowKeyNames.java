package com.mybezo.macro;

import org.lwjgl.glfw.GLFW;

public final class SlimeFlowKeyNames {
	private SlimeFlowKeyNames() {
	}

	static String name(int key) {
		if (key <= 0) return "None";
		try {
			String text = GLFW.glfwGetKeyName(key, 0);
			if (text != null && !text.isEmpty()) return text.toUpperCase();
		} catch (Throwable ignored) {
		}
		if (key == GLFW.GLFW_KEY_SPACE) return "SPACE";
		if (key == GLFW.GLFW_KEY_ESCAPE) return "ESC";
		if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) return "ENTER";
		if (key == GLFW.GLFW_KEY_BACKSPACE) return "BACK";
		if (key == GLFW.GLFW_KEY_LEFT_SHIFT) return "L-SHIFT";
		if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "R-SHIFT";
		if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
		return "KEY " + key;
	}

	static boolean canBind(int key) {
		return key > 0 && key != GLFW.GLFW_KEY_ESCAPE;
	}
}
