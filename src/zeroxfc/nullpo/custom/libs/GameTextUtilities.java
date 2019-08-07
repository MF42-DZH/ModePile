package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

import java.util.Arrays;
import java.util.Random;

public class GameTextUtilities {
	/** Rainbow colour order */
	public static final Integer[] RAINBOW_ORDER = {
			EventReceiver.COLOR_RED,
			EventReceiver.COLOR_ORANGE,
			EventReceiver.COLOR_YELLOW,
			EventReceiver.COLOR_WHITE,
			EventReceiver.COLOR_GREEN,
			EventReceiver.COLOR_CYAN,
			EventReceiver.COLOR_BLUE,
			EventReceiver.COLOR_DARKBLUE,
			EventReceiver.COLOR_PURPLE,
			EventReceiver.COLOR_PINK,
	};

	/** Rainbow colour count */
	public static final int RAINBOW_COLOURS = 10;

	/** Valid characters */
	private static final String CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopq";

	/** Sequential Character Phase */
	private static int CharacterPhase = 0;

	// region String Utilities

	/**
	 * Generates a completely random string.
	 * @param length Length of string
	 * @param randomEngine Random instance to use
	 * @return Random string, with all characters either being visible or a space.
	 */
	public static String randomString(int length, Random randomEngine) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++) {
			sb.append(CHARACTERS.charAt(randomEngine.nextInt(CHARACTERS.length())));
		}

		return sb.toString();
	}

	/**
	 * Completely obfuscates a string.
	 * @param str String to obfuscate
	 * @param randomEngine Random instance to use
	 * @return Obfuscated string.
	 */
	public static String obfuscateString(String str, Random randomEngine) {
		return obfuscateString(str, 1d, randomEngine);
	}

	/**
	 * Obfuscates a string with random characters.
	 * @param str String to obfuscate
	 * @param chance Chance of character obfuscation (0 < chance < 1)
	 * @param randomEngine Random instance to use
	 * @return Obfuscated string.
	 */
	public static String obfuscateString(String str, double chance, Random randomEngine) {
		if (chance <= 0) return str;

		StringBuilder sb = new StringBuilder(str);

		for (int i = 0; i < sb.length(); i++) {
			double c = randomEngine.nextDouble();
			if (c < chance) {
				sb.setCharAt(i, CHARACTERS.charAt(randomEngine.nextInt(CHARACTERS.length())));
			}
		}

		return sb.toString();
	}

	// endregion String Utilities

	// region Rainbow Text

	/**
	 * Draws a rainbow string using <code>drawDirectFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 */
	public static void drawRainbowDirectString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
		drawRainbowDirectString(receiver, engine, playerID, x, y, str, startColour, scale, false);
	}

	/**
	 * Draws a rainbow string using <code>drawScoreFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 */
	public static void drawRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
		drawRainbowScoreString(receiver, engine, playerID, x, y, str, startColour, scale, false);
	}

	/**
	 * Draws a rainbow string using <code>drawMenuFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 */
	public static void drawRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
		drawRainbowMenuString(receiver, engine, playerID, x, y, str, startColour, scale, false);
	}

	/**
	 * Draws a rainbow string using <code>drawDirectFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 * @param reverse Reverse order or not
	 */
	public static void drawRainbowDirectString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale, boolean reverse) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
				if (j < 0) j = RAINBOW_COLOURS - j;
				receiver.drawDirectFont(engine, playerID, x + (int)(i * 16 * scale), y, str.substring(i, i + 1), RAINBOW_ORDER[j]);
			}
		}
	}

	/**
	 * Draws a rainbow string using <code>drawScoreFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 * @param reverse Reverse order or not
	 */
	public static void drawRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale, boolean reverse) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
				if (j < 0) j = RAINBOW_COLOURS - j;
				receiver.drawScoreFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[j]);
			}
		}
	}

	/**
	 * Draws a rainbow string using <code>drawMenuFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param startColour Starting colour of text
	 * @param scale Scale of text
	 * @param reverse Reverse order or not
	 */
	public static void drawRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale, boolean reverse) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
				if (j < 0) j = RAINBOW_COLOURS - j;
				receiver.drawMenuFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[j]);
			}
		}
	}

	/**
	 * Draws a rainbow string using <code>drawDirectFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param randomEngine Random instance to use
	 * @param scale Scale of text
	 */
	public static void drawRandomRainbowDirectString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, Random randomEngine, float scale) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawDirectFont(engine, playerID, x + (int)(i * 16 * scale), y, str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)]);
			}
		}
	}

	/**
	 * Draws a rainbow string using <code>drawScoreFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param randomEngine Random instance to use
	 * @param scale Scale of text
	 */
	public static void drawRandomRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, Random randomEngine, float scale) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawScoreFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)]);
			}
		}
	}

	/**
	 * Draws a rainbow string using <code>drawMenuFont</code>.
	 * @param receiver EventReceiver used to draw
	 * @param engine Current GameEngine
	 * @param playerID Player ID (1P = 0)
	 * @param x X coordinate of top-left corner of text
	 * @param y Y coordinate of top-left corner of text
	 * @param str String to draw
	 * @param randomEngine Random instance to use
	 * @param scale Scale of text
	 */
	public static void drawRandomRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, Random randomEngine, float scale) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawMenuFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)]);
			}
		}
	}

	// endregion Rainbow Text

	// region Mixed Colour Text

	/**
	 * Draws a mixed-colour string to a location using <code>drawDirectFont</code>.
	 *
	 * Uses <code>String[] and int[]</code> for the text and colour data in this formet:
	 * <code>String[x]</code> the text at index x.
	 * <code>int[x]</code> the text colour at index x.
	 *
	 * For newlines, insert a -1 for colour.
	 * By default, all text is left-aligned.
	 *
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param stringData String[] containing text data.
	 * @param colourData int[] containing colour data.
	 * @param destinationX X of destination (uses drawDirectFont(...)).
	 * @param destinationY Y of destination (uses drawDirectFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawMixedColourDirectString(EventReceiver receiver, GameEngine engine, int playerID, String[] stringData, int[] colourData, int destinationX, int destinationY, float scale) {
		if (stringData.length != colourData.length || stringData.length == 0) return;

		int counterX = 0, counterY = 0;
		for (int i = 0; i < stringData.length; i++) {
			if (colourData[i] != -1) {
				receiver.drawDirectFont(engine, playerID, destinationX + (int)(counterX * 16 * scale), destinationY + (int)(counterY * 16 * scale), stringData[i], colourData[i], scale);
				counterX += stringData[i].length();
			} else {
				counterX = 0;
				counterY++;
			}
		}
	}

	/**
	 * Draws a mixed-colour string to a location using <code>drawScoreFont</code>.
	 *
	 * Uses <code>String[] and int[]</code> for the text and colour data in this formet:
	 * <code>String[x]</code> the text at index x.
	 * <code>int[x]</code> the text colour at index x.
	 *
	 * For newlines, insert a -1 for colour.
	 * By default, all text is left-aligned.
	 *
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param stringData String[] containing text data.
	 * @param colourData int[] containing colour data.
	 * @param destinationX X of destination (uses drawScoreFont(...)).
	 * @param destinationY Y of destination (uses drawScoreFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawMixedColourScoreString(EventReceiver receiver, GameEngine engine, int playerID, String[] stringData, int[] colourData, int destinationX, int destinationY, float scale) {
		if (stringData.length != colourData.length || stringData.length == 0) return;

		int counterX = 0, counterY = 0;
		for (int i = 0; i < stringData.length; i++) {
			if (colourData[i] != -1) {
				receiver.drawScoreFont(engine, playerID, destinationX + counterX, destinationY + counterY, stringData[i], colourData[i], scale);
				counterX += stringData[i].length();
			} else {
				counterX = 0;
				counterY++;
			}
		}
	}

	/**
	 * Draws a mixed-colour string to a location using <code>drawMenuFont</code>.
	 *
	 * Uses <code>String[] and int[]</code> for the text and colour data in this formet:
	 * <code>String[x]</code> the text at index x.
	 * <code>int[x]</code> the text colour at index x.
	 *
	 * For newlines, insert a -1 for colour.
	 * By default, all text is left-aligned.
	 *
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param stringData String[] containing text data.
	 * @param colourData int[] containing colour data.
	 * @param destinationX X of destination (uses drawMenuFont(...)).
	 * @param destinationY Y of destination (uses drawMenuFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawMixedColourMenuString(EventReceiver receiver, GameEngine engine, int playerID, String[] stringData, int[] colourData, int destinationX, int destinationY, float scale) {
		if (stringData.length != colourData.length || stringData.length == 0) return;

		int counterX = 0, counterY = 0;
		for (int i = 0; i < stringData.length; i++) {
			if (colourData[i] != -1) {
				receiver.drawMenuFont(engine, playerID, destinationX + counterX, destinationY + counterY, stringData[i], colourData[i], scale);
				counterX += stringData[i].length();
			} else {
				counterX = 0;
				counterY++;
			}
		}
	}

	//endregion Mixed Colour Text

	// region Colour Alternator Text

	/**
	 * Draws an alternating-colour string to a location using <code>drawDirectFont</code>.
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param string Text.
	 * @param colourData int[] containing colour data.
	 * @param offset Start offset of colour array.
	 * @param x X of destination (uses drawDirectFont(...)).
	 * @param y Y of destination (uses drawDirectFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawAlternatorColourDirectString(EventReceiver receiver, GameEngine engine, int playerID, String string, int[] colourData, int offset, int x, int y, float scale) {
		if (colourData.length <= 0) return;

		offset %= colourData.length;
		if (offset < 0) offset += colourData.length;

		for (int i = 0; i < string.length(); i++) {
			receiver.drawDirectFont(engine, playerID, x + (int)(16 * i * scale), y, string.substring(i, i + 1), colourData[(offset + i) % colourData.length], scale);
		}
	}

	/**
	 * Draws an alternating-colour string to a location using <code>drawScoreFont</code>.
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param string Text.
	 * @param colourData int[] containing colour data.
	 * @param offset Start offset of colour array.
	 * @param x X of destination (uses drawScoreFont(...)).
	 * @param y Y of destination (uses drawScoreFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawAlternatorColourScoreString(EventReceiver receiver, GameEngine engine, int playerID, String string, int[] colourData, int offset, int x, int y, float scale) {
		if (colourData.length <= 0) return;

		offset %= colourData.length;
		if (offset < 0) offset += colourData.length;

		for (int i = 0; i < string.length(); i++) {
			receiver.drawScoreFont(engine, playerID, x, y, string.substring(i, i + 1), colourData[(offset + i) % colourData.length], scale);
		}
	}

	/**
	 * Draws an alternating-colour string to a location using <code>drawMenuFont</code>.
	 * @param engine GameEngine to draw with.
	 * @param playerID Player to draw next to (0 = 1P).
	 * @param string Text.
	 * @param colourData int[] containing colour data.
	 * @param offset Start offset of colour array.
	 * @param x X of destination (uses drawMenuFont(...)).
	 * @param y Y of destination (uses drawMenuFont(...)).
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 */
	public static void drawAlternatorColourMenuString(EventReceiver receiver, GameEngine engine, int playerID, String string, int[] colourData, int offset, int x, int y, float scale) {
		if (colourData.length <= 0) return;

		offset %= colourData.length;
		if (offset < 0) offset += colourData.length;

		for (int i = 0; i < string.length(); i++) {
			receiver.drawScoreFont(engine, playerID, x, y, string.substring(i, i + 1), colourData[(offset + i) % colourData.length], scale);
		}
	}

	// endregion Colour Alternator Text

	// region Character Phase Functions

	/**
	 * Get current character in sequence.
	 * @return Character at phase.
	 */
	public static char getCurrentCharacter() {
		return CHARACTERS.charAt(CharacterPhase);
	}

	/**
	 * Gets current character in sequence with offset.
	 * @param offset Character offset.
	 * @return Offset sequence character.
	 */
	public static char getCurrentCharacter(int offset) {
		int i = CharacterPhase + offset;
		i %= CharacterPhase;
		if (i < 0) i += CharacterPhase;

		return CHARACTERS.charAt(i);
	}

	/**
	 * Increments character phase by 1.
	 */
	public static void updatePhase() {
		updatePhase(1);
	}

	/**
	 * Increments character phase by x.
	 * @param x Amount to increment by.
	 */
	public static void updatePhase(int x) {
		CharacterPhase = (CharacterPhase + x) % CHARACTERS.length();
		if (CharacterPhase < 0) CharacterPhase = CHARACTERS.length() + CharacterPhase;
	}

	/**
	 * Resets phase to 0.
	 */
	public static void resetPhase() {
		CharacterPhase = 0;
	}

	/**
	 * Sets the character phase.
	 * @param x Integer to set phase to.
	 */
	public static void setPhase(int x) {
		CharacterPhase = x % CHARACTERS.length();
		if (CharacterPhase < 0) CharacterPhase = CHARACTERS.length() + CharacterPhase;
	}

	// endregion Character Phase Functions
}
