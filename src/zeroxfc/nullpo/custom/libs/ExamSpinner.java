/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

public class ExamSpinner {
	private static Logger log = Logger.getLogger(ExamSpinner.class);

	/**
	 * Default asset coordinates and sizes.
	 */
	private static final int[][][] SOURCE_DETAILS = {
			new int[][] {
					new int[] { 0, 0 },
					new int[] { 128, 32 }
			},  // Result announcement
			new int[][] {
					new int[] { 0, 32 },
					new int[] { 128, 32 }
			},  // Examination grade
			new int[][] {
					new int[] { 32, 64 },
					new int[] { 64, 32 }
			},  // Pass
			new int[][] {
					new int[] { 0, 64 },
					new int[] { 96, 32 }
			}   // Fail
	};

	private static final int[] startXs = {
			0, 80, 160, 240  // P F P F
	};

	private static final int TravelClose = 320 * 32;

	private static final int[] endXs = {
			startXs[0] + TravelClose,
			startXs[1] + TravelClose,
			startXs[2] + TravelClose,
			startXs[3] + TravelClose
	};

	private static final int spinDuration = 360;

	private static final Piece HUGE_O;

	private ResourceHolderCustomAssetExtension customHolder;
	private String header, subheading;
	private String gradeText;
	private String[] possibilities;
	private Integer selectedOutcome;
	private Boolean close;
	private boolean custom;
	private int[] locations;
	private boolean clickedBefore;

	private int lifeTime;

	static {
		HUGE_O = new Piece(Piece.PIECE_O);
		HUGE_O.big = true;
		HUGE_O.setSkin(0);
		HUGE_O.direction = 0;
		HUGE_O.setColor(Block.BLOCK_COLOR_YELLOW);
	}

	{
		lifeTime = 0;

		// locations = endXs;
		locations = endXs.clone();

		clickedBefore = false;
	}

	/**
	 * Create a new promo exam graphic.
	 * @param gradeText What grade to display
	 * @param selectedOutcome 0 = pass, 1 = fail
	 * @param close Was it a close one?
	 */
	public ExamSpinner(String gradeText, int selectedOutcome, boolean close) {
		custom = false;

		if (gradeText == null) gradeText = "UNDEFINED";

		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage("res/graphics/examResultText.png", "default");

		this.gradeText = gradeText;
		this.selectedOutcome = selectedOutcome;
		this.close = close;
	}

	/*
	/**
	 * Creates a custom spinner. Make sure to fill in all fields. Note: use lowercase "z" for newlines.
	 * @param header Heading text
	 * @param subheading Subheading text
	 * @param gradeText Grade Qualification text
	 * @param possibilities How many possibilities? (Should be length 2.)
	 * @param selectedOutcome 0 for first outcome, 1 for second.
	 * @param close Was it a close one?
	 *
	public ExamSpinner(String header, String subheading, String gradeText, String[] possibilities, int selectedOutcome, boolean close) {
		custom = true;

		if (header == null) header = "PROMOTIONzEXAM";
		if (subheading == null) subheading = "EXAMzGRADE";
		if (gradeText == null) gradeText = "UNDEFINED";
		if (possibilities == null) possibilities = new String[] { "PASS", "FAIL" };

		this.header = header;
		this.subheading = subheading;
		this.gradeText = gradeText;
		this.possibilities = possibilities;
		this.selectedOutcome = selectedOutcome;
		this.close = close;
	}
	*/

	/*
	 * Plan: frame goes from 0 to 159;
	 * 0 = possibility 0 is on centre of screen.
	 * 80 = possibility 1 is on centre of screen.
	 */

	public void draw(EventReceiver receiver, GameEngine engine, int playerID, boolean flag) {
		int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
		int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
		int size = 16;

		if (custom) {
			log.warn("CUSTOM NOT IMPLEMENTED YET.");
		} else {
			int b = 255;
			if (flag) b = 0;

			customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[0][1][0] / 2), baseY, SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1], SOURCE_DETAILS[0][0][0], SOURCE_DETAILS[0][0][1], SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1],255, 255, b, 255, 0);
			customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[1][1][0] / 2), baseY + size * 4, SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1], SOURCE_DETAILS[1][0][0], SOURCE_DETAILS[1][0][1], SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1],255, 255, b, 255, 0);

			int color = EventReceiver.COLOR_WHITE;
			if (flag) color = EventReceiver.COLOR_YELLOW;

			receiver.drawMenuFont(engine, playerID,5 - gradeText.length(), 9, gradeText, color, 2.0f);

			int[] alphas = new int[locations.length];
			for (int i = 0; i < locations.length; i++) {
				int diff;
				int l = (locations[i] % 320);
				if (l <= 80) diff = l;
				else diff = 80 - (l - 80);
				if (diff < 0) diff = 0;

				final int alpha = (int)Interpolation.smoothStep(0, 255, (double)diff / 80d);
				alphas[i] = alpha;
			}

			// NEW CODE GOES HERE.
			if (close) {
				if (lifeTime < spinDuration) {
					customHolder.drawImage(engine, "default", baseX + (locations[0] % 320) - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, alphas[0], 0);
					customHolder.drawImage(engine, "default", baseX + (locations[1] % 320) - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, alphas[1], 0);
					customHolder.drawImage(engine, "default", baseX + (locations[2] % 320) - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, alphas[2], 0);
					customHolder.drawImage(engine, "default", baseX + (locations[3] % 320) - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, alphas[3], 0);
				} else if (lifeTime < spinDuration + 120) {
					int offset = (lifeTime % 3) - 1;
					customHolder.drawImage(engine, "default", baseX + 80 + offset - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);

					if (lifeTime >= spinDuration + 112) {
						int height = ((lifeTime - spinDuration - 113) * 2) - 1;
						int width = 3;

						RendererExtension.drawScaledPiece(receiver, baseX + width * 16, baseY + height * 16, HUGE_O, 1f, 0f);
					}
				} else {
					if (lifeTime == spinDuration + 120) {
						Block blk = new Block(Block.BLOCK_COLOR_YELLOW);
						for (int y = 13; y < 18; y++) {
							for (int x = 3; x < 8; x++) {
								int x2 = x * 16 + baseX;
								int y2 = y * 16 + baseY;
								RendererExtension.addBlockBreakEffect(receiver, x2, y2, blk);
							}
						}
					}
					if (selectedOutcome == 0) {
						// PASS
						customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
					} else {
						customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
					}
				}
			} else if (lifeTime >= 60) {
				if (selectedOutcome == 0) {
					// PASS
					customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
				} else {
					customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
				}
			}
		}
	}

	public void update(GameEngine engine) {
		lifeTime++;

		if (lifeTime == 60 && !close) {
			engine.playSE("linefall");
			if (selectedOutcome == 0) {
				engine.playSE("excellent");
			} else {
				engine.playSE("regret");
			}
		} else if (lifeTime == spinDuration + 120 && close) {
			engine.playSE("linefall");
			if (selectedOutcome == 0) {
				engine.playSE("excellent");
			} else {
				engine.playSE("regret");
			}
		}

		if (close && lifeTime <= spinDuration) {
			double j = (double)lifeTime / (double)spinDuration;
			// StringBuilder sb = new StringBuilder();
			// sb.append("[");
			for (int i = 0; i < locations.length; i++) {
				double res = Interpolation.smoothStep(endXs[i], startXs[i], 64, j);
				// sb.append(res).append(", ");
				locations[i] = (int)res;
			}
			// sb.append("]");
			// log.debug(lifeTime + ": (LOC) " + Arrays.toString(locations) + ", " + j);
			// log.debug(lifeTime + ": (RAW) " + sb.toString());

			for (int i = 0; i < locations.length; i++) {
				if (MathHelper.almostEqual(locations[i] % 320, 80, 24)) {
					if (!clickedBefore) {
						clickedBefore = true;
						engine.playSE("change");
					}
					break;
				}

				if (i == locations.length - 1) {
					clickedBefore = false;
				}
			}
		}
	}

	private int pythonModulo(int a, int b) {
		int c = a % b;
		if (c < 0) c = b + c;
		return c;
	}
}
