package zeroxfc.nullpo.custom.libs;

import java.util.Random;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class StaticFlyInText {
	// String to draw
	private String mainString;
		
	// Timings
	private int flyInTime;
		
	// Lifetime variable
	private int currentLifetime;
		
	// Vector array of positions
	private DoubleVector[] letterPositions;
		
	// Start location
	private DoubleVector[] startLocation;
		
	// Destination vector
	private DoubleVector[] destinationLocation;
		
	// Colour
	private int textColour;
		
	public int getTextColour() {
		return textColour;
	}

	public void setTextColour(int textColour) {
		this.textColour = textColour;
	}

	// Randomiser for start pos
	private Random positionRandomiser;
		
	// Text scale
	private float textScale;
	
	/**
	 * Create a text object that has its letters fly in from various points on the edges and stays in one spot.
	 * @param text String to draw.
	 * @param destinationX X-position of pixel of destination.
	 * @param destinationY Y-position of pixel of destination.
	 * @param timeIn Frames to fly in.
	 * @param colour Text colour.
	 * @param scale Text scale (0.5f, 1.0f, 2.0f).
	 * @param seed Random seed for position.
	 */
	public StaticFlyInText(String text, int destinationX, int destinationY, int timeIn, int colour, float scale, long seed) {
		mainString = text;
		flyInTime = timeIn;
		textColour = colour;
		textScale = scale;
		positionRandomiser = new Random(seed);
		
		letterPositions = new DoubleVector[mainString.length()];
		startLocation = new DoubleVector[mainString.length()];
		destinationLocation = new DoubleVector[mainString.length()];
		
		int sMod = 16;
		if (textScale == 2.0f) sMod = 32;
		if (textScale == 0.5f) sMod = 16;
		
		for (int i = 0; i < mainString.length(); i++) {
			int startX = 0, startY = 0;
			DoubleVector position = DoubleVector.zero();
			
			double dec1 = positionRandomiser.nextDouble();
			double dec2 = positionRandomiser.nextDouble();
			
			if (dec1 < 0.5) {
				startX = -sMod;
				if (dec2 < 0.5) startX = 41 * sMod;
				
				startY = (int)(positionRandomiser.nextDouble() * (32 * sMod)) - sMod;
			} else {
				startY = -sMod;
				if (dec2 < 0.5) startY = 31 * sMod;
				
				startX = (int)(positionRandomiser.nextDouble() * (42 * sMod)) - sMod;
			}
			
			position = new DoubleVector(startX, startY, false);

			letterPositions[i] = position;
			startLocation[i] = position;
			destinationLocation[i] = new DoubleVector(destinationX + (sMod * i), destinationY, false);
		}
		
		currentLifetime = 0;
	}
	
	/**
	 * Updates the position and lifetime of this object.
	 */
	public void update() {
		if (currentLifetime < flyInTime) {
			currentLifetime++;
			
			for (int i = 0; i < letterPositions.length; i++) {
				int v1 = (int)(Interpolation.lerp(startLocation[i].getX(), destinationLocation[i].getX(), ((double)(currentLifetime) / flyInTime)));
				int v2 = (int)(Interpolation.lerp(startLocation[i].getY(), destinationLocation[i].getY(), ((double)(currentLifetime) / flyInTime)));
				letterPositions[i] = new DoubleVector(v1, v2, false);
			}
		}
	}
	
	/**
	 * Draws the text at its current position.
	 */
	public void draw(GameEngine engine, EventReceiver receiver, int playerID) {
		for (int j = 0; j < letterPositions.length; j++) {
			receiver.drawDirectFont(engine, playerID,
					(int)letterPositions[j].getX(), (int)letterPositions[j].getY(),
					String.valueOf(mainString.charAt(j)),
					textColour, textScale);
		}
	}
}
