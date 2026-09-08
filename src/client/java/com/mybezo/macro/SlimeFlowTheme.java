package com.mybezo.macro;

public final class SlimeFlowTheme {
	private SlimeFlowTheme() {
	}

	// Dark layered theme with a teal accent.
	static final int WINDOW = 0xDA0A0E11;
	static final int WINDOW_STRONG = 0xEE080B0D;
	static final int HEADER = 0xF0121820;
	static final int HAIRLINE = 0x2BFFFFFF;

	static final int ROW = 0xA00E1417;
	static final int ROW_ALT = 0x8A11171B;
	static final int ROW_HOVER = 0xC0182028;

	static final int BG = WINDOW;
	static final int BG_SOFT = 0x900C1418;
	static final int CARD = 0x701A2228;
	static final int CARD_DARK = 0x60141B20;

	static final int BORDER = 0xCCE0393F;
	static final int BORDER_SOFT = 0x66E0393F;
	static final int HIGHLIGHT = 0x22E0393F;
	static final int SHADOW = 0x33000000;

	static final int TEXT = 0xFFE8E8E8;
	static final int MUTED = 0xFF8E8E8E;
	static final int RED = 0xFFE0393F;
	static final int GREEN = 0xFF3DDC97;
	static final int BLUE = 0xFF4FC3F7;
	static final int YELLOW = 0xFFFFD36B;
	static final int ORANGE = 0xFFFF9F43;
	static final int PURPLE = 0xFFB45CFF;
	/** Primary accent for standout actions (e.g. quick-amount buttons). */
	static final int ACCENT = 0xFFE0393F;

	static int surface(int rgb) {
		return 0x72000000 | (rgb & 0x00FFFFFF);
	}

	static int surfaceSoft(int rgb) {
		return 0x4E000000 | (rgb & 0x00FFFFFF);
	}
}
