package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

import java.util.Random;

public class ShakingText {
	private Random textPositionRandomiser;

	/**
	 * Generates a text shaker object.
	 * @param random <code>Random</code> instance to use.
	 */
	public ShakingText(Random random) {
	    textPositionRandomiser = random;
	}

	/**
	 * Generates a text shaker object with an auto-generated randomiser.
	 */
	public ShakingText() {
	    this(new Random());
	}

	/**
	 * Draws some shaken text at some pixel coordinates.
	 * @param receiver     <code>EventReceiver</code> instance to draw on
	 * @param engine       Current <code>GameEngine</code> instance
	 * @param playerID     Current player ID (1P = 0)
	 * @param x            X-coordinate (pixel) of the top-left of the text anchor
	 * @param y            Y-coordinate (pixel) of the top-left of the text anchor
	 * @param maxDevianceX Maximum x-pixel-coordinate deviance in text
	 * @param maxDevianceY Maximum y-pixel-coordinate deviance in text
	 * @param text         Text to draw
	 * @param colour       Colour of text. Use colours from <code>EventReceiver</code>
	 * @param scale        Scale of drawn text.
	 */
	public void drawDirectText(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, int maxDevianceX, int maxDevianceY, String text, int colour, float scale) {
		final double offset = 16d * scale;
		for (int i = 0; i < text.length(); i++) {
			int offsetUsed = (int)(offset * i);
			int xDiff, yDiff;
			xDiff = textPositionRandomiser.nextInt(maxDevianceX * 2 + 1) - maxDevianceX;
			yDiff = textPositionRandomiser.nextInt(maxDevianceY * 2 + 1) - maxDevianceY;

			receiver.drawDirectFont(engine, playerID, x + offsetUsed + xDiff, y + yDiff, text.substring(i, i + 1), colour, scale);
		}
	}

	/**
	 * Draws some shaken text relative to the score display position.
	 * @param receiver     <code>EventReceiver</code> instance to draw on
	 * @param engine       Current <code>GameEngine</code> instance
	 * @param playerID     Current player ID (1P = 0)
	 * @param x            X-coordinate of the top-left of the text anchor
	 * @param y            Y-coordinate of the top-left of the text anchor
	 * @param maxDevianceX Maximum x-pixel-coordinate deviance in text
	 * @param maxDevianceY Maximum y-pixel-coordinate deviance in text
	 * @param text         Text to draw
	 * @param colour       Colour of text. Use colours from <code>EventReceiver</code>
	 * @param scale        Scale of drawn text.
	 */
	public void drawScoreText(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, int maxDevianceX, int maxDevianceY, String text, int colour, float scale) {
		int nx, ny;
		nx = receiver.getScoreDisplayPositionX(engine, playerID) + (x * ((scale <= 0.5f) ? 8 : 16));
		ny = receiver.getScoreDisplayPositionY(engine, playerID) + (y * ((scale <= 0.5f) ? 8 : 16));

		drawDirectText(receiver, engine, playerID, nx, ny, maxDevianceX, maxDevianceY, text, colour, scale);
	}

	/**
	 * Draws some shaken text relative to the field display position.
	 * @param receiver     <code>EventReceiver</code> instance to draw on
	 * @param engine       Current <code>GameEngine</code> instance
	 * @param playerID     Current player ID (1P = 0)
	 * @param x            X-coordinate of the top-left of the text anchor
	 * @param y            Y-coordinate of the top-left of the text anchor
	 * @param maxDevianceX Maximum x-pixel-coordinate deviance in text
	 * @param maxDevianceY Maximum y-pixel-coordinate deviance in text
	 * @param text         Text to draw
	 * @param colour       Colour of text. Use colours from <code>EventReceiver</code>
	 * @param scale        Scale of drawn text.
	 */
	public void drawMenuText(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, int maxDevianceX, int maxDevianceY, String text, int colour, float scale) {
		int nx, ny;
		nx = receiver.getFieldDisplayPositionX(engine, playerID) + (x * ((scale <= 0.5f) ? 8 : 16)) + 4;
		ny = receiver.getFieldDisplayPositionY(engine, playerID) + (y * ((scale <= 0.5f) ? 8 : 16)) + 52;

		drawDirectText(receiver, engine, playerID, nx, ny, maxDevianceX, maxDevianceY, text, colour, scale);
	}
}
