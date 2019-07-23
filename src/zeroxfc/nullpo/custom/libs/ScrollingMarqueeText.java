package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class ScrollingMarqueeText {
	// Sizes of texts
	private static final int[] SIZES = { 8, 16, 32 };
	
	// Float sizes
	private static final float[] SCALES_FLOAT = { 0.5f, 1f, 2f };
	
	// Text size index names
	public static final int SIZE_SMALL = 0,
							SIZE_NORMAL = 1,
							SIZE_LARGE = 2;
	
	// Excess length
	private static final int EXCESS_LENGTH = 5;
	
	// Strings that make up the headings
	private String[] headings;
	
	// Strings that fill the info under the headings
	private String[] texts;
	
	// Whole string
	private String mainHeadingString;
	
	// Whole text string
	private String mainTextString;
	
	// Main colour
	private int headingColour;
	
	// Text colour
	private int textColour;
	
	/*
	 * Create a staff roll.
	 */
	public ScrollingMarqueeText(String[] headingArray, String[] textArray, int hColour, int tColour) {
		headings = headingArray;
		texts = textArray;
		headingColour = hColour;
		textColour = tColour;
		
		mainHeadingString = "";
		mainTextString = "";
		for (int i = 0; i < headings.length; i++) {
			mainTextString += (new String(new char[headings[i].length()]).replace("\0", " ")) + " " + texts[i] + ((i < headings.length - 1) ? " / " : "");
			mainHeadingString += headings[i] + " " + (new String(new char[texts[i].length()]).replace("\0", " ")) + ((i < headings.length - 1) ? " / " : "");
		}
	}
	
	public void drawAtY(GameEngine engine, EventReceiver receiver, int playerID, double y, int size, double progress) {
		int mainOffset1 = (40 * SIZES[size]) - (int)((progress) * ((40 * SIZES[size]) + ((mainHeadingString.length() + EXCESS_LENGTH) * SIZES[size])));
		int mainOffset2 = (40 * SIZES[size]) - (int)((progress) * ((40 * SIZES[size]) + ((mainTextString.length() + EXCESS_LENGTH) * SIZES[size])));
		receiver.drawDirectFont(engine, playerID, mainOffset1, (int)(y * SIZES[size]), mainHeadingString, headingColour, SCALES_FLOAT[size]);
		receiver.drawDirectFont(engine, playerID, mainOffset2, (int)(y * SIZES[size]), mainTextString, textColour, SCALES_FLOAT[size]);
	}
}
