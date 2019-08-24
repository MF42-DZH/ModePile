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

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

public class ExamSpinner {
	private static Logger log = Logger.getLogger(ExamSpinner.class);

	/**
	 * Default asset coordinates.
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

	private ResourceHolderCustomAssetExtension customHolder;
	private String header, subheading;
	private String gradeText;
	private String[] possibilities;
	private Integer selectedOutcome;
	private Boolean close;
	private boolean custom;

	private int lifeTime;

	{
		lifeTime = 0;
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
		int frame = lifeTime;

		if (close) {
			if (lifeTime < 300) {
				if (frame > 0) frame %= 160;
			} else if (lifeTime < 480) {
				frame = -1 - (lifeTime % 2);
			} else {
				if (selectedOutcome == 0) {
					frame = -3;
				} else {
					frame = -4;
				}
			}

		} else {
			if (selectedOutcome == 0) {
				frame = -3;
			} else {
				frame = -4;
			}
		}

		int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
		int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
		int size = 16;

		if (custom) {
			log.warn("CUSTOM NOT IMPLEMENTED YET.");
		} else {
			int b = 255;
			if (flag) b = 0;

			customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[0][1][0] / 2), baseY, SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1], SOURCE_DETAILS[0][0][0], SOURCE_DETAILS[0][0][1], SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1],255, 255, b, 255, 0);
			customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[1][1][0] / 2), baseY + size * 6, SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1], SOURCE_DETAILS[1][0][0], SOURCE_DETAILS[1][0][1], SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1],255, 255, b, 255, 0);

			int color = EventReceiver.COLOR_WHITE;
			if (flag) color = EventReceiver.COLOR_YELLOW;

			receiver.drawMenuFont(engine, playerID,5 - gradeText.length(), 10, gradeText, color, 2.0f);

			/*
			 * TODO:
			 *  > Redo whatever the hell this is.
			 *  > Add "slowing down".
			 */

			/*
			if (frame % 10 == 0 && (frame / 10 % 2) == 1) {
				customHolder.drawImage(engine, "default", baseX - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
				customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
				customHolder.drawImage(engine, "default", baseX + 160 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			} else if (frame % 10 == 0 && (frame / 10 % 2) == 0) {
				customHolder.drawImage(engine, "default", baseX - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
				customHolder.drawImage(engine, "default", baseX + 160 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
				customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			} else if (frame == -1) {
				customHolder.drawImage(engine, "default", baseX + 79 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			} else if (frame == -2) {
				customHolder.drawImage(engine, "default", baseX + 81 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			} else if (frame == -3) {
				customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
			} else if (frame == -4) {
				customHolder.drawImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			} else {
				customHolder.drawImage(engine, "default", baseX + pythonModulo(80 - (SOURCE_DETAILS[2][1][0] / 2) - (frame * 8), 160), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1],255, 255, b, 255, 0);
				customHolder.drawImage(engine, "default", baseX + pythonModulo(160 - (SOURCE_DETAILS[3][1][0] / 2) - (frame * 8), 160), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1],80, 80, 160, 255, 0);
			}
			*/

			// NEW CODE GOES HERE.
		}
	}

	public void update(GameEngine engine) {
		lifeTime++;
		if (lifeTime < 300 && lifeTime % 10 == 0) {
			engine.playSE("change");
		} else {
			if ((close && lifeTime == 480) || (!close && lifeTime == 1)) {
				if (selectedOutcome == 0) {
					engine.playSE("excellent");
				} else {
					engine.playSE("regret");
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
