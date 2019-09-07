package zeroxfc.nullpo.custom.random;

import mu.nu.nullpo.game.component.Piece;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

import java.util.ArrayList;

public class DroughtedPieceBiasRandomizer extends Randomizer {
	private static final int ROLLS_INITIAL = 6;
	private static final int ROLLS = 3;

	private static final int INITIAL_PIECES = 4;

	int pieceCount;
	int[] counters;
	ArrayList<Integer> history;

	@Override
	public void init() {
		pieceCount = pieces.length;
		counters = new int[pieceCount];

		history = new ArrayList<>();

		history.add(Piece.PIECE_O);  // LEAST RECENT
		history.add(Piece.PIECE_S);  // --
		history.add(Piece.PIECE_Z);  // --
		history.add(Piece.PIECE_O);  // MOST RECENT
	}

	/**
	 * This is overridden as changing piece count screws with the counter averages.<br />
	 * Therefore this calso calls <code>init()</code>.
	 * @param pieceEnable Array of enabled pieces.
	 */
	@Override
	public void setPieceEnable(boolean[] pieceEnable) {
		int piece = 0;
		for (int i = 0; i < Piece.PIECE_COUNT; i++) {
			if  (pieceEnable[i]) piece++;
		}
		pieces = new int[piece];
		piece = 0;
		for (int i = 0; i < Piece.PIECE_COUNT; i++) {
			if (pieceEnable[i]) {
				pieces[piece] = i;
				piece++;
			}
		}

		init();
	}

	@Override
	public int next() {
		if (pieceCount == 1) return pieces[0];

		int v = -1;
		double divisor = 0;
		double[] rawChances = new double[pieceCount];
		double[] finalChances = new double[pieceCount];

		int total = 0;

		// Get total pieces rolled
		for (int i : counters) total += i;

		// Get raw chances and the final divisor for the values
		for (int i = 0; i < pieceCount; i++) {
			rawChances[i] = 1 - ((double)counters[i] / total);
			divisor += rawChances[i];
		}

		// Find final values
		for (int i = 0; i < pieceCount; i++) {
			double chance = rawChances[i] / divisor;
			for (int j = 0; j < i; j++) {
				chance += rawChances[j] / divisor;
			}

			finalChances[i] = chance;
		}

		// Roll up to three times to get piece that is not in history
		for (int i = 0; i < (total < INITIAL_PIECES ? ROLLS_INITIAL : ROLLS); i++) {
			double val = r.nextDouble();
			int idx = 0;
			for (int j = 1; j < pieceCount; j++) {
				if (val < finalChances[j] && val >= finalChances[j - 1]) idx = j;
			}

			v = pieces[idx];
			if (!history.contains(v)) {
				counters[idx]++;
				break;
			} else if (i == 2) {
				counters[idx]++;
			}
		}

		// Add piece to history
		appendHistory(v);
		return v;
	}

	private void appendHistory(int i) {
		history.remove(0);
		history.add(i);
	}
}
