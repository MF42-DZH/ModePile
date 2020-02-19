package zeroxfc.nullpo.custom.modes;

public class Deltatris extends MarathonModeBase {
	/** Gravity denominator for G calculation */
	private static final int GRAVITY_DENOMINATOR = 256;

	/** Starting delays */
	private static final int START_ARE = 25;
	private static final int START_LINE_ARE = 25;
	private static final int START_LINE_DELAY = 40;
	private static final int START_DAS = 14;
	private static final int START_LOCK_DELAY = 30;
	private static final int START_GRAVITY = 4;

	/** Delays after reaching max piece */
	private static final int[] END_ARE = { 8, 6, 4 };
	private static final int[] END_LINE_ARE = { 5, 4, 3 };
	private static final int[] END_LINE_DELAY = { 9, 6, 3 };
	private static final int[] END_DAS = { 6, 6, 4 };
	private static final int[] END_LOCK_DELAY = { 17, 15, 8 };
	private static final int[] END_GRAVITY = { GRAVITY_DENOMINATOR * 40, GRAVITY_DENOMINATOR * 80, GRAVITY_DENOMINATOR * 120 };

	/** Pieces until max
	 *  Use interpolation to get the delays and gravity.
	 *
	 *  -- Planned --
	 *  GRAVITY, ARE, LINE ARE: Linear
	 *  DAS, LOCK DELAY, LINE DELAY: Ease-in-ease-out
	 */
	private static final int[] PIECES_MAX = { (int)(573 * 1.5), 573, 573 / 2 };

	/** Difficulties */
	private static final int DIFFICULTIES = 3;
	private static final int DIFFICULTY_EASY = 0;
	private static final int DIFFICULTY_NORMAL = 1;
	private static final int DIFFICULTY_HARD = 2;
}
