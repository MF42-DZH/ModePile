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
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawDirectFont(engine, playerID, x + (int)(i * 16 * scale), y, str.substring(i, i + 1), RAINBOW_ORDER[(Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + i - offset) % RAINBOW_COLOURS]);
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
	 */
	public static void drawRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawScoreFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[(Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + i - offset) % RAINBOW_COLOURS]);
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
	 */
	public static void drawRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
		int offset = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				offset++;
			} else {
				receiver.drawMenuFont(engine, playerID, x + i, y, str.substring(i, i + 1), RAINBOW_ORDER[(Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + i - offset) % RAINBOW_COLOURS]);
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
	 * @param stringData String[] containing text and colour data.
	 * @param colourData int[] containing text and colour data.
	 * @param destinationX X of destination (uses drawScoreFont(...)).
	 * @param destinationY Y of destination (uses drawScoreFont(...)).
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
	 * @param stringData String[] containing text and colour data.
	 * @param colourData int[] containing text and colour data.
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
	 * @param stringData String[] containing text and colour data.
	 * @param colourData int[] containing text and colour data.
	 * @param destinationX X of destination (uses drawScoreFont(...)).
	 * @param destinationY Y of destination (uses drawScoreFont(...)).
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
}
